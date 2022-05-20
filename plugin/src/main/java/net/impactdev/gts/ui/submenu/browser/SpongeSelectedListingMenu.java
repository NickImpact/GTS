package net.impactdev.gts.ui.submenu.browser;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicDouble;
import net.impactdev.gts.SpongeGTSPlugin;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.communication.message.errors.ErrorCode;
import net.impactdev.gts.api.ui.GTSMenu;
import net.impactdev.gts.manager.SpongeListingManager;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.data.registry.GTSKeyMarker;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.api.listings.makeup.Display;
import net.impactdev.gts.api.listings.manager.ListingManager;
import net.impactdev.gts.api.listings.prices.Price;
import net.impactdev.gts.api.listings.prices.PriceManager;
import net.impactdev.gts.api.communication.message.errors.ErrorCodes;
import net.impactdev.gts.api.communication.message.type.listings.ClaimMessage;
import net.impactdev.gts.common.discord.DiscordOption;
import net.impactdev.gts.common.discord.Message;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;
import net.impactdev.gts.sponge.listings.SpongeAuction;
import net.impactdev.gts.sponge.listings.SpongeBuyItNow;
import net.impactdev.gts.sponge.listings.SpongeListing;
import net.impactdev.gts.sponge.pricing.provided.MonetaryPrice;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.gts.sponge.utils.items.ProvidedIcons;
import net.impactdev.gts.ui.SpongeMainMenu;
import net.impactdev.gts.ui.submenu.SpongeListingMenu;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.configuration.ConfigKey;
import net.impactdev.impactor.api.placeholders.PlaceholderSources;
import net.impactdev.impactor.api.platform.players.PlatformPlayer;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.impactor.api.ui.containers.ImpactorUI;
import net.impactdev.impactor.api.ui.containers.detail.RefreshDetail;
import net.impactdev.impactor.api.ui.containers.detail.RefreshTypes;
import net.impactdev.impactor.api.ui.containers.icons.DisplayProvider;
import net.impactdev.impactor.api.ui.containers.icons.Icon;
import net.impactdev.impactor.api.ui.containers.layouts.Layout;
import net.impactdev.impactor.api.utilities.mappings.Tuple;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class SpongeSelectedListingMenu {

    private static final MessageService service = Impactor.getInstance().getRegistry().get(MessageService.class);

    private final ImpactorUI display;
    private final PlatformPlayer viewer;

    private final Listing listing;

    private final Supplier<GTSMenu> parent;
    private final boolean claim;

    public SpongeSelectedListingMenu(ServerPlayer viewer, Listing listing, Supplier<GTSMenu> parent, boolean claim, boolean update) {
        this.viewer = PlatformPlayer.from(viewer);
        this.listing = listing;
        this.parent = parent;
        this.claim = claim;

        boolean isLister = viewer.uniqueId().equals(listing.getLister());

        Optional<ScheduledTask> task = Optional.of(update)
                .filter(t -> t)
                .map(ignore -> Task.builder()
                        .execute(() -> {
                            if(Sponge.server().ticksPerSecond() >= 18) {
                                this.update();
                            }
                        })
                        .interval(1, TimeUnit.SECONDS)
                        .plugin(GTSPlugin.instance().as(SpongeGTSPlugin.class).container())
                        .build()
                )
                .map(t -> Sponge.server().scheduler().submit(t));

        this.display = ImpactorUI.builder()
                .provider(Key.key("gts", "selected-listing"))
                .title(service.parse(Utilities.readMessageConfigOption(
                        this.claim ? MsgConfigKeys.UI_MENU_LISTING_SELECTED_CLAIM :
                                isLister ? MsgConfigKeys.UI_MENU_LISTING_SELECTED_LISTER :
                                this.listing instanceof BuyItNow ? MsgConfigKeys.UI_MENU_LISTING_SELECTED_OTHER :
                                        MsgConfigKeys.UI_MENU_LISTING_SELECTED_OTHER_AUCTION
                )))
                .layout(this.design())
                .onClose(context -> {
                    task.ifPresent(ScheduledTask::cancel);
                    return true;
                })
                .build();
    }

    public void open() {
        this.display.open(PlatformPlayer.from(this.viewer));
    }

    private void update() {
        RefreshDetail detail = RefreshDetail.create(RefreshTypes.SLOT_INDEX);
        detail.context().put(Integer.class, 13);
        this.display.refresh(detail);
    }

    private Layout design() {
        Layout.LayoutBuilder builder = Layout.builder();
        Icon<ItemStack> colored = Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(ItemStack.builder()
                        .itemType(ItemTypes.LIGHT_BLUE_STAINED_GLASS_PANE)
                        .add(Keys.CUSTOM_NAME, Component.empty())
                        .build()
                ))
                .build();

        builder.border(ProvidedIcons.BORDER);
        builder.slots(colored, 3, 4, 5, 10, 11, 12, 14, 15, 16, 21, 22, 23);
        builder.slots(ProvidedIcons.BORDER, 19, 20, 24, 25, 40);
        builder.row(ProvidedIcons.BORDER, 3);

        Icon<ItemStack> icon = Icon.builder(ItemStack.class)
                .display(() -> {
                    final Config lang = GTSPlugin.instance().configuration().language();
                    final MessageService service = Impactor.getInstance().getRegistry().get(MessageService.class);
                    Display<ItemStack> display = ((SpongeListing)this.listing).getEntry().getDisplay(this.viewer.uuid());
                    ItemStack item = display.get();

                    Optional<List<Component>> lore = item.get(Keys.LORE);
                    lore.ifPresent(texts -> texts.addAll(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_LISTING_DETAIL_SEPARATOR))));

                    Supplier<List<Component>> append = () -> {
                        List<Component> result = Lists.newArrayList();
                        if (this.listing instanceof Auction) {
                            Auction auction = (Auction) listing;
                            List<String> input;
                            if (auction.getBids().size() > 1) {
                                input = lang.get(MsgConfigKeys.UI_AUCTION_DETAILS_WITH_BIDS);
                            } else if (auction.getBids().size() == 1) {
                                input = lang.get(MsgConfigKeys.UI_AUCTION_DETAILS_WITH_SINGLE_BID);
                            } else {
                                input = lang.get(MsgConfigKeys.UI_AUCTION_DETAILS_NO_BIDS);
                            }

                            PlaceholderSources sources = PlaceholderSources.builder()
                                    .append(Auction.class, () -> auction)
                                    .build();

                            result.addAll(service.parse(input, sources));
                        } else if (listing instanceof BuyItNow) {
                            BuyItNow bin = (BuyItNow) listing;

                            List<String> input = lang.get(MsgConfigKeys.UI_BIN_DETAILS);
                            PlaceholderSources sources = PlaceholderSources.builder()
                                    .append(BuyItNow.class, () -> bin)
                                    .build();
                            result.addAll(service.parse(input, sources));
                        }
                        return result;
                    };
                    List<Component> result = lore.orElse(Lists.newArrayList());
                    result.addAll(append.get());
                    item.offer(Keys.LORE, result);

                    return item;
                })
                .build();
        builder.slot(icon, 13);

        ItemStack b = ItemStack.builder()
                .itemType(ItemTypes.BARRIER)
                .add(Keys.CUSTOM_NAME, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_GENERAL_BACK)))
                .build();

        Icon<ItemStack> back = Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(b))
                .listener(context -> {
                    this.parent.get().open();
                    return false;
                })
                .build();
        builder.slot(back, 38);

        if(this.claim) {
            this.createClaimerSection(builder);
        } else {
            if(this.listing.getLister().equals(this.viewer.uuid())) {
                this.createRemoverSection(builder);
            } else {
                this.createSubmittersSection(builder);
            }
        }

        return builder.build();
    }

    private void createSubmittersSection(Layout.LayoutBuilder builder) {
        if(this.listing instanceof Auction) {
            Auction auction = (Auction) this.listing;
            double current = auction.hasAnyBidsPlaced() ? auction.getCurrentPrice() : auction.getStartingPrice();
            final double newBid = auction.getNextBidRequirement();

            Currency currency = Sponge.server().serviceProvider().economyService()
                    .orElseThrow(IllegalStateException::new)
                    .defaultCurrency();
            Tuple<Boolean, Boolean> affordability = this.getBalanceAbilities(currency, auction.hasAnyBidsPlaced() ? current : newBid);

            Icon<ItemStack> normal = Icon.builder(ItemStack.class)
                    .display(new DisplayProvider.Constant<>(this.buildBidIcon()))
                    .listener(context -> {
                        if(auction.getHighBid().map(bid -> bid.getFirst().equals(this.viewer.uuid())).orElse(false)) {
                            this.player().sendMessage(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_AUCTIONS_ALREADY_TOP_BIDDER)));
                        } else {
                            if (affordability.getFirst()) {
                                this.display.close(this.viewer);
                                SpongeListingManager manager = (SpongeListingManager) Impactor.getInstance().getRegistry().get(ListingManager.class);
                                manager.bid(this.viewer.uuid(), (SpongeAuction) auction, newBid);
                            } else {
                                this.player().sendMessage(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_AUCTIONS_CANT_AFFORD_BID)));
                            }
                        }

                        return false;
                    })
                    .build();
            builder.slot(normal, 41);
            builder.slot(this.getBidHistory(), 42);

            if(affordability.getSecond()) {
                List<Component> lore = Lists.newArrayList();
                lore.addAll(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_ICON_PLACE_CUSTOM_BID_LORE_BASE)));

                Icon<ItemStack> custom = Icon.builder(ItemStack.class)
                        .display(new DisplayProvider.Constant<>(ItemStack.builder()
                                .itemType(ItemTypes.GOLD_INGOT)
                                .add(Keys.CUSTOM_NAME, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_ICON_PLACE_CUSTOM_BID_TITLE)))
                                .add(Keys.LORE, lore)
                                .build()
                        ))
                        .listener(context -> {
                            if(auction.getHighBid().map(bid -> bid.getFirst().equals(this.viewer.uuid())).orElse(false)) {
                                this.player().sendMessage(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_AUCTIONS_ALREADY_TOP_BIDDER)));
                            } else {
//                                SignQuery<Text, Player> query = SignQuery.<Text, Player>builder()
//                                        .position(new Vector3d(0, 1, 0))
//                                        .response(submission -> {
//                                            try {
//                                                double bid = Double.parseDouble(submission.get(0));
//                                                if (bid < newBid) {
//                                                    Sponge.getServer().getPlayer(this.viewer.getUniqueId()).ifPresent(player -> {
//                                                        player.sendMessage(service.parse(
//                                                                Utilities.readMessageConfigOption(MsgConfigKeys.CUSTOM_BID_INVALID),
//                                                                Lists.newArrayList(() -> this.listing)
//                                                        ));
//                                                    });
//                                                    return false;
//                                                }
//
//                                                EconomyService economy = GTSPlugin.getInstance()
//                                                        .as(GTSSpongePlugin.class)
//                                                        .getEconomy();
//                                                boolean canAfford = economy.getOrCreateAccount(this.viewer.getUniqueId())
//                                                        .map(account -> account.getBalance(currency).doubleValue() >= bid)
//                                                        .orElse(false);
//
//                                                if(!canAfford) {
//                                                    this.viewer.sendMessage(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_AUCTIONS_CANT_AFFORD_BID)));
//                                                    return false;
//                                                }
//
//                                                SpongeListingManager manager = (SpongeListingManager) Impactor.getInstance().getRegistry().get(ListingManager.class);
//                                                manager.bid(this.viewer.getUniqueId(), (SpongeAuction) auction, bid);
//                                                return true;
//                                            } catch (NumberFormatException e) {
//                                                // TODO - Inform of invalid format
//                                                return false;
//                                            } catch (Exception fatal) {
//                                                ExceptionWriter.write(fatal);
//                                                // TODO - Inform user of the fatal error
//                                                return false;
//                                            }
//                                        })
//                                        .reopenOnFailure(false)
//                                        .text(Lists.newArrayList(
//                                                Text.EMPTY,
//                                                Text.of("----------------"),
//                                                Text.of("Enter your bid"),
//                                                Text.of("above")
//                                        ))
//                                        .build();
//                                this.display.close(this.viewer);
//                                query.sendTo(this.viewer);
                            }

                            return false;
                        })
                        .build();
                builder.slot(custom, 43);
            }
        } else {
            ItemStack display = ItemStack.builder()
                    .itemType(ItemTypes.LIME_CONCRETE)
                    .add(Keys.CUSTOM_NAME, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.CONFIRM_PURCHASE)))
                    .build();
            Icon<ItemStack> icon = Icon.builder(ItemStack.class)
                            .display(new DisplayProvider.Constant<>(display))
                            .listener(context -> {
                                BuyItNow bin = (BuyItNow) this.listing;

                                Price<?, ?, ?> price = bin.getPrice();
                                Optional<PriceManager.PriceSelectorUI<ImpactorUI>> selector = GTSService.getInstance().getGTSComponentManager()
                                        .getPriceManager(price.getClass().getAnnotation(GTSKeyMarker.class).value()[0])
                                        .map(ui -> (PriceManager<?>) ui)
                                        .orElseThrow(() -> new IllegalStateException("Unable to find price manager for " + price.getClass().getAnnotation(GTSKeyMarker.class).value()[0]))
                                        .getSelector(this.viewer, price, source -> {
                                            SpongeListingManager manager = (SpongeListingManager) Impactor.getInstance().getRegistry().get(ListingManager.class);
                                            manager.purchase(this.viewer.uuid(), (SpongeBuyItNow) bin, source);
                                        });
                                if(selector.isPresent()) {
                                    selector.get().getDisplay().open(this.viewer);
                                } else {
                                    // For prices that have no need for a source
                                    SpongeListingManager manager = (SpongeListingManager) Impactor.getInstance().getRegistry().get(ListingManager.class);
                                    manager.purchase(this.viewer.uuid(), (SpongeBuyItNow) bin, null);
                                    this.display.close(this.viewer);
                                }

                                return false;
                            })
                            .build();

            builder.slot(icon, 42);
        }
    }

    private void createRemoverSection(Layout.LayoutBuilder builder) {
        final MessageService service = Impactor.getInstance().getRegistry().get(MessageService.class);

        ItemStack display = ItemStack.builder()
                .itemType(ItemTypes.ANVIL)
                .add(Keys.CUSTOM_NAME, Utilities.PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_ICON_SELECTED_REMOVE_TITLE)))
                .add(Keys.LORE, Utilities.PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_ICON_SELECTED_REMOVE_LORE)))
                .build();

        Icon<ItemStack> icon = Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(display))
                .listener(context -> {
                    this.display.close(this.viewer);
                    this.player().sendMessage(Utilities.PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_BEGIN_PROCESSING_REQUEST)));

                    if(this.listing instanceof BuyItNow) {
                        GTSPlugin.instance().messagingService()
                                .requestBINRemoveRequest(this.listing.getID(), this.viewer.uuid())
                                .thenAccept(response -> {
                                    if (response.wasSuccessful()) {
                                        Impactor.getInstance().getScheduler().executeSync(() -> {
                                            if(this.claim) {
                                                if(((BuyItNow) this.listing).getPrice().reward(this.viewer.uuid())) {
                                                    PlaceholderSources sources = PlaceholderSources.builder()
                                                            .append(Component.class, () -> ((BuyItNow) this.listing).getPrice().getText())
                                                            .build();
                                                    service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_ITEM_CLAIMED), sources);

                                                    SpongeListingManager.notifier.forgeAndSend(
                                                            DiscordOption.fetch(DiscordOption.Options.Remove),
                                                            MsgConfigKeys.DISCORD_REMOVAL_TEMPLATE,
                                                            this.listing,
                                                            PlaceholderSources.builder()
                                                                    .append(Listing.class, () -> listing)
                                                                    .build()
                                                    );
                                                }
                                            } else {
                                                if (this.listing.getEntry().give(this.viewer.uuid())) {
                                                    this.player().sendMessage(Utilities.PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_LISTING_RETURNED)));

                                                    SpongeListingManager.notifier.forgeAndSend(
                                                            DiscordOption.fetch(DiscordOption.Options.Remove),
                                                            MsgConfigKeys.DISCORD_REMOVAL_TEMPLATE,
                                                            this.listing,
                                                            PlaceholderSources.builder()
                                                                    .append(Listing.class, () -> listing)
                                                                    .build()
                                                    );
                                                } else {
                                                    this.player().sendMessage(Utilities.PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_LISTING_FAIL_TO_RETURN)));

                                                    BuyItNow bin = BuyItNow.builder()
                                                            .from((BuyItNow) this.listing)
                                                            .expiration(LocalDateTime.now())
                                                            .build();

                                                    // Place BIN back in storage, in a state such that it'll only be
                                                    // accessible via the lister's stash
                                                    GTSPlugin.instance().storage().publishListing(bin);
                                                }
                                            }
                                        });
                                    } else {
                                        PlaceholderSources sources = PlaceholderSources.builder()
                                                .append(ErrorCode.class, () -> response.getErrorCode().orElse(ErrorCodes.UNKNOWN))
                                                .build();
                                        this.player().sendMessage(service.parse(
                                                Utilities.readMessageConfigOption(MsgConfigKeys.REQUEST_FAILED),
                                                sources
                                        ));
                                    }
                                });
                    } else {
                        GTSPlugin.instance().messagingService()
                                .requestAuctionCancellation(this.listing.getID(), this.viewer.uuid())
                                .thenAccept(response -> {
                                    if (response.wasSuccessful()) {
                                        Impactor.getInstance().getScheduler().executeSync(() -> {
                                            if (this.listing.getEntry().give(this.viewer.uuid())) {
                                                this.player().sendMessage(Utilities.PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_LISTING_RETURNED)));

                                                SpongeListingManager.notifier.forgeAndSend(
                                                        DiscordOption.fetch(DiscordOption.Options.Remove),
                                                        MsgConfigKeys.DISCORD_REMOVAL_TEMPLATE,
                                                        this.listing,
                                                        PlaceholderSources.builder()
                                                                .append(Listing.class, () -> listing)
                                                                .build()
                                                );
                                            } else {
                                                this.player().sendMessage(Utilities.PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_LISTING_FAIL_TO_RETURN)));

                                                // Set auction as expired, with no bids
                                                Auction fallback = Auction.builder()
                                                        .from((Auction) this.listing)
                                                        .expiration(LocalDateTime.now())
                                                        .bids(ArrayListMultimap.create())
                                                        .build();

                                                // Place auction back in storage, in a state such that it'll only be
                                                // accessible via the lister's stash
                                                GTSPlugin.instance().storage().publishListing(fallback);
                                            }
                                        });
                                    } else {
                                        PlaceholderSources sources = PlaceholderSources.builder()
                                                .append(ErrorCode.class, () -> response.getErrorCode().orElse(ErrorCodes.UNKNOWN))
                                                .build();
                                        this.player().sendMessage(service.parse(
                                                Utilities.readMessageConfigOption(MsgConfigKeys.REQUEST_FAILED),
                                                sources
                                        ));
                                    }
                                });
                    }

                    return false;
                })
                .build();

        if(this.listing instanceof BuyItNow) {
            builder.slot(icon, 42);
        } else {
            builder.slot(icon, 41);
            builder.slot(this.getBidHistory(), 43);
        }
    }

    private void createClaimerSection(Layout.LayoutBuilder builder) {
        final MessageService service = Impactor.getInstance().getRegistry().get(MessageService.class);
        PlaceholderSources sources = PlaceholderSources.empty();

        boolean lister = this.listing.getLister().equals(this.viewer.uuid());
        if(lister) {
            if(this.listing instanceof BuyItNow) {
                if(((BuyItNow) this.listing).isPurchased()) {
                    sources.append(Component.class, () -> ((BuyItNow) this.listing).getPrice().getText());
                } else {
                    sources.append(Component.class, () -> this.listing.getEntry().getName());
                }
            } else {
                if(((Auction) this.listing).hasAnyBidsPlaced()) {
                    sources.append(Component.class, () -> new MonetaryPrice(((Auction) this.listing).getHighBid().get().getSecond().getAmount()).getText());
                } else {
                    sources.append(Component.class, () -> this.listing.getEntry().getName());
                }
            }
        } else {
            if(this.listing instanceof Auction) {
                Auction auction = (Auction) this.listing;
                if(auction.getHighBid().get().getFirst().equals(this.viewer.uuid())) {
                    sources.append(Component.class, () -> this.listing.getEntry().getName());
                } else {
                    double amount = auction.getCurrentBid(this.viewer.uuid()).get().getAmount();
                    sources.append(Component.class, () -> new MonetaryPrice(amount).getText());
                }
            } else {
                BuyItNow bin = (BuyItNow) this.listing;
                if(bin.stashedForPurchaser()) {
                    sources.append(Component.class, () -> this.listing.getEntry().getName());
                }
            }
        }

        Icon<ItemStack> collect = Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(ItemStack.builder()
                        .itemType(ItemTypes.CHEST_MINECART)
                        .add(Keys.CUSTOM_NAME, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_ICON_SELECTED_CLAIM_TITLE), sources))
                        .add(Keys.LORE, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_ICON_SELECTED_CLAIM_LORE)))
                        .build()
                ))
                .listener(context -> {
                    this.display.close(this.viewer);

                    if(lister) {
                        if(this.listing instanceof BuyItNow) {
                            GTSPlugin.instance().messagingService()
                                    .requestClaim(this.listing.getID(), this.viewer.uuid(), null, false)
                                    .thenAccept(response -> {
                                        if(response.wasSuccessful()) {
                                            Impactor.getInstance().getScheduler().executeSync(() -> {
                                                boolean result;
                                                if(((BuyItNow) this.listing).isPurchased()) {
                                                    result = ((BuyItNow) this.listing).getPrice().reward(this.viewer.uuid());
                                                } else {
                                                    result = this.listing.getEntry().give(this.viewer.uuid());
                                                }

                                                if (result) {
                                                    this.player().sendMessage(Utilities.PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_ITEM_CLAIMED), sources));
                                                } else {
                                                    this.player().sendMessage(Utilities.PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_LISTING_FAIL_TO_RETURN)));

                                                    BuyItNow.BuyItNowBuilder b = BuyItNow.builder()
                                                            .from((BuyItNow) this.listing)
                                                            .expiration(LocalDateTime.now());

                                                    BuyItNow bin = b.build();

                                                    // Place BIN back in storage, in a state such that it'll only be
                                                    // accessible via the lister's stash
                                                    GTSPlugin.instance().storage().publishListing(bin);
                                                }
                                            });
                                        } else {
                                            this.player().sendMessage(service.parse(
                                                    Utilities.readMessageConfigOption(MsgConfigKeys.REQUEST_FAILED),
                                                    sources.append(ErrorCode.class, () -> response.getErrorCode().orElse(ErrorCodes.UNKNOWN))
                                            ));
                                        }
                                    });
                        } else {
                            Auction auction = (Auction) this.listing;
                            if(auction.hasAnyBidsPlaced()) {
                                GTSPlugin.instance().messagingService()
                                        .requestClaim(this.listing.getID(), this.viewer.uuid(), null, true)
                                        .thenAccept(r -> {
                                            ClaimMessage.Response.AuctionResponse response = (ClaimMessage.Response.AuctionResponse) r;

                                            if (response.wasSuccessful()) {
                                                Impactor.getInstance().getScheduler().executeSync(() -> {
                                                    // This is the user attempting to receive the money for the auction

                                                    MonetaryPrice wrapper = new MonetaryPrice(auction.getHighBid().get().getSecond().getAmount());
                                                    if (wrapper.reward(this.viewer.uuid())) {
                                                        this.player().sendMessage(service.parse(
                                                                Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_ITEM_CLAIMED),
                                                                sources
                                                        ));
                                                    } else {
                                                        this.player().sendMessage(Utilities.PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_LISTING_FAIL_TO_RETURN)));

                                                        GTSPlugin.instance().storage().appendOldClaimStatus(
                                                                auction.getID(),
                                                                response.hasListerClaimed(),
                                                                response.hasWinnerClaimed(),
                                                                response.getAllOtherClaimers()
                                                        );
                                                    }
                                                });
                                            } else {
                                                this.player().sendMessage(service.parse(
                                                        Utilities.readMessageConfigOption(MsgConfigKeys.REQUEST_FAILED),
                                                        sources.append(ErrorCode.class, () -> response.getErrorCode().orElse(ErrorCodes.UNKNOWN))
                                                ));
                                            }
                                        });
                            } else {
                                GTSPlugin.instance().messagingService()
                                        .requestAuctionCancellation(this.listing.getID(), this.viewer.uuid())
                                        .thenAccept(response -> {
                                            if (response.wasSuccessful()) {
                                                Impactor.getInstance().getScheduler().executeSync(() -> {
                                                    if (this.listing.getEntry().give(this.viewer.uuid())) {
                                                        this.player().sendMessage(Utilities.PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_ITEM_CLAIMED), sources));
                                                    } else {
                                                        this.player().sendMessage(Utilities.PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_LISTING_FAIL_TO_RETURN)));

                                                        // Set auction as expired, with no bids
                                                        Auction fallback = Auction.builder()
                                                                .from((Auction) this.listing)
                                                                .expiration(LocalDateTime.now())
                                                                .bids(ArrayListMultimap.create())
                                                                .build();

                                                        // Place auction back in storage, in a state such that it'll only be
                                                        // accessible via the lister's stash
                                                        GTSPlugin.instance().storage().publishListing(fallback);
                                                    }
                                                });
                                            } else {
                                                this.player().sendMessage(service.parse(
                                                        Utilities.readMessageConfigOption(MsgConfigKeys.REQUEST_FAILED),
                                                        sources.append(ErrorCode.class, () -> response.getErrorCode().orElse(ErrorCodes.UNKNOWN))
                                                ));
                                            }
                                        });
                            }
                        }
                    } else {
                        if(this.listing instanceof Auction) {
                            GTSPlugin.instance().messagingService()
                                    .requestClaim(this.listing.getID(), this.viewer.uuid(), null, true)
                                    .thenAccept(r -> {
                                        ClaimMessage.Response.AuctionResponse response = (ClaimMessage.Response.AuctionResponse) r;

                                        if (response.wasSuccessful()) {
                                            Impactor.getInstance().getScheduler().executeSync(() -> {
                                                // This is the user attempting to receive the money for the auction

                                                Auction auction = (Auction) this.listing;

                                                if (auction.getHighBid().get().getFirst().equals(this.viewer.uuid())) {
                                                    if (auction.getEntry().give(this.viewer.uuid())) {
                                                        this.player().sendMessage(service.parse(
                                                                Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_ITEM_CLAIMED),
                                                                sources
                                                        ));
                                                    } else {
                                                        this.player().sendMessage(Utilities.PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_LISTING_FAIL_TO_RETURN)));

                                                        GTSPlugin.instance().storage().appendOldClaimStatus(
                                                                auction.getID(),
                                                                response.hasListerClaimed(),
                                                                response.hasWinnerClaimed(),
                                                                response.getAllOtherClaimers()
                                                        );
                                                    }
                                                } else {
                                                    double bid = auction.getCurrentBid(this.viewer.uuid()).get().getAmount();
                                                    MonetaryPrice price = new MonetaryPrice(bid);
                                                    if (price.reward(this.viewer.uuid())) {
                                                        this.player().sendMessage(service.parse(
                                                                Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_ITEM_CLAIMED),
                                                                sources
                                                        ));
                                                    } else {
                                                        this.player().sendMessage(Utilities.PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_LISTING_FAIL_TO_RETURN)));

                                                        GTSPlugin.instance().storage().appendOldClaimStatus(
                                                                auction.getID(),
                                                                response.hasListerClaimed(),
                                                                response.hasWinnerClaimed(),
                                                                response.getAllOtherClaimers()
                                                        );
                                                    }
                                                }
                                            });
                                        } else {
                                            this.player().sendMessage(service.parse(
                                                    Utilities.readMessageConfigOption(MsgConfigKeys.REQUEST_FAILED),
                                                    sources.append(ErrorCode.class, () -> response.getErrorCode().orElse(ErrorCodes.UNKNOWN))
                                            ));
                                        }
                                    });
                        } else {
                            GTSPlugin.instance().messagingService()
                                    .requestClaim(this.listing.getID(), this.viewer.uuid(), null, false)
                                    .thenAccept(r -> {
                                        if(r.wasSuccessful()) {
                                            BuyItNow bin = (BuyItNow) this.listing;
                                            if (bin.stashedForPurchaser()) {
                                                Impactor.getInstance().getScheduler().executeSync(() -> {
                                                    if (!this.listing.getEntry().give(this.viewer.uuid())) {
                                                        // Re-append data as our claim request will have deleted it
                                                        GTSPlugin.instance().storage().publishListing(BuyItNow.builder()
                                                                .from(bin)
                                                                .build()
                                                        );
                                                        this.player().sendMessage(service.parse(
                                                                Utilities.readMessageConfigOption(MsgConfigKeys.REQUEST_FAILED),
                                                                sources.append(ErrorCode.class, () -> ErrorCodes.FAILED_TO_GIVE)
                                                        ));
                                                    } else {
                                                        this.player().sendMessage(service.parse(
                                                                Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_ITEM_CLAIMED),
                                                                sources
                                                        ));
                                                    }
                                                });
                                            }
                                        }
                                    });
                        }
                    }

                    return false;
                })
                .build();

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

        Sponge.server().serviceProvider().economyService()
                        .orElseThrow(IllegalStateException::new)
                        .findOrCreateAccount(this.viewer.uuid())
                        .ifPresent(account -> {
                            double balance = account.balance(currency).doubleValue();
                            canAfford.set(balance >= value);
                            isExact.set(balance > value);
                        });

        return new Tuple<>(canAfford.get(), isExact.get());
    }

    private ItemStack buildBidIcon() {
        final Config lang = GTSPlugin.instance().configuration().language();
        final MessageService service = Impactor.getInstance().getRegistry().get(MessageService.class);
        Auction auction = (Auction) this.listing;
        PlaceholderSources sources = PlaceholderSources.builder()
                .append(Auction.class, () -> auction)
                .build();


        AtomicDouble previous = new AtomicDouble(0);
        List<Component> lore = Lists.newArrayList();
        ConfigKey<List<String>> base = MsgConfigKeys.UI_ICON_PLACE_BID_LORE;
        if(auction.getBids().containsKey(this.viewer.uuid())) {
            base = MsgConfigKeys.UI_ICON_PLACE_BID_WITH_USER_BID_PLACED_LORE;
            sources.append(Double.class, () -> (previous.addAndGet(auction.getCurrentBid(this.viewer.uuid()).map(Auction.Bid::getAmount).orElse(0D))));
        }

        lore.addAll(service.parse(lang.get(base), sources));

        double required = auction.getNextBidRequirement();
        AtomicBoolean canAfford = new AtomicBoolean(false);
        EconomyService economy = Sponge.server().serviceProvider().economyService().orElseThrow(IllegalStateException::new);
        economy.findOrCreateAccount(this.viewer.uuid())
                .ifPresent(account -> {
                    if(account.balance(economy.defaultCurrency()).doubleValue() >= required) {
                        canAfford.set(true);
                    }
                });

        ConfigKey<List<String>> append = canAfford.get() ? MsgConfigKeys.UI_ICON_PLACE_BID_CAN_AFFORD : MsgConfigKeys.UI_ICON_PLACE_BID_CANT_AFFORD;
        if(auction.getHighBid().map(t -> t.getFirst().equals(this.viewer.uuid())).orElse(false)) {
            append = MsgConfigKeys.UI_ICON_PLACE_BID_IS_TOP_BID;
        }

        lore.addAll(service.parse(lang.get(append), sources));

        return ItemStack.builder()
                .itemType(ItemTypes.WRITABLE_BOOK)
                .add(Keys.CUSTOM_NAME, service.parse(lang.get(MsgConfigKeys.UI_ICON_PLACE_BID_TITLE)))
                .add(Keys.LORE, lore)
                .build();
    }

    private Icon<ItemStack> getBidHistory() {
        final Config lang = GTSPlugin.instance().configuration().language();
        final MessageService service = Impactor.getInstance().getRegistry().get(MessageService.class);
        Auction auction = (Auction) this.listing;
        PlaceholderSources sources = PlaceholderSources.builder()
                .append(Auction.class, () -> auction)
                .build();

        List<Component> lore = Lists.newArrayList();
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
                sources.append(Auction.BidContext.class, () -> context);
                lore.add(service.parse(lang.get(MsgConfigKeys.UI_ICON_BID_HISTORY_SEPARATOR)));
                lore.addAll(service.parse(lang.get(MsgConfigKeys.UI_ICON_BID_HISTORY_BID_INFO), sources));
            }
        } else {
            if(!this.listing.getLister().equals(this.viewer.uuid())) {
                lore.addAll(service.parse(lang.get(MsgConfigKeys.UI_ICON_BID_HISTORY_NO_BIDS)));
            }
        }

        ItemStack display = ItemStack.builder()
                .itemType(ItemTypes.PAPER)
                .add(Keys.CUSTOM_NAME, service.parse(lang.get(MsgConfigKeys.UI_ICON_BID_HISTORY_TITLE)))
                .add(Keys.LORE, lore)
                .build();

        return Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(display))
                .build();
    }

    private ServerPlayer player() {
        return Sponge.server().player(this.viewer.uuid()).orElseThrow(IllegalStateException::new);
    }
}
