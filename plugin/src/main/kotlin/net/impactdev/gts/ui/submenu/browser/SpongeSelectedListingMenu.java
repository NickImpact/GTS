package net.impactdev.gts.ui.submenu.browser;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicDouble;
import net.impactdev.gts.GTSSpongePlugin;
import net.impactdev.gts.manager.SpongeListingManager;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.data.registry.GTSKeyMarker;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.api.listings.makeup.Display;
import net.impactdev.gts.api.listings.manager.ListingManager;
import net.impactdev.gts.api.listings.prices.Price;
import net.impactdev.gts.api.listings.prices.PriceManager;
import net.impactdev.gts.api.messaging.message.errors.ErrorCodes;
import net.impactdev.gts.api.messaging.message.type.listings.ClaimMessage;
import net.impactdev.gts.common.discord.DiscordNotifier;
import net.impactdev.gts.common.discord.DiscordOption;
import net.impactdev.gts.common.discord.Message;
import net.impactdev.gts.common.utils.EconomicFormatter;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;
import net.impactdev.gts.sponge.listings.SpongeAuction;
import net.impactdev.gts.sponge.listings.SpongeBuyItNow;
import net.impactdev.gts.sponge.pricing.provided.MonetaryPrice;
import net.impactdev.gts.sponge.ui.SpongeAsyncPage;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.configuration.ConfigKey;
import net.impactdev.impactor.api.gui.signs.SignQuery;
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
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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
    private boolean claim;

    public SpongeSelectedListingMenu(Player viewer, SpongeListing listing, Supplier<SpongeAsyncPage<?>> parent, boolean claim, boolean update) {
        this.viewer = viewer;
        this.listing = listing;
        this.parent = parent;
        this.claim = claim;

        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);

        boolean isLister = viewer.getUniqueId().equals(listing.getLister());

        this.display = SpongeUI.builder()
                .title(service.parse(Utilities.readMessageConfigOption(
                        this.claim ? MsgConfigKeys.UI_MENU_LISTING_SELECTED_CLAIM :
                                isLister ? MsgConfigKeys.UI_MENU_LISTING_SELECTED_LISTER :
                                this.listing instanceof BuyItNow ? MsgConfigKeys.UI_MENU_LISTING_SELECTED_OTHER :
                                        MsgConfigKeys.UI_MENU_LISTING_SELECTED_OTHER_AUCTION
                )))
                .dimension(InventoryDimension.of(9, 6))
                .build()
                .define(this.design());

        if(update) {
            Task task = Sponge.getScheduler().createTaskBuilder()
                    .execute(() -> {
                        if(Sponge.getServer().getTicksPerSecond() >= 18) {
                            this.update();
                        }
                    })
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
        builder.slots(SpongeIcon.BORDER, 19, 20, 24, 25, 40);
        builder.row(SpongeIcon.BORDER, 3);

        final Config lang = GTSPlugin.getInstance().getMsgConfig();
        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);
        Display<ItemStack> display = this.listing.getEntry().getDisplay(this.viewer.getUniqueId(), this.listing);
        ItemStack item = display.get();

        Optional<List<Text>> lore = item.get(Keys.ITEM_LORE);
        lore.ifPresent(texts -> texts.addAll(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_LISTING_DETAIL_SEPARATOR))));

        Supplier<List<Text>> append = () -> {
            List<Text> result = Lists.newArrayList();
            if(this.listing instanceof Auction) {
                Auction auction = (Auction) this.listing;
                List<String> input;
                if(auction.getBids().size() > 1) {
                    input = lang.get(MsgConfigKeys.UI_AUCTION_DETAILS_WITH_BIDS);
                } else if(auction.getBids().size() == 1) {
                    input = lang.get(MsgConfigKeys.UI_AUCTION_DETAILS_WITH_SINGLE_BID);
                } else {
                    input = lang.get(MsgConfigKeys.UI_AUCTION_DETAILS_NO_BIDS);
                }
                List<Supplier<Object>> sources = Lists.newArrayList(() -> auction);
                result.addAll(service.parse(input, sources));
            } else if(this.listing instanceof BuyItNow) {
                BuyItNow bin = (BuyItNow) this.listing;

                List<String> input = lang.get(MsgConfigKeys.UI_BIN_DETAILS);
                List<Supplier<Object>> sources = Lists.newArrayList(() -> bin);
                result.addAll(service.parse(input, sources));
            }
            return result;
        };
        List<Text> result = lore.orElse(Lists.newArrayList());
        result.addAll(append.get());
        item.offer(Keys.ITEM_LORE, result);

        SpongeIcon icon = new SpongeIcon(this.listing.getEntry()
                .getDisplay(this.viewer.getUniqueId(), this.listing)
                .get()
        );
        builder.slot(icon, 13);

        SpongeIcon back = new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.BARRIER)
                .add(Keys.DISPLAY_NAME, Utilities.PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_GENERAL_BACK), Lists.newArrayList(() -> this.viewer)))
                .build()
        );
        back.addListener(clickable -> {
            this.parent.get().open();
        });
        builder.slot(back, 38);

        if(this.claim) {
            this.createClaimerSection(builder);
        } else {
            if(this.listing.getLister().equals(this.viewer.getUniqueId())) {
                this.createRemoverSection(builder);
            } else {
                this.createSubmittersSection(builder);

            }
        }

        return builder.build();
    }

    private void createSubmittersSection(SpongeLayout.SpongeLayoutBuilder builder) {
        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);

        if(this.listing instanceof Auction) {
            Auction auction = (Auction) this.listing;
            double current = auction.hasAnyBidsPlaced() ? auction.getCurrentPrice() : auction.getStartingPrice();
            final double newBid = auction.getNextBidRequirement();

            Currency currency = GTSPlugin.getInstance().as(GTSSpongePlugin.class).getEconomy().getDefaultCurrency();
            Tuple<Boolean, Boolean> affordability = this.getBalanceAbilities(currency, auction.hasAnyBidsPlaced() ? current : newBid);

            SpongeIcon normal = new SpongeIcon(this.buildBidIcon());
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
            builder.slot(this.getBidHistory(), 42);
            if(affordability.getSecond()) {
                List<Text> lore = Lists.newArrayList();
                lore.addAll(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_ICON_PLACE_CUSTOM_BID_LORE_BASE)));

                SpongeIcon custom = new SpongeIcon(ItemStack.builder()
                        .itemType(ItemTypes.GOLD_INGOT)
                        .add(Keys.DISPLAY_NAME, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_ICON_PLACE_CUSTOM_BID_TITLE)))
                        .add(Keys.ITEM_LORE, lore)
                        .build());
                custom.addListener(clickable -> {
                    if(auction.getHighBid().map(bid -> bid.getFirst().equals(this.viewer.getUniqueId())).orElse(false)) {
                        this.viewer.sendMessage(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_AUCTIONS_ALREADY_TOP_BIDDER)));
                    } else {
                        SignQuery<Text, Player> query = SignQuery.<Text, Player>builder()
                                .position(new Vector3d(0, 1, 0))
                                .response(submission -> {
                                    try {
                                        double bid = Double.parseDouble(submission.get(0));
                                        if (bid < newBid) {
                                            Sponge.getServer().getPlayer(this.viewer.getUniqueId()).ifPresent(player -> {
                                                player.sendMessage(service.parse(
                                                        Utilities.readMessageConfigOption(MsgConfigKeys.CUSTOM_BID_INVALID),
                                                        Lists.newArrayList(() -> this.listing)
                                                ));
                                            });
                                            return false;
                                        }

                                        EconomyService economy = GTSPlugin.getInstance()
                                                .as(GTSSpongePlugin.class)
                                                .getEconomy();
                                        boolean canAfford = economy.getOrCreateAccount(this.viewer.getUniqueId())
                                                .map(account -> account.getBalance(currency).doubleValue() >= bid)
                                                .orElse(false);

                                        if(!canAfford) {
                                            this.viewer.sendMessage(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_AUCTIONS_CANT_AFFORD_BID)));
                                            return false;
                                        }

                                        SpongeListingManager manager = (SpongeListingManager) Impactor.getInstance().getRegistry().get(ListingManager.class);
                                        manager.bid(this.viewer.getUniqueId(), (SpongeAuction) auction, bid);
                                        return true;
                                    } catch (NumberFormatException e) {
                                        // TODO - Inform of invalid format
                                        return false;
                                    } catch (Exception fatal) {
                                        ExceptionWriter.write(fatal);
                                        // TODO - Inform user of the fatal error
                                        return false;
                                    }
                                })
                                .reopenOnFailure(false)
                                .text(Lists.newArrayList(
                                        Text.EMPTY,
                                        Text.of("----------------"),
                                        Text.of("Enter your bid"),
                                        Text.of("above")
                                ))
                                .build();
                        this.display.close(this.viewer);
                        query.sendTo(this.viewer);
                    }
                });

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
                        .getPriceManager(price.getClass().getAnnotation(GTSKeyMarker.class).value()[0])
                        .map(ui -> (PriceManager<?, Player>) ui)
                        .orElseThrow(() -> new IllegalStateException("Unable to find price manager for " + price.getClass().getAnnotation(GTSKeyMarker.class).value()[0]))
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
                    this.display.close(this.viewer);
                }
            });

            builder.slot(icon, 42);
        }
    }

    private void createRemoverSection(SpongeLayout.SpongeLayoutBuilder builder) {
        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);

        ItemStack display = ItemStack.builder()
                .itemType(ItemTypes.ANVIL)
                .add(Keys.DISPLAY_NAME, Utilities.PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_ICON_SELECTED_REMOVE_TITLE)))
                .add(Keys.ITEM_LORE, Utilities.PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_ICON_SELECTED_REMOVE_LORE)))
                .build();

        SpongeIcon icon = new SpongeIcon(display);
        icon.addListener(clickable -> {
            this.display.close(this.viewer);
            this.viewer.sendMessage(Utilities.PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_BEGIN_PROCESSING_REQUEST)));

            if(this.listing instanceof BuyItNow) {
                GTSPlugin.getInstance().getMessagingService()
                        .requestBINRemoveRequest(this.listing.getID(), this.viewer.getUniqueId())
                        .thenAccept(response -> {
                            if (response.wasSuccessful()) {
                                Impactor.getInstance().getScheduler().executeSync(() -> {
                                    if(this.claim) {
                                        if(((BuyItNow) this.listing).getPrice().reward(this.viewer.getUniqueId())) {
                                            service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_ITEM_CLAIMED),
                                                    Lists.newArrayList(() -> ((BuyItNow) this.listing).getPrice().getText())
                                            );

                                            Message message = SpongeListingManager.notifier.forgeMessage(
                                                    DiscordOption.fetch(DiscordOption.Options.Remove),
                                                    MsgConfigKeys.DISCORD_REMOVAL_TEMPLATE,
                                                    this.listing
                                            );
                                            SpongeListingManager.notifier.sendMessage(message);
                                        }
                                    } else {
                                        if (this.listing.getEntry().give(this.viewer.getUniqueId())) {
                                            this.viewer.sendMessage(Utilities.PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_LISTING_RETURNED)));

                                            Message message = SpongeListingManager.notifier.forgeMessage(
                                                    DiscordOption.fetch(DiscordOption.Options.Remove),
                                                    MsgConfigKeys.DISCORD_REMOVAL_TEMPLATE,
                                                    this.listing
                                            );
                                            SpongeListingManager.notifier.sendMessage(message);
                                        } else {
                                            this.viewer.sendMessage(Utilities.PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_LISTING_FAIL_TO_RETURN)));

                                            BuyItNow bin = BuyItNow.builder()
                                                    .from((BuyItNow) this.listing)
                                                    .expiration(LocalDateTime.now())
                                                    .build();

                                            // Place BIN back in storage, in a state such that it'll only be
                                            // accessible via the lister's stash
                                            GTSPlugin.getInstance().getStorage().publishListing(bin);
                                        }
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
                            if (response.wasSuccessful()) {
                                Impactor.getInstance().getScheduler().executeSync(() -> {
                                    if (this.listing.getEntry().give(this.viewer.getUniqueId())) {
                                        this.viewer.sendMessage(Utilities.PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_LISTING_RETURNED)));
                                        Message message = SpongeListingManager.notifier.forgeMessage(
                                                DiscordOption.fetch(DiscordOption.Options.Remove),
                                                MsgConfigKeys.DISCORD_REMOVAL_TEMPLATE,
                                                this.listing
                                        );
                                        SpongeListingManager.notifier.sendMessage(message);
                                    } else {
                                        this.viewer.sendMessage(Utilities.PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_LISTING_FAIL_TO_RETURN)));

                                        // Set auction as expired, with no bids
                                        Auction fallback = Auction.builder()
                                                .from((Auction) this.listing)
                                                .expiration(LocalDateTime.now())
                                                .bids(ArrayListMultimap.create())
                                                .build();

                                        // Place auction back in storage, in a state such that it'll only be
                                        // accessible via the lister's stash
                                        GTSPlugin.getInstance().getStorage().publishListing(fallback);
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

        if(this.listing instanceof BuyItNow) {
            builder.slot(icon, 42);
        } else {
            builder.slot(icon, 41);
            builder.slot(this.getBidHistory(), 43);
        }
    }

    private void createClaimerSection(SpongeLayout.SpongeLayoutBuilder builder) {
        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);
        List<Supplier<Object>> sources = Lists.newArrayList();

        boolean lister = this.listing.getLister().equals(this.viewer.getUniqueId());
        if(lister) {
            if(this.listing instanceof BuyItNow) {
                if(((BuyItNow) this.listing).isPurchased()) {
                    sources.add(() -> ((BuyItNow) this.listing).getPrice().getText());
                } else {
                    sources.add(() -> this.listing.getEntry().getName());
                }
            } else {
                if(((Auction) this.listing).hasAnyBidsPlaced()) {
                    sources.add(() -> new MonetaryPrice(((Auction) this.listing).getHighBid().get().getSecond().getAmount()).getText());
                } else {
                    sources.add(() -> this.listing.getEntry().getName());
                }
            }
        } else {
            if(this.listing instanceof Auction) {
                Auction auction = (Auction) this.listing;
                if(auction.getHighBid().get().getFirst().equals(this.viewer.getUniqueId())) {
                    sources.add(() -> this.listing.getEntry().getName());
                } else {
                    double amount = auction.getCurrentBid(this.viewer.getUniqueId()).get().getAmount();
                    sources.add(() -> new MonetaryPrice(amount).getText());
                }
            } else {
                BuyItNow bin = (BuyItNow) this.listing;
                if(bin.stashedForPurchaser()) {
                    sources.add(() -> this.listing.getEntry().getName());
                }
            }
        }

        SpongeIcon collect = new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.CHEST_MINECART)
                .add(Keys.DISPLAY_NAME, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_ICON_SELECTED_CLAIM_TITLE), sources))
                .add(Keys.ITEM_LORE, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_ICON_SELECTED_CLAIM_LORE)))
                .build()
        );
        collect.addListener(clickable -> {
            this.display.close(this.viewer);

            if(lister) {
                if(this.listing instanceof BuyItNow) {
                    GTSPlugin.getInstance().getMessagingService()
                            .requestClaim(this.listing.getID(), this.viewer.getUniqueId(), null, false)
                            .thenAccept(response -> {
                                if(response.wasSuccessful()) {
                                    Impactor.getInstance().getScheduler().executeSync(() -> {
                                        boolean result;
                                        if(((BuyItNow) this.listing).isPurchased()) {
                                            result = ((BuyItNow) this.listing).getPrice().reward(this.viewer.getUniqueId());
                                        } else {
                                            result = this.listing.getEntry().give(this.viewer.getUniqueId());
                                        }

                                        if (result) {
                                            this.viewer.sendMessage(Utilities.PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_ITEM_CLAIMED), sources));
                                        } else {
                                            this.viewer.sendMessage(Utilities.PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_LISTING_FAIL_TO_RETURN)));

                                            BuyItNow.BuyItNowBuilder b = BuyItNow.builder()
                                                    .from((BuyItNow) this.listing)
                                                    .expiration(LocalDateTime.now());

                                            BuyItNow bin = b.build();

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
                    Auction auction = (Auction) this.listing;
                    if(auction.hasAnyBidsPlaced()) {
                        GTSPlugin.getInstance().getMessagingService()
                                .requestClaim(this.listing.getID(), this.viewer.getUniqueId(), null, true)
                                .thenAccept(r -> {
                                    ClaimMessage.Response.AuctionResponse response = (ClaimMessage.Response.AuctionResponse) r;

                                    if (response.wasSuccessful()) {
                                        Impactor.getInstance().getScheduler().executeSync(() -> {
                                            // This is the user attempting to receive the money for the auction

                                            MonetaryPrice wrapper = new MonetaryPrice(auction.getHighBid().get().getSecond().getAmount());
                                            if (wrapper.reward(this.viewer.getUniqueId())) {
                                                this.viewer.sendMessage(service.parse(
                                                        Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_ITEM_CLAIMED),
                                                        Lists.newArrayList(wrapper::getText)
                                                ));
                                            } else {
                                                this.viewer.sendMessage(Utilities.PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_LISTING_FAIL_TO_RETURN)));

                                                GTSPlugin.getInstance().getStorage().appendOldClaimStatus(
                                                        auction.getID(),
                                                        response.hasListerClaimed(),
                                                        response.hasWinnerClaimed(),
                                                        response.getAllOtherClaimers()
                                                );
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
                                    if (response.wasSuccessful()) {
                                        Impactor.getInstance().getScheduler().executeSync(() -> {
                                            if (this.listing.getEntry().give(this.viewer.getUniqueId())) {
                                                this.viewer.sendMessage(Utilities.PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_ITEM_CLAIMED), sources));
                                            } else {
                                                this.viewer.sendMessage(Utilities.PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_LISTING_FAIL_TO_RETURN)));

                                                // Set auction as expired, with no bids
                                                Auction fallback = Auction.builder()
                                                        .from((Auction) this.listing)
                                                        .expiration(LocalDateTime.now())
                                                        .bids(ArrayListMultimap.create())
                                                        .build();

                                                // Place auction back in storage, in a state such that it'll only be
                                                // accessible via the lister's stash
                                                GTSPlugin.getInstance().getStorage().publishListing(fallback);
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
                }
            } else {
                if(this.listing instanceof Auction) {
                    GTSPlugin.getInstance().getMessagingService()
                            .requestClaim(this.listing.getID(), this.viewer.getUniqueId(), null, true)
                            .thenAccept(r -> {
                                ClaimMessage.Response.AuctionResponse response = (ClaimMessage.Response.AuctionResponse) r;

                                if (response.wasSuccessful()) {
                                    Impactor.getInstance().getScheduler().executeSync(() -> {
                                        // This is the user attempting to receive the money for the auction

                                        Auction auction = (Auction) this.listing;

                                        if (auction.getHighBid().get().getFirst().equals(this.viewer.getUniqueId())) {
                                            if (auction.getEntry().give(this.viewer.getUniqueId())) {
                                                this.viewer.sendMessage(service.parse(
                                                        Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_ITEM_CLAIMED),
                                                        Lists.newArrayList(() -> auction.getEntry().getName())
                                                ));
                                            } else {
                                                this.viewer.sendMessage(Utilities.PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_LISTING_FAIL_TO_RETURN)));

                                                GTSPlugin.getInstance().getStorage().appendOldClaimStatus(
                                                        auction.getID(),
                                                        response.hasListerClaimed(),
                                                        response.hasWinnerClaimed(),
                                                        response.getAllOtherClaimers()
                                                );
                                            }
                                        } else {
                                            double bid = auction.getCurrentBid(this.viewer.getUniqueId()).get().getAmount();
                                            MonetaryPrice price = new MonetaryPrice(bid);
                                            if (price.reward(this.viewer.getUniqueId())) {
                                                this.viewer.sendMessage(service.parse(
                                                        Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_ITEM_CLAIMED),
                                                        Lists.newArrayList(price::getText)
                                                ));
                                            } else {
                                                this.viewer.sendMessage(Utilities.PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_LISTING_FAIL_TO_RETURN)));

                                                GTSPlugin.getInstance().getStorage().appendOldClaimStatus(
                                                        auction.getID(),
                                                        response.hasListerClaimed(),
                                                        response.hasWinnerClaimed(),
                                                        response.getAllOtherClaimers()
                                                );
                                            }
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
                            .requestClaim(this.listing.getID(), this.viewer.getUniqueId(), null, false)
                            .thenAccept(r -> {
                                if(r.wasSuccessful()) {
                                    BuyItNow bin = (BuyItNow) this.listing;
                                    if (bin.stashedForPurchaser()) {
                                        Impactor.getInstance().getScheduler().executeSync(() -> {
                                            if (!this.listing.getEntry().give(this.viewer.getUniqueId())) {
                                                // Re-append data as our claim request will have deleted it
                                                GTSPlugin.getInstance().getStorage().publishListing(BuyItNow.builder()
                                                        .from(bin)
                                                        .build()
                                                );
                                                this.viewer.sendMessage(service.parse(
                                                        Utilities.readMessageConfigOption(MsgConfigKeys.REQUEST_FAILED),
                                                        Lists.newArrayList(() -> ErrorCodes.FAILED_TO_GIVE)
                                                ));
                                            } else {
                                                this.viewer.sendMessage(service.parse(
                                                        Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_ITEM_CLAIMED),
                                                        Lists.newArrayList(this.listing.getEntry()::getName)
                                                ));
                                            }
                                        });
                                    }
                                }
                            });
                }
            }
        });

        if(this.listing instanceof BuyItNow) {
            builder.slot(collect, 42);
        } else {
            builder.slot(collect, 41);
            builder.slot(this.getBidHistory(), 43);
        }
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

    private ItemStack buildBidIcon() {
        final Config lang = GTSPlugin.getInstance().getMsgConfig();
        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);
        Auction auction = (Auction) this.listing;
        List<Supplier<Object>> sources = Lists.newArrayList();
        sources.add(() -> auction);

        AtomicDouble previous = new AtomicDouble(0);
        List<Text> lore = Lists.newArrayList();
        ConfigKey<List<String>> base = MsgConfigKeys.UI_ICON_PLACE_BID_LORE;
        if(auction.getBids().containsKey(this.viewer.getUniqueId())) {
            base = MsgConfigKeys.UI_ICON_PLACE_BID_WITH_USER_BID_PLACED_LORE;
            sources.add(() -> (previous.addAndGet(auction.getCurrentBid(this.viewer.getUniqueId()).map(Auction.Bid::getAmount).orElse(0D))));
        }

        lore.addAll(service.parse(lang.get(base), sources));

        double required = auction.getNextBidRequirement();
        AtomicBoolean canAfford = new AtomicBoolean(false);
        EconomyService economy = GTSPlugin.getInstance().as(GTSSpongePlugin.class).getEconomy();
        economy.getOrCreateAccount(this.viewer.getUniqueId())
                .ifPresent(account -> {
                    if(account.getBalance(economy.getDefaultCurrency()).doubleValue() >= required) {
                        canAfford.set(true);
                    }
                });

        ConfigKey<List<String>> append = canAfford.get() ? MsgConfigKeys.UI_ICON_PLACE_BID_CAN_AFFORD : MsgConfigKeys.UI_ICON_PLACE_BID_CANT_AFFORD;
        if(auction.getHighBid().map(t -> t.getFirst().equals(this.viewer.getUniqueId())).orElse(false)) {
            append = MsgConfigKeys.UI_ICON_PLACE_BID_IS_TOP_BID;
        }

        lore.addAll(service.parse(lang.get(append), sources));

        return ItemStack.builder()
                .itemType(ItemTypes.WRITABLE_BOOK)
                .add(Keys.DISPLAY_NAME, service.parse(lang.get(MsgConfigKeys.UI_ICON_PLACE_BID_TITLE)))
                .add(Keys.ITEM_LORE, lore)
                .build();
    }

    private SpongeIcon getBidHistory() {
        final Config lang = GTSPlugin.getInstance().getMsgConfig();
        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);
        Auction auction = (Auction) this.listing;
        List<Supplier<Object>> sources = Lists.newArrayList(() -> auction);

        List<Text> lore = Lists.newArrayList();
        lore.addAll(service.parse(lang.get(MsgConfigKeys.UI_ICON_BID_HISTORY_BASE_INFO), sources));

        final AtomicDouble prior = new AtomicDouble(Double.MAX_VALUE);
        if(auction.getBids().size() > 0) {
            for (int i = 0; i < 5 && i < auction.getBids().size(); i++) {
                Map.Entry<UUID, Auction.Bid> entry = auction.getBids().entries().stream()
                        .filter(bid -> bid.getValue().getAmount() < prior.get())
                        .max(Comparator.comparing(value -> value.getValue().getAmount()))
                        .get();

                prior.set(entry.getValue().getAmount());

                Auction.BidContext context = new Auction.BidContext(entry.getKey(), entry.getValue());
                lore.add(service.parse(lang.get(MsgConfigKeys.UI_ICON_BID_HISTORY_SEPARATOR)));
                lore.addAll(service.parse(lang.get(MsgConfigKeys.UI_ICON_BID_HISTORY_BID_INFO), Lists.newArrayList(() -> context)));
            }
        } else {
            if(!this.listing.getLister().equals(this.viewer.getUniqueId())) {
                lore.addAll(service.parse(lang.get(MsgConfigKeys.UI_ICON_BID_HISTORY_NO_BIDS)));
            }
        }

        ItemStack display = ItemStack.builder()
                .itemType(ItemTypes.PAPER)
                .add(Keys.DISPLAY_NAME, service.parse(lang.get(MsgConfigKeys.UI_ICON_BID_HISTORY_TITLE)))
                .add(Keys.ITEM_LORE, lore)
                .build();

        return new SpongeIcon(display);
    }
}
