package net.impactdev.gts.ui.submenu.stash;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.leangen.geantyref.TypeToken;
import net.impactdev.gts.api.deliveries.Delivery;
import net.impactdev.gts.api.listings.makeup.Display;
import net.impactdev.gts.api.messaging.message.type.listings.ClaimMessage;
import net.impactdev.gts.api.stashes.StashedContent;
import net.impactdev.gts.api.ui.GTSMenu;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;
import net.impactdev.gts.common.utils.future.CompletableFutureManager;
import net.impactdev.gts.sponge.deliveries.SpongeDelivery;
import net.impactdev.gts.sponge.utils.items.ProvidedIcons;
import net.impactdev.gts.ui.SpongeMainMenu;
import net.impactdev.gts.ui.submenu.browser.SpongeSelectedListingMenu;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.placeholders.PlaceholderSources;
import net.impactdev.impactor.api.platform.players.PlatformPlayer;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.impactor.api.ui.containers.icons.ClickProcessor;
import net.impactdev.impactor.api.ui.containers.icons.DisplayProvider;
import net.impactdev.impactor.api.ui.containers.icons.Icon;
import net.impactdev.impactor.api.ui.containers.layouts.Layout;
import net.impactdev.impactor.api.ui.containers.pagination.Pagination;
import net.impactdev.impactor.api.ui.containers.pagination.updaters.PageUpdater;
import net.impactdev.impactor.api.ui.containers.pagination.updaters.PageUpdaterType;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.api.stashes.Stash;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.ui.Historical;
import net.impactdev.gts.sponge.listings.SpongeListing;
import net.impactdev.gts.sponge.pricing.provided.MonetaryPrice;
import net.impactdev.gts.sponge.utils.Utilities;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.util.TriState;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.math.vector.Vector2i;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SpongeStashMenu implements Historical<SpongeMainMenu>, GTSMenu {

    private static final ExecutorService CLAIMER = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(),
            new ThreadFactoryBuilder()
                    .setNameFormat("GTS Stash Claiming Executor - #%d")
                    .setDaemon(true)
                    .build()
    );

    private static final MessageService service = Impactor.getInstance().getRegistry().get(MessageService.class);

    private final Pagination.Generic<StashedContent<?>> pagination;
    private final UUID viewer;

    public SpongeStashMenu(ServerPlayer viewer) {
        this.viewer = viewer.uniqueId();

        this.pagination = Pagination.builder()
                .provider(Key.key("gts", "stash"))
                .title(service.parse(GTSPlugin.instance().configuration().language().get(MsgConfigKeys.UI_MENU_STASH_TITLE)))
                .layout(this.design())
                .readonly(true)
                .viewer(PlatformPlayer.from(viewer))
                .style(TriState.TRUE)
                .zone(Vector2i.from(7, 2), Vector2i.ONE)
                .updater(PageUpdater.builder()
                        .type(PageUpdaterType.PREVIOUS)
                        .slot(37)
                        .provider(target -> ItemStack.builder()
                                .itemType(ItemTypes.SPECTRAL_ARROW)
                                .add(Keys.CUSTOM_NAME, MiniMessage.miniMessage().deserialize("<gradient:red:gold>Previous Page (" + target + ")</gradient>"))
                                .build()
                        )
                        .build()
                )
                .updater(PageUpdater.builder()
                        .type(PageUpdaterType.NEXT)
                        .slot(43)
                        .provider(target -> ItemStack.builder()
                                .itemType(ItemTypes.SPECTRAL_ARROW)
                                .add(Keys.CUSTOM_NAME, MiniMessage.miniMessage().deserialize("<gradient:green:blue>Next Page (" + target + ")</gradient>"))
                                .build()
                        )
                        .build()
                )
                .asynchronous(new TypeToken<StashedContent<?>>() {})
                .accumulator(this.fetchAndTranslate(viewer))
                .build();
    }

    public void open() {
        this.pagination.open();
    }

    private ServerPlayer player() {
        return Sponge.server().player(this.viewer).orElseThrow(() -> new IllegalStateException("Viewer player could not be found"));
    }

    protected Layout design() {
        Layout.LayoutBuilder slb = Layout.builder();
        slb.size(5)
                .rows(ProvidedIcons.BORDER, 1, 4)
                .columns(ProvidedIcons.BORDER, 1, 9)
                .slots(ProvidedIcons.BORDER, 38, 42);

        Icon<ItemStack> collect = Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(ItemStack.builder()
                    .itemType(ItemTypes.HOPPER)
                    .add(Keys.CUSTOM_NAME, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_ICON_STASH_COLLECT_ALL_TITLE)))
                    .add(Keys.LORE, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_ICON_STASH_COLLECT_ALL_LORE)))
                    .build()
                ))
                .listener(context -> {
                    this.player().sendMessage(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_BEGIN_PROCESSING_REQUEST)));
                    CompletableFutureManager.makeFuture(() -> {
                        // Lets use this to ensure our debug messages are lined up accordingly
                        List<StashedContent<?>> contents = this.pagination.pages().getFramesNonCircular()
                                .stream()
                                .flatMap(page -> page.icons().values().stream())
                                .map(icon -> ((Icon.Binding<ItemStack, StashedContent<?>>) icon).binding())
                                .collect(Collectors.toList());

                        AtomicInteger successful = new AtomicInteger();
                        AtomicBoolean ready = new AtomicBoolean(true);
                        CountDownLatch finished = new CountDownLatch(contents.size());

                        for(StashedContent<?> entry : ImmutableList.copyOf(contents)) {
                            while(!ready.get()) {
                                try {
                                    //noinspection BusyWait
                                    Thread.sleep(50);
                                } catch (InterruptedException e) {
                                    ExceptionWriter.write(e);
                                }
                            }
                            ready.set(false);

                            if(entry instanceof StashedContent.ListingContent) {
                                Listing listing = ((StashedContent.ListingContent) entry).getContent();
                                boolean lister = listing.getLister().equals(this.viewer);

                                GTSPlugin.instance().messagingService().requestClaim(
                                        listing.getID(),
                                        this.viewer,
                                        null,
                                        listing instanceof Auction
                                ).thenAccept(response -> {
                                    // Handle message accordingly
                                    //
                                    // NOTE: We don't care about non-successful requests here, as they shouldn't affect the
                                    // user's stash and will therefore not end up counted.
                                    if (response.wasSuccessful()) {
                                        if (lister) {
                                            Impactor.getInstance().getScheduler().executeSync(() -> {
                                                try {
                                                    if (response.isAuction()) {
                                                        ClaimMessage.Response.AuctionResponse actual = (ClaimMessage.Response.AuctionResponse) response;

                                                        Auction auction = (Auction) listing;
                                                        boolean hasBids = !auction.getBids().isEmpty();

                                                        // If an auction has bids, return the highest bid value.
                                                        // Otherwise, return the entry element
                                                        if (hasBids) {
                                                            if (new MonetaryPrice(auction.getHighBid().get().getSecond().getAmount()).reward(this.viewer)) {
                                                                successful.incrementAndGet();
                                                            } else {
                                                                // Re-append our data as the claim request will have been updated
                                                                GTSPlugin.instance().storage().appendOldClaimStatus(
                                                                        auction.getID(),
                                                                        actual.hasListerClaimed(),
                                                                        actual.hasWinnerClaimed(),
                                                                        actual.getAllOtherClaimers()
                                                                );
                                                            }
                                                        } else {
                                                            // No bids, return the listing instead
                                                            if (listing.getEntry().give(this.viewer)) {
                                                                successful.incrementAndGet();
                                                            } else {
                                                                // Re-append data as our claim request will have deleted it
                                                                GTSPlugin.instance().storage().appendOldClaimStatus(
                                                                        auction.getID(),
                                                                        actual.hasListerClaimed(),
                                                                        actual.hasWinnerClaimed(),
                                                                        actual.getAllOtherClaimers()
                                                                );
                                                            }
                                                        }
                                                    } else {
                                                        BuyItNow bin = (BuyItNow) listing;
                                                        if (bin.isPurchased()) {
                                                            if (bin.getPrice().reward(this.viewer)) {
                                                                successful.incrementAndGet();
                                                            } else {
                                                                // Re-append data as our claim request will have deleted it
                                                                GTSPlugin.instance().storage().publishListing(BuyItNow.builder()
                                                                        .from(bin)
                                                                        .purchased()
                                                                        .expiration(LocalDateTime.now())
                                                                        .build()
                                                                );
                                                            }
                                                        } else {
                                                            // Listing expired
                                                            if (listing.getEntry().give(this.viewer)) {
                                                                successful.incrementAndGet();
                                                            } else {
                                                                // Re-append data as our claim request will have deleted it
                                                                GTSPlugin.instance().storage().publishListing(BuyItNow.builder()
                                                                        .from(bin)
                                                                        .expiration(LocalDateTime.now())
                                                                        .build()
                                                                );
                                                            }
                                                        }
                                                    }
                                                } finally {
                                                    ready.set(true);
                                                }
                                            });
                                        } else {
                                            if (response.isAuction()) {
                                                Auction auction = (Auction) listing;
                                                ClaimMessage.Response.AuctionResponse actual = (ClaimMessage.Response.AuctionResponse) response;

                                                Impactor.getInstance().getScheduler().executeSync(() -> {
                                                    try {
                                                        if (entry.getContext() == TriState.NOT_SET) {
                                                            Auction.Bid bid = auction.getCurrentBid(this.viewer).orElseThrow(() -> new IllegalStateException("Unable to locate bid for user where required"));

                                                            MonetaryPrice value = new MonetaryPrice(bid.getAmount());
                                                            value.reward(this.viewer);

                                                            successful.incrementAndGet();
                                                        } else if (listing.getEntry().give(this.viewer)) {
                                                            successful.incrementAndGet();
                                                        } else {
                                                            // Re-append data as our claim request will have deleted it
                                                            GTSPlugin.instance().storage().appendOldClaimStatus(
                                                                    auction.getID(),
                                                                    actual.hasListerClaimed(),
                                                                    actual.hasWinnerClaimed(),
                                                                    actual.getAllOtherClaimers()
                                                            );
                                                        }
                                                    } finally {
                                                        ready.set(true);
                                                    }
                                                });
                                            } else {
                                                // Special case: The user directly purchased the listing, but rewarding failed
                                                // at time of purchase.

                                                BuyItNow bin = (BuyItNow) listing;
                                                if (bin.stashedForPurchaser()) {
                                                    Impactor.getInstance().getScheduler().executeSync(() -> {
                                                        try {
                                                            if (listing.getEntry().give(this.viewer)) {
                                                                successful.getAndIncrement();
                                                            } else {
                                                                // Re-append data as our claim request will have deleted it
                                                                GTSPlugin.instance().storage().publishListing(BuyItNow.builder()
                                                                        .from(bin)
                                                                        .build()
                                                                );
                                                            }
                                                        } finally {
                                                            ready.set(true);
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    }
                                }).get(2, TimeUnit.SECONDS);
                            } else {
                                StashedContent.DeliverableContent delivery = (StashedContent.DeliverableContent) entry;
                                this.claimDelivery((SpongeDelivery) delivery.getContent(), () -> ready.set(true)).join();
                            }
                            finished.countDown();
                        }

                        try {
                            finished.await();
                        } catch (Exception ignored) {}

                        while(!ready.get()) {
                            Thread.sleep(50);
                        }

                        PlaceholderSources sources = PlaceholderSources.builder()
                                .append(Integer.class, successful::get)
                                .build();
                        this.player().sendMessage(Utilities.PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.STASH_COLLECT_ALL_RESULTS), sources));
                    }, CLAIMER);

                    return false;
                })
                .build();

        slb.slot(collect, 41);

        ItemStack b = ItemStack.builder()
                .itemType(ItemTypes.BARRIER)
                .add(Keys.CUSTOM_NAME, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_GENERAL_BACK)))
                .build();

        Icon<ItemStack> back = Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(b))
                .listener(context -> {
                    SpongeMainMenu menu = new SpongeMainMenu(this.player());
                    menu.open();
                    return false;
                })
                .build();
        slb.slot(back, 39);

        return slb.build();
    }

    private CompletableFuture<List<Icon.Binding<?, StashedContent<?>>>> fetchAndTranslate(ServerPlayer viewer) {
        return GTSPlugin.instance().storage().getStash(viewer.uniqueId())
                .thenApply(Stash::getStashContents)
                .thenApply(contents -> {
                    Config lang = GTSPlugin.instance().configuration().language();
                    List<Icon.Binding<?, StashedContent<?>>> results = Lists.newArrayList();
                    for(StashedContent<?> content : contents) {
                        AtomicReference<ClickProcessor> processor = new AtomicReference<>(context -> false);
                        Icon.Binding<ItemStack, StashedContent<?>> icon = Icon.builder(ItemStack.class)
                                        .display(() -> {
                                            if(content instanceof StashedContent.ListingContent) {
                                                SpongeListing listing = (SpongeListing) content.getContent();

                                                Display<ItemStack> display = listing.getEntry().getDisplay(this.viewer);
                                                ItemStack item = display.get();

                                                Optional<List<Component>> lore = item.get(Keys.LORE);
                                                lore.ifPresent(texts -> texts.addAll(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_LISTING_DETAIL_SEPARATOR))));

                                                List<Component> fill = Lists.newArrayList();
                                                if (listing instanceof Auction) {
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

                                                    fill.addAll(service.parse(input, sources));
                                                } else if (listing instanceof BuyItNow) {
                                                    BuyItNow bin = (BuyItNow) listing;

                                                    List<String> input = lang.get(MsgConfigKeys.UI_BIN_DETAILS);
                                                    PlaceholderSources sources = PlaceholderSources.builder()
                                                            .append(BuyItNow.class, () -> bin)
                                                            .build();
                                                    fill.addAll(service.parse(input, sources));
                                                }

                                                List<Component> result = lore.orElse(Lists.newArrayList());
                                                result.addAll(fill);
                                                item.offer(Keys.LORE, result);

                                                processor.set(context -> {
                                                    new SpongeSelectedListingMenu(this.player(), listing, () -> new SpongeStashMenu(this.player()), true, false).open();
                                                    return false;
                                                });
                                                return item;
                                            } else {
                                                SpongeDelivery delivery = (SpongeDelivery) content.getContent();
                                                Display<ItemStack> display = delivery.getContent().getDisplay(this.viewer);
                                                ItemStack item = display.get();

                                                Optional<List<Component>> lore = item.get(Keys.LORE);
                                                lore.ifPresent(texts -> texts.addAll(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_LISTING_DETAIL_SEPARATOR))));

                                                List<Component> fill = Lists.newArrayList();
                                                List<String> buffer = Lists.newArrayList();
                                                buffer.addAll(lang.get(MsgConfigKeys.DELIVERY_INFO));
                                                if(delivery.getExpiration().isPresent()) {
                                                    buffer.add(lang.get(MsgConfigKeys.DELIVERY_EXPIRATION_INFO));
                                                }

                                                PlaceholderSources sources = PlaceholderSources.builder()
                                                        .append(Delivery.class, () -> delivery)
                                                        .append(Component.class, () -> delivery.getContent().getName())
                                                        .build();
                                                fill.addAll(service.parse(buffer, sources));
                                                item.offer(Keys.LORE, fill);


                                                return item;
                                            }
                                        })
                                        .listener(processor.get())
                                        .build(() -> content);
                        results.add(icon);
                    }

                    return results;
                });
    }

    @Override
    public Optional<Supplier<SpongeMainMenu>> getParent() {
        return Optional.of(() -> new SpongeMainMenu(this.player()));
    }

    private CompletableFuture<Void> claimDelivery(SpongeDelivery delivery, Runnable callback) {
        return CompletableFutureManager.makeFuture(() -> {
            GTSPlugin.instance().messagingService().requestDeliveryClaim(
                    delivery.getID(),
                    this.viewer
            ).thenAccept(response -> {
                if(response.wasSuccessful()) {
                    Impactor.getInstance().getScheduler().executeSync(() -> {
                        delivery.getContent().give(this.viewer);
                        callback.run();
                    });
                }
            });
        });
    }
}
