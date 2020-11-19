package net.impactdev.gts.ui.submenu.browser;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import net.impactdev.gts.GTSSpongePlugin;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.data.registry.GTSKeyMarker;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.api.listings.manager.ListingManager;
import net.impactdev.gts.api.listings.prices.Price;
import net.impactdev.gts.api.listings.prices.PriceManager;
import net.impactdev.gts.api.messaging.message.errors.ErrorCodes;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.sponge.listings.SpongeAuction;
import net.impactdev.gts.sponge.listings.SpongeBuyItNow;
import net.impactdev.gts.manager.SpongeListingManager;
import net.impactdev.gts.sponge.ui.SpongeAsyncPage;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.impactor.api.utilities.mappings.Tuple;
import net.impactdev.impactor.sponge.ui.SpongeIcon;
import net.impactdev.impactor.sponge.ui.SpongeLayout;
import net.impactdev.impactor.sponge.ui.SpongeUI;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.sponge.listings.SpongeListing;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static net.impactdev.gts.sponge.utils.Utilities.PARSER;
import static net.impactdev.gts.sponge.utils.Utilities.readMessageConfigOption;

public class SpongeSelectedListingMenu {

    private Player viewer;
    private SpongeUI display;

    private SpongeListing listing;

    private Supplier<SpongeAsyncPage<?>> parent;

    public SpongeSelectedListingMenu(Player viewer, SpongeListing listing, Supplier<SpongeAsyncPage<?>> parent, boolean update) {
        this.viewer = viewer;
        this.listing = listing;
        this.parent = parent;

        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);

        boolean isLister = viewer.getUniqueId().equals(listing.getLister());

        this.display = SpongeUI.builder()
                .title(service.parse(Utilities.readMessageConfigOption(
                        isLister ? MsgConfigKeys.UI_MENU_LISTING_SELECTED_LISTER :
                                MsgConfigKeys.UI_MENU_LISTING_SELECTED_OTHER
                )))
                .dimension(InventoryDimension.of(9, 6))
                .build()
                .define(this.design());

        if(update) {
            Task task = Sponge.getScheduler().createTaskBuilder()
                    .execute(this::update)
                    .interval(1, TimeUnit.SECONDS)
                    .submit(GTSPlugin.getInstance().getBootstrap());
            this.display.attachCloseListener(close -> task.cancel());
        }
    }

    public void open() {
        this.display.open(this.viewer);
    }

    private void update() {
        SpongeIcon icon = new SpongeIcon(this.listing.getEntry()
                .getDisplay(this.viewer.getUniqueId(), this.listing)
                .get()
        );
        this.display.setSlot(13, icon);
    }

    private SpongeLayout design() {
        SpongeLayout.SpongeLayoutBuilder builder = SpongeLayout.builder();
        SpongeIcon colored = new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.STAINED_GLASS_PANE)
                .add(Keys.DYE_COLOR, DyeColors.LIGHT_BLUE)
                .add(Keys.DISPLAY_NAME, Text.EMPTY)
                .build()
        );

        builder.border();
        builder.slots(colored, 3, 4, 5, 10, 11, 12, 14, 15, 16, 21, 22, 23);
        builder.slots(SpongeIcon.BORDER, 19, 20, 24, 25);
        builder.row(SpongeIcon.BORDER, 3);

        SpongeIcon icon = new SpongeIcon(this.listing.getEntry()
                .getDisplay(this.viewer.getUniqueId(), this.listing)
                .get()
        );
        builder.slot(icon, 13);

        SpongeIcon back = new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.BARRIER)
                .add(Keys.DISPLAY_NAME, PARSER.parse(readMessageConfigOption(MsgConfigKeys.UI_GENERAL_BACK), Lists.newArrayList(() -> this.viewer)))
                .build()
        );
        back.addListener(clickable -> {
            this.parent.get().open();
        });
        builder.slot(back, 38);

        if(this.listing.getLister().equals(this.viewer.getUniqueId())) {
            builder.slot(this.createRemover(), 42);
        } else {
            this.createSubmitters(builder);
        }

        return builder.build();
    }

    private void createSubmitters(SpongeLayout.SpongeLayoutBuilder builder) {
        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);

        if(this.listing instanceof Auction) {
            Auction auction = (Auction) this.listing;
            double current = auction.hasAnyBidsPlaced() ? auction.getCurrentPrice() : auction.getStartingPrice();
            double newBid = auction.hasAnyBidsPlaced() ? current * (1.0 + GTSPlugin.getInstance().getConfiguration().get(ConfigKeys.AUCTIONS_INCREMENT_RATE)) : current;

            Currency currency = GTSPlugin.getInstance().as(GTSSpongePlugin.class).getEconomy().getDefaultCurrency();
            Tuple<Boolean, Boolean> affordability = this.getBalanceAbilities(currency, auction.hasAnyBidsPlaced() ? current : newBid);

            SpongeIcon normal = new SpongeIcon(ItemStack.builder()
                    .itemType(ItemTypes.WRITABLE_BOOK)
                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "TODO - Place Bid"))
                    .add(Keys.ITEM_LORE, Lists.newArrayList(
                            Text.of(TextColors.RED, "TODO - Bid lore"),
                            Text.of(TextColors.GRAY, "New Bid: ", TextColors.YELLOW, currency.format(new BigDecimal(newBid)))
                    ))
                    .build()
            );
            normal.addListener(clickable -> {
                if(auction.getHighBid().map(bid -> bid.getFirst().equals(this.viewer.getUniqueId())).orElse(false)) {
                    this.viewer.sendMessage(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_AUCTIONS_ALREADY_TOP_BIDDER)));
                } else {
                    if (affordability.getFirst()) {
                        this.display.close(this.viewer);
                        SpongeListingManager manager = (SpongeListingManager) Impactor.getInstance().getRegistry().get(ListingManager.class);
                        manager.bid(this.viewer.getUniqueId(), (SpongeAuction) auction, newBid);
                    } else {
                        this.viewer.sendMessage(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_AUCTIONS_CANT_AFFORD_BID)));
                    }
                }
            });
            builder.slot(normal, 41);

            if(affordability.getSecond()) {
                SpongeIcon custom = new SpongeIcon(ItemStack.builder().itemType(ItemTypes.BARRIER).build());
                builder.slot(custom, 43);
            }
        } else {
            ItemStack display = ItemStack.builder()
                    .itemType(ItemTypes.CONCRETE)
                    .add(Keys.DYE_COLOR, DyeColors.LIME)
                    .add(Keys.DISPLAY_NAME, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.CONFIRM_PURCHASE)))
                    .build();
            SpongeIcon icon = new SpongeIcon(display);
            icon.addListener(clickable -> {
                BuyItNow bin = (BuyItNow) this.listing;

                Price<?, ?, ?> price = bin.getPrice();
                Optional<PriceManager.PriceSelectorUI<SpongeUI>> selector = GTSService.getInstance().getGTSComponentManager()
                        .getPriceManager(price.getClass().getAnnotation(GTSKeyMarker.class).value())
                        .map(ui -> (PriceManager<?, Player>) ui)
                        .orElseThrow(() -> new IllegalStateException("Unable to find price manager for " + price.getClass().getAnnotation(GTSKeyMarker.class).value()))
                        .getSelector(this.viewer, price, source -> {
                            SpongeListingManager manager = (SpongeListingManager) Impactor.getInstance().getRegistry().get(ListingManager.class);
                            manager.purchase(this.viewer.getUniqueId(), (SpongeBuyItNow) bin, source);
                        });
                if(selector.isPresent()) {
                    selector.get().getDisplay().open(this.viewer);
                } else {
                    // For prices that have no need for a source
                    SpongeListingManager manager = (SpongeListingManager) Impactor.getInstance().getRegistry().get(ListingManager.class);
                    manager.purchase(this.viewer.getUniqueId(), (SpongeBuyItNow) bin, null);
                }
            });
            builder.slot(icon, 42);
        }
    }

    private SpongeIcon createRemover() {
        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);

        ItemStack display = ItemStack.builder()
                .itemType(ItemTypes.ANVIL)
                .add(Keys.DISPLAY_NAME, PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_ICON_SELECTED_REMOVE_TITLE)))
                .add(Keys.ITEM_LORE, PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_ICON_SELECTED_REMOVE_LORE)))
                .build();

        SpongeIcon icon = new SpongeIcon(display);
        icon.addListener(clickable -> {
            this.display.close(this.viewer);
            this.viewer.sendMessage(PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_BEGIN_PUBLISH_REQUEST)));

            if(this.listing instanceof BuyItNow) {
                GTSPlugin.getInstance().getMessagingService()
                        .requestBINRemoveRequest(this.listing.getID(), this.viewer.getUniqueId())
                        .thenAccept(response -> {
                            if (response.wasSuccessful()) {
                                Impactor.getInstance().getScheduler().executeSync(() -> {
                                    if (this.listing.getEntry().give(this.viewer.getUniqueId())) {
                                        this.viewer.sendMessage(PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_LISTING_RETURNED)));
                                    } else {
                                        this.viewer.sendMessage(PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_LISTING_FAIL_TO_RETURN)));

                                        BuyItNow bin = BuyItNow.builder()
                                                .from((BuyItNow) this.listing)
                                                .expiration(LocalDateTime.now())
                                                .build();

                                        // Place BIN back in storage, in a state such that it'll only be
                                        // accessible via the lister's stash
                                        GTSPlugin.getInstance().getStorage().publishListing(bin);
                                    }
                                });
                            } else {
                                this.viewer.sendMessage(service.parse(
                                        Utilities.readMessageConfigOption(MsgConfigKeys.REQUEST_FAILED),
                                        Lists.newArrayList(() -> response.getErrorCode().orElse(ErrorCodes.UNKNOWN))
                                ));
                            }
                        });
            } else {
                GTSPlugin.getInstance().getMessagingService()
                        .requestAuctionCancellation(this.listing.getID(), this.viewer.getUniqueId())
                        .thenAccept(response -> {
                            if(response.wasSuccessful()) {
                                Impactor.getInstance().getScheduler().executeSync(() -> {
                                    if(this.listing.getEntry().give(this.viewer.getUniqueId())) {
                                        this.viewer.sendMessage(PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_LISTING_RETURNED)));
                                    } else {
                                        this.viewer.sendMessage(PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_LISTING_FAIL_TO_RETURN)));

                                        // Set auction as expired, with no bids
                                        Auction auction = Auction.builder()
                                                .from((Auction) this.listing)
                                                .expiration(LocalDateTime.now())
                                                .bids(ArrayListMultimap.create())
                                                .build();

                                        // Place auction back in storage, in a state such that it'll only be
                                        // accessible via the lister's stash
                                        GTSPlugin.getInstance().getStorage().publishListing(auction);
                                    }
                                });
                            } else {
                                this.viewer.sendMessage(service.parse(
                                        Utilities.readMessageConfigOption(MsgConfigKeys.REQUEST_FAILED),
                                        Lists.newArrayList(() -> response.getErrorCode().orElse(ErrorCodes.UNKNOWN))
                                ));
                            }
                        });
            }
        });

        return icon;
    }

    private Tuple<Boolean, Boolean> getBalanceAbilities(Currency currency, double value) {
        AtomicBoolean canAfford = new AtomicBoolean();
        AtomicBoolean isExact = new AtomicBoolean();
        GTSPlugin.getInstance().as(GTSSpongePlugin.class)
                .getEconomy()
                .getOrCreateAccount(this.viewer.getUniqueId())
                .ifPresent(account -> {
                    double balance = account.getBalance(currency).doubleValue();
                    canAfford.set(balance >= value);
                    isExact.set(balance > value);
                });

        return new Tuple<>(canAfford.get(), isExact.get());
    }
}
