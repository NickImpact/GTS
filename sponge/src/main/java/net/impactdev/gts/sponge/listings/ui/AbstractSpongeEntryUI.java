package net.impactdev.gts.sponge.listings.ui;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import net.impactdev.gts.api.listings.manager.ListingManager;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.gui.signs.SignQuery;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.impactor.api.utilities.Time;
import net.impactdev.impactor.api.utilities.mappings.Tuple;
import net.impactdev.impactor.sponge.ui.SpongeIcon;
import net.impactdev.impactor.sponge.ui.SpongeLayout;
import net.impactdev.impactor.sponge.ui.SpongeUI;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.api.listings.ui.AbstractEntryUI;
import net.impactdev.gts.api.listings.ui.EntrySelection;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.sponge.listings.SpongeAuction;
import net.impactdev.gts.sponge.listings.SpongeBuyItNow;
import net.impactdev.gts.sponge.listings.SpongeListing;
import net.impactdev.gts.sponge.listings.makeup.SpongeEntry;
import net.impactdev.gts.sponge.listings.ui.creator.TimeSelectMenu;
import net.impactdev.gts.sponge.pricing.SpongePrice;
import net.impactdev.gts.sponge.pricing.provided.MonetaryPrice;
import net.impactdev.gts.sponge.listings.ui.creator.SpongePriceTypeSelectionMenu;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.gts.sponge.utils.items.SkullCreator;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public abstract class AbstractSpongeEntryUI<E> extends AbstractEntryUI<Player, E, SpongeIcon> {

    private final SpongeUI display;

    private boolean auction = false;
    private Time duration = GTSPlugin.getInstance().getConfiguration().get(ConfigKeys.LISTING_TIME_LOW);

    protected SpongePrice<?, ?> price = new MonetaryPrice(50);

    public AbstractSpongeEntryUI(Player viewer) {
        super(viewer);
        this.display = SpongeUI.builder()
                .title(this.getTitle())
                .dimension(this.getDimensions())
                .build()
                .define(this.getDesign());
    }

    protected abstract Text getTitle();
    protected abstract InventoryDimension getDimensions();
    protected abstract SpongeLayout getDesign();

    protected abstract EntrySelection<? extends SpongeEntry<?>> getSelection();

    protected abstract int getPriceSlot();
    protected abstract int getSelectionTypeSlot();
    protected abstract int getTimeSlot();

    protected SpongeUI getDisplay() {
        return this.display;
    }

    @Override
    public void open(Player user) {
        this.display.open(user);
    }

    protected SpongePrice<?, ?> getPrice() {
        return this.price;
    }

    public void setPrice(SpongePrice<?, ?> price) {
        this.price = price;
    }

    @Override
    public SpongeIcon generateWaitingIcon(boolean auction) {
        return new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.CONCRETE)
                .add(Keys.DYE_COLOR, DyeColors.RED)
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Create BIN Listing"))
                .add(Keys.ITEM_LORE, Lists.newArrayList(
                        Text.of(TextColors.GRAY, "No item selected..."),
                        Text.EMPTY,
                        Text.of(TextColors.GRAY, "Click an item in your"),
                        Text.of(TextColors.GRAY, "to list it on the GTS"),
                        Text.of(TextColors.GRAY, "for a BIN purchase!")
                ))
                .build()
        );
    }

    public SpongeIcon createBINIcon() {
        MessageService<Text> parser = Impactor.getInstance().getRegistry().get(MessageService.class);

        ItemStack bin = ItemStack.builder()
                .itemType(ItemTypes.EMERALD)
                .add(Keys.DISPLAY_NAME, parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_ICON_BIN_CREATE_TITLE), Lists.newArrayList()))
                .add(Keys.ITEM_LORE, parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_ICON_BIN_CREATE_LORE), Lists.newArrayList()))
                .build();
        SpongeIcon icon = new SpongeIcon(bin);
        icon.addListener(clickable -> {
            this.auction = true;
            if(!(this.price instanceof MonetaryPrice)) {
                this.price = new MonetaryPrice(50);
            }
            this.display.setSlot(this.getPriceSlot(), this.createPriceIcon());
            this.display.setSlot(this.getSelectionTypeSlot(), this.createAuctionIcon());
        });
        return icon;
    }

    public SpongeIcon createAuctionIcon() {
        MessageService<Text> parser = Impactor.getInstance().getRegistry().get(MessageService.class);

        ItemStack bin = ItemStack.builder()
                .itemType(ItemTypes.GOLD_INGOT)
                .add(Keys.DISPLAY_NAME, parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_ICON_AUCTION_CREATE_TITLE), Lists.newArrayList()))
                .add(Keys.ITEM_LORE, parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_ICON_AUCTION_CREATE_LORE), Lists.newArrayList()))
                .build();
        SpongeIcon icon = new SpongeIcon(bin);
        icon.addListener(clickable -> {
            this.auction = false;
            this.display.setSlot(this.getPriceSlot(), this.createPriceIcon());
            this.display.setSlot(this.getSelectionTypeSlot(), this.createBINIcon());
        });
        return icon;
    }

    @Override
    public SpongeIcon generateConfirmIcon() {
        ItemStack rep = ItemStack.builder()
                .itemType(ItemTypes.CONCRETE)
                .add(Keys.DYE_COLOR, DyeColors.LIME)
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "TODO - Confirm Title"))
                .add(Keys.ITEM_LORE, Lists.newArrayList(

                ))
                .build();
        SpongeIcon confirm = new SpongeIcon(rep);
        confirm.addListener(clickable -> {
            SpongeEntry<?> entry = this.getSelection().createFromSelection();

            SpongeListing listing;
            if(this.auction) {
                Preconditions.checkArgument((this.price instanceof MonetaryPrice), "Auctions must have monetary prices");
                listing = (SpongeAuction) Auction.builder()
                        .lister(this.viewer.getUniqueId())
                        .entry(entry)
                        .start(((MonetaryPrice) this.price).getPrice().doubleValue())
                        .increment(0.02f)
                        .expiration(LocalDateTime.now().plusSeconds(this.duration.getTime()))
                        .build();
            } else {
                listing = (SpongeBuyItNow) BuyItNow.builder()
                        .lister(this.viewer.getUniqueId())
                        .entry(entry)
                        .price(this.getPrice())
                        .expiration(LocalDateTime.now().plusSeconds(this.duration.getTime()))
                        .build();
            }

            this.display.close(this.viewer);
            ListingManager<SpongeListing, SpongeAuction, SpongeBuyItNow> manager = Impactor.getInstance().getRegistry().get(ListingManager.class);
            manager.list(this.viewer.getUniqueId(), listing).exceptionally(error -> {
                ExceptionWriter.write(error);
                return false;
            });
        });
        return confirm;
    }

    @Override
    public SpongeIcon createNoneChosenIcon() {
        return new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.STONE_BUTTON)
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.YELLOW, "Click an item in your inventory!"))
                .add(Keys.ITEM_LORE, Lists.newArrayList(
                        Text.of(TextColors.GRAY, "Clicking on an item will"),
                        Text.of(TextColors.GRAY, "select it for your listing!")
                ))
                .build()
        );
    }

    @Override
    public SpongeIcon createTimeIcon() {
        MessageService<Text> parser = Impactor.getInstance().getRegistry().get(MessageService.class);
        List<Supplier<Object>> sources = Lists.newArrayList(
                () -> this.duration
        );

        ItemStack duration = ItemStack.builder()
                .itemType(ItemTypes.CLOCK)
                .add(Keys.DISPLAY_NAME, parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_TIME_DISPLAY_TITLE), sources))
                .add(Keys.ITEM_LORE, parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_TIME_DISPLAY_LORE), sources))
                .build();
        SpongeIcon time = new SpongeIcon(duration);
        time.addListener(clickable -> {
            new TimeSelectMenu(this.viewer, this, (ui, t) -> {
                this.duration = t;
                ui.getDisplay().setSlot(this.getTimeSlot(), this.createTimeIcon());
                ui.open(this.viewer);
            }).open();
        });
        return time;
    }

    @Override
    public SpongeIcon createPriceIcon() {
        MessageService<Text> parser = Impactor.getInstance().getRegistry().get(MessageService.class);
        List<Supplier<Object>> sources = Lists.newArrayList(
                () -> this.price,
                () -> new Tuple<>(this.price, !this.auction),
                () -> !this.auction
        );

        if(this.auction) {
            ItemStack rep = ItemStack.builder()
                    .itemType(ItemTypes.GOLD_NUGGET)
                    .add(Keys.DISPLAY_NAME, parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_PRICE_DISPLAY_TITLE), sources))
                    .add(Keys.ITEM_LORE, parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_PRICE_DISPLAY_LORE), sources))
                    .build();
            SpongeIcon icon = new SpongeIcon(rep);
            icon.addListener(clickable -> {
                SignQuery<Text, Player> query = SignQuery.<Text, Player>builder()
                        .position(new Vector3d(0, 1, 0))
                        .text(Lists.newArrayList(
                                Text.of(""),
                                Text.of("----------------"),
                                Text.of("Enter a Price"),
                                Text.of("for this Listing")
                        ))
                        .response(submission -> {
                            try {
                                double value = Double.parseDouble(submission.get(0));
                                if(value > 0) {
                                    this.price = new MonetaryPrice(value);
                                    SpongeIcon updated = this.createPriceIcon();
                                    this.getDisplay().setSlot(this.getPriceSlot(), updated);

                                    Impactor.getInstance().getScheduler().executeSync(() -> {
                                        this.open(this.viewer);
                                    });
                                    return true;
                                }
                                return false;
                            } catch (Exception e) {
                                return false;
                            }
                        })
                        .reopenOnFailure(true)
                        .build();
                this.viewer.closeInventory();
                query.sendTo(this.viewer);
            });
            return icon;
        } else {
            ItemStack selector = ItemStack.builder()
                    .from(SkullCreator.fromBase64("Mzk2Y2UxM2ZmNjE1NWZkZjMyMzVkOGQyMjE3NGM1ZGU0YmY1NTEyZjFhZGVkYTFhZmEzZmMyODE4MGYzZjcifX19"))
                    .add(Keys.DISPLAY_NAME, parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_PRICE_DISPLAY_TITLE), sources))
                    .add(Keys.ITEM_LORE, parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_PRICE_DISPLAY_LORE), sources))
                    .build();
            SpongeIcon icon = new SpongeIcon(selector);
            icon.addListener(clickable -> {
                new SpongePriceTypeSelectionMenu(this.viewer, this, (ui, price) -> {
                    if(price != null) {
                        ((AbstractSpongeEntryUI<E>) ui).setPrice((SpongePrice<?, ?>) price);
                        ((AbstractSpongeEntryUI<E>) ui).display.setSlot(this.getPriceSlot(), this.createPriceIcon());
                    }
                    ((AbstractSpongeEntryUI<E>)ui).open(this.viewer);
                }).open();
            });

            return icon;
        }
    }

    private final SpongeIcon red = this.border(DyeColors.RED);
    private final SpongeIcon darkGreen = this.border(DyeColors.GREEN);
    private final SpongeIcon green = this.border(DyeColors.LIME);

    @Override
    public void style(boolean selected) {
        BiFunction<Boolean, SpongeIcon, SpongeIcon> applier = (state, color) -> {
            if(state) {
                return color;
            }

            return this.red;
        };

        this.display.setSlot(10, applier.apply(selected, this.darkGreen));
        this.display.setSlot(11, applier.apply(selected, this.darkGreen));
        this.display.setSlot(15, applier.apply(selected, this.darkGreen));
        this.display.setSlot(16, applier.apply(selected, this.darkGreen));

        this.display.setSlot( 3, applier.apply(selected, this.green));
        this.display.setSlot( 4, applier.apply(selected, this.green));
        this.display.setSlot( 5, applier.apply(selected, this.green));
        this.display.setSlot(12, applier.apply(selected, this.green));
        this.display.setSlot(14, applier.apply(selected, this.green));
        this.display.setSlot(21, applier.apply(selected, this.green));
        this.display.setSlot(22, applier.apply(selected, this.green));
        this.display.setSlot(23, applier.apply(selected, this.green));
    }

    protected SpongeIcon border(DyeColor color) {
        return new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.STAINED_GLASS_PANE)
                .add(Keys.DISPLAY_NAME, Text.EMPTY)
                .add(Keys.DYE_COLOR, color)
                .build());
    }

}
