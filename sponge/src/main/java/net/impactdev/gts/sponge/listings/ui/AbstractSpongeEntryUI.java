package net.impactdev.gts.sponge.listings.ui;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import net.impactdev.gts.api.listings.manager.ListingManager;
import net.impactdev.gts.api.listings.prices.Price;
import net.impactdev.gts.api.listings.ui.AbstractEntryUI;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;
import net.impactdev.gts.sponge.listings.ui.creator.SpongePriceTypeSelectionMenu;
import net.impactdev.gts.sponge.listings.ui.creator.TimeSelectMenu;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.placeholders.PlaceholderSources;
import net.impactdev.impactor.api.platform.players.PlatformPlayer;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.impactor.api.ui.containers.ImpactorUI;
import net.impactdev.impactor.api.ui.containers.icons.DisplayProvider;
import net.impactdev.impactor.api.ui.containers.icons.Icon;
import net.impactdev.impactor.api.ui.containers.layouts.Layout;
import net.impactdev.impactor.api.utilities.Time;
import net.impactdev.impactor.api.utilities.mappings.Tuple;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.api.listings.ui.EntrySelection;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.sponge.listings.SpongeAuction;
import net.impactdev.gts.sponge.listings.SpongeBuyItNow;
import net.impactdev.gts.sponge.listings.SpongeListing;
import net.impactdev.gts.sponge.listings.makeup.SpongeEntry;
import net.impactdev.gts.sponge.pricing.SpongePrice;
import net.impactdev.gts.sponge.pricing.provided.MonetaryPrice;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.gts.sponge.utils.items.SkullCreator;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.menu.ClickType;
import org.spongepowered.api.item.inventory.menu.ClickTypes;
import org.spongepowered.math.vector.Vector2i;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiFunction;

public abstract class AbstractSpongeEntryUI<E extends EntrySelection<?>> extends AbstractEntryUI<ServerPlayer, E, Icon<ItemStack>> {

    private final ImpactorUI display;

    private boolean auction;
    private Time duration = GTSPlugin.instance().configuration().main().get(ConfigKeys.LISTING_TIME_LOW);

    protected SpongePrice<?, ?> price = new MonetaryPrice(GTSPlugin.instance().configuration().main().get(ConfigKeys.LISTINGS_MIN_PRICE));

    public AbstractSpongeEntryUI(ServerPlayer viewer) {
        super(viewer);

        this.auction = this.getTargetMode();
        ImpactorUI.UIBuilder builder= ImpactorUI.builder()
                .title(this.getTitle())
                .provider(Key.key("gts:entry-ui"))
                .layout(this.getDesign());

        this.display = this.modifyDisplayBuilder(builder).build();
    }

    private boolean getTargetMode() {
        Config config = GTSPlugin.instance().configuration().main();
        if(config.get(ConfigKeys.BINS_ENABLED)) {
            return false;
        } else return config.get(ConfigKeys.AUCTIONS_ENABLED);
    }

    protected abstract ImpactorUI.UIBuilder modifyDisplayBuilder(ImpactorUI.UIBuilder builder);
    protected abstract Component getTitle();
    protected abstract Vector2i getDimensions();
    protected abstract Layout getDesign();

    protected abstract EntrySelection<? extends SpongeEntry<?>> getSelection();

    protected abstract int getChosenSlot();
    protected abstract int getPriceSlot();
    protected abstract int getSelectionTypeSlot();
    protected abstract int getTimeSlot();
    protected abstract int getConfirmSlot();

    protected abstract double getMinimumMonetaryPrice(E chosen);

    protected ImpactorUI getDisplay() {
        return this.display;
    }

    @Override
    public void open(ServerPlayer user) {
        this.display.open(PlatformPlayer.from(user));
    }

    public void open(ServerPlayer user, boolean auction) {
        if(auction) {
            this.auction = true;
            this.getDisplay().set(this.createPriceIcon(), this.getPriceSlot());
            this.getDisplay().set(this.createAuctionIcon(), this.getSelectionTypeSlot());
        }
        this.display.open(PlatformPlayer.from(user));
    }

    public void open(ServerPlayer user, E entry, boolean auction, long duration) {
        this.setChosen(entry);
        if(duration != -1) {
            this.duration = new Time(duration);
        }
        this.getDisplay().set(this.createChosenIcon(), this.getChosenSlot());
        this.getDisplay().set(this.createTimeIcon(), this.getTimeSlot());
        this.style(true);
        this.getDisplay().set(this.generateConfirmIcon(), this.getConfirmSlot());
        if(auction) {
            this.auction = true;
            this.getDisplay().set(this.createPriceIcon(), this.getPriceSlot());
            this.getDisplay().set(this.createAuctionIcon(), this.getSelectionTypeSlot());
        }
        this.display.open(PlatformPlayer.from(user));
    }

    protected SpongePrice<?, ?> getPrice() {
        return this.price;
    }

    public void setPrice(SpongePrice<?, ?> price) {
        this.price = price;
    }

    @Override
    public void setChosen(E chosen) {
        this.chosen = chosen;
        if(this.price instanceof MonetaryPrice) {
            this.price = new MonetaryPrice(Math.max(
                    GTSPlugin.instance().configuration().main().get(ConfigKeys.LISTINGS_MIN_PRICE),
                    this.getMinimumMonetaryPrice(chosen)
            ));
        }
    }

    @Override
    public Icon<ItemStack> generateWaitingIcon(boolean auction) {
        MessageService parser = Impactor.getInstance().getRegistry().get(MessageService.class);

        return Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(ItemStack.builder()
                        .itemType(ItemTypes.RED_CONCRETE)
                        .add(Keys.CUSTOM_NAME, parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.AWAITING_CREATE_LISTING_TITLE)))
                        .add(Keys.LORE, parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.AWAITING_CREATE_LISTING_LORE)))
                        .build()
                ))
                .build();
    }

    public Icon<ItemStack> createBINIcon() {
        MessageService parser = Impactor.getInstance().getRegistry().get(MessageService.class);

        ItemStack bin = ItemStack.builder()
                .itemType(ItemTypes.EMERALD)
                .add(Keys.CUSTOM_NAME, parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_ICON_BIN_CREATE_TITLE)))
                .add(Keys.LORE, parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_ICON_BIN_CREATE_LORE)))
                .build();
        return Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(bin))
                .listener(context -> {
                    if(GTSPlugin.instance().configuration().main().get(ConfigKeys.AUCTIONS_ENABLED)) {
                        this.auction = true;
                        if (!(this.price instanceof MonetaryPrice)) {
                            if (this.chosen != null) {
                                this.price = new MonetaryPrice(this.getMinimumMonetaryPrice(this.chosen));
                            } else {
                                this.price = new MonetaryPrice(GTSPlugin.instance().configuration().main().get(ConfigKeys.LISTINGS_MIN_PRICE));
                            }
                        }
                        this.display.set(this.createPriceIcon(), this.getPriceSlot());
                        this.display.set(this.createAuctionIcon(), this.getSelectionTypeSlot());
                    }
                    return false;
                })
                .build();
    }

    public Icon<ItemStack> createAuctionIcon() {
        MessageService parser = Impactor.getInstance().getRegistry().get(MessageService.class);

        ItemStack bin = ItemStack.builder()
                .itemType(ItemTypes.GOLD_INGOT)
                .add(Keys.CUSTOM_NAME, parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_ICON_AUCTION_CREATE_TITLE)))
                .add(Keys.LORE, parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_ICON_AUCTION_CREATE_LORE)))
                .build();
        return Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(bin))
                .listener(context -> {
                    if(GTSPlugin.instance().configuration().main().get(ConfigKeys.BINS_ENABLED)) {
                        this.auction = false;
                        this.display.set(this.createPriceIcon(), this.getPriceSlot());
                        this.display.set(this.createBINIcon(), this.getSelectionTypeSlot());
                    }

                    return false;
                })
                .build();
    }

    @Override
    public Icon<ItemStack> generateConfirmIcon() {
        MessageService parser = Impactor.getInstance().getRegistry().get(MessageService.class);

        ItemStack rep = ItemStack.builder()
                .itemType(ItemTypes.LIME_CONCRETE)
                .add(Keys.CUSTOM_NAME, parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.CONFIRM_CREATE_LISTING_TITLE)))
                .add(Keys.LORE, parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.CONFIRM_CREATE_LISTING_LORE)))
                .build();

        return Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(rep))
                .listener(context -> {
                    ClickType<?> type = context.require(ClickType.class);
                    if(type.equals(ClickTypes.KEY_THROW_ALL.get()) || type.equals(ClickTypes.KEY_THROW_ONE.get())) {
                        return false;
                    }

                    SpongeEntry<?> entry = this.getSelection().createFromSelection();
                    SpongeListing listing;
                    if(this.auction) {
                        Preconditions.checkArgument((this.price instanceof MonetaryPrice), "Auctions must have monetary prices");
                        listing = (SpongeAuction) Auction.builder()
                                .lister(this.viewer.uniqueId())
                                .entry(entry)
                                .start(((MonetaryPrice) this.price).getPrice().doubleValue())
                                .increment(GTSPlugin.instance().configuration().main().get(ConfigKeys.AUCTIONS_INCREMENT_RATE))
                                .expiration(LocalDateTime.now().plusSeconds(this.duration.getTime()))
                                .build();
                    } else {
                        listing = (SpongeBuyItNow) BuyItNow.builder()
                                .lister(this.viewer.uniqueId())
                                .entry(entry)
                                .price(this.getPrice())
                                .expiration(LocalDateTime.now().plusSeconds(this.duration.getTime()))
                                .build();
                    }

                    this.display.close(PlatformPlayer.from(this.viewer));
                    ListingManager<SpongeListing, SpongeAuction, SpongeBuyItNow> manager = Impactor.getInstance().getRegistry().get(ListingManager.class);
                    manager.list(this.viewer.uniqueId(), listing).exceptionally(error -> {
                        ExceptionWriter.write(error);
                        return false;
                    });

                    return false;
                })
                .build();
    }

    @Override
    public Icon<ItemStack> createNoneChosenIcon() {
        return Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(ItemStack.builder()
                        .itemType(ItemTypes.STONE_BUTTON)
                        .add(Keys.CUSTOM_NAME, Component.text("Click an item in you inventory").color(NamedTextColor.YELLOW))
                        .add(Keys.LORE, Lists.newArrayList(
                                Component.text("Clicking on an item will").color(NamedTextColor.GRAY),
                                Component.text("select it for your listing!").color(NamedTextColor.GRAY)
                        ))
                        .build()
                ))
                .build();
    }

    @Override
    public Icon<ItemStack> createTimeIcon() {
        MessageService parser = Impactor.getInstance().getRegistry().get(MessageService.class);
        PlaceholderSources sources = PlaceholderSources.builder()
                .append(Time.class, () -> this.duration)
                .build();

        List<Component> result = Lists.newArrayList();
        result.addAll(parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_TIME_DISPLAY_LORE), sources));

        if(GTSPlugin.instance().configuration().main().get(ConfigKeys.FEES_ENABLED)) {
            result.addAll(parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_TIME_DISPLAY_FEES), sources));
        }

        result.addAll(parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_COMPONENT_EDIT_LORE), sources));

        ItemStack duration = ItemStack.builder()
                .itemType(ItemTypes.CLOCK)
                .add(Keys.DISPLAY_NAME, parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_TIME_DISPLAY_TITLE), sources))
                .add(Keys.LORE, result)
                .build();

        return Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(duration))
                .listener(context -> {
                    new TimeSelectMenu(this.viewer, this, (ui, t) -> {
                        if (t != null && t.getTime() != 0) {
                            this.duration = t;
                            ui.getDisplay().set(this.createTimeIcon(), this.getTimeSlot());
                        }
                        ui.open(this.viewer);
                    }).open();

                    return false;
                })
                .build();
    }

    @Override
    public Icon<ItemStack> createPriceIcon() {
        MessageService parser = Impactor.getInstance().getRegistry().get(MessageService.class);
        PlaceholderSources sources = PlaceholderSources.builder()
                .append(Price.class, () -> this.price)
                .append(Tuple.class, () -> new Tuple<>(this.price, !this.auction))
                .append(Boolean.class, () -> !this.auction)
                .build();

        List<Component> result = Lists.newArrayList();
        result.addAll(parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_PRICE_DISPLAY_LORE), sources));

        if(GTSPlugin.instance().configuration().main().get(ConfigKeys.FEES_ENABLED)) {
            result.addAll(parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_PRICE_DISPLAY_FEES), sources));
        }

        result.addAll(parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_COMPONENT_EDIT_LORE), sources));

        if(this.auction) {
            ItemStack rep = ItemStack.builder()
                    .itemType(ItemTypes.GOLD_NUGGET)
                    .add(Keys.DISPLAY_NAME, parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_PRICE_DISPLAY_TITLE), sources))
                    .add(Keys.LORE, result)
                    .build();

            return Icon.builder(ItemStack.class)
                    .display(new DisplayProvider.Constant<>(rep))
                    .listener(context -> {
                        // TODO - Come up with solution to replace
//                        SignQuery<Text, Player> query = SignQuery.<Text, Player>builder()
//                                .position(new Vector3d(0, 1, 0))
//                                .text(Lists.newArrayList(
//                                        Text.of(""),
//                                        Text.of("----------------"),
//                                        Text.of("Enter a Price"),
//                                        Text.of("for this Listing")
//                                ))
//                                .response(submission -> {
//                                    try {
//                                        double value = Double.parseDouble(submission.get(0));
//                                        if(value > 0) {
//                                            this.price = new MonetaryPrice(value);
//                                            SpongeIcon updated = this.createPriceIcon();
//                                            this.getDisplay().setSlot(this.getPriceSlot(), updated);
//
//                                            Impactor.getInstance().getScheduler().executeSync(() -> {
//                                                this.open(this.viewer);
//                                            });
//                                            return true;
//                                        }
//                                        return false;
//                                    } catch (Exception e) {
//                                        return false;
//                                    }
//                                })
//                                .reopenOnFailure(true)
//                                .build();
//                        this.viewer.closeInventory();
//                        query.sendTo(this.viewer);

                        return false;
                    })
                    .build();
        } else {
            ItemStack selector = ItemStack.builder()
                    .from(SkullCreator.fromBase64("Mzk2Y2UxM2ZmNjE1NWZkZjMyMzVkOGQyMjE3NGM1ZGU0YmY1NTEyZjFhZGVkYTFhZmEzZmMyODE4MGYzZjcifX19"))
                    .add(Keys.DISPLAY_NAME, parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_PRICE_DISPLAY_TITLE), sources))
                    .add(Keys.LORE, result)
                    .build();

            return Icon.builder(ItemStack.class)
                    .display(new DisplayProvider.Constant<>(selector))
                    .listener(context -> {
                        new SpongePriceTypeSelectionMenu(this.viewer, this, (ui, price) -> {
                            if(price != null) {
                                ((AbstractSpongeEntryUI<E>) ui).setPrice((SpongePrice<?, ?>) price);
                                ((AbstractSpongeEntryUI<E>) ui).display.set(this.createPriceIcon(), this.getPriceSlot());
                            }
                            ((AbstractSpongeEntryUI<E>)ui).open(this.viewer);
                        }).open();

                        return false;
                    })
                    .build();
        }
    }

    private final Icon<ItemStack> red = this.border(ItemTypes.BLACK_STAINED_GLASS_PANE.get());
    private final Icon<ItemStack> darkGreen = this.border(ItemTypes.GREEN_STAINED_GLASS_PANE.get());
    private final Icon<ItemStack> green = this.border(ItemTypes.LIME_STAINED_GLASS_PANE.get());

    @Override
    public void style(boolean selected) {
        BiFunction<Boolean, Icon<ItemStack>, Icon<ItemStack>> applier = (state, color) -> {
            if(state) {
                return color;
            }

            return this.red;
        };

        this.display.set(applier.apply(selected, this.darkGreen), 10);
        this.display.set(applier.apply(selected, this.darkGreen), 11);
        this.display.set(applier.apply(selected, this.darkGreen), 15);
        this.display.set(applier.apply(selected, this.darkGreen), 16);

        this.display.set(applier.apply(selected, this.green), 3);
        this.display.set(applier.apply(selected, this.green), 4);
        this.display.set(applier.apply(selected, this.green), 5);
        this.display.set(applier.apply(selected, this.green), 12);
        this.display.set(applier.apply(selected, this.green), 14);
        this.display.set(applier.apply(selected, this.green), 21);
        this.display.set(applier.apply(selected, this.green), 22);
        this.display.set(applier.apply(selected, this.green), 23);
    }

    protected Icon<ItemStack> border(ItemType type) {
        return Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(ItemStack.builder()
                    .itemType(type)
                    .add(Keys.CUSTOM_NAME, Component.empty())
                    .build())
                )
                .build();
    }

}
