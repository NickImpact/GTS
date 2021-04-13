package net.impactdev.gts.ui.submenu.stash;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.impactdev.gts.api.listings.makeup.Display;
import net.impactdev.gts.api.messaging.message.type.listings.ClaimMessage;
import net.impactdev.gts.api.stashes.StashedContent;
import net.impactdev.gts.api.util.TriState;
import net.impactdev.gts.common.utils.future.CompletableFutureManager;
import net.impactdev.gts.ui.SpongeMainMenu;
import net.impactdev.gts.ui.submenu.browser.SpongeSelectedListingMenu;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.gui.InventoryDimensions;
import net.impactdev.impactor.api.gui.Page;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.impactor.api.utilities.mappings.Tuple;
import net.impactdev.impactor.sponge.ui.SpongeIcon;
import net.impactdev.impactor.sponge.ui.SpongeLayout;
import net.impactdev.impactor.sponge.ui.SpongeUI;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.api.stashes.Stash;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.config.wrappers.TitleLorePair;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.ui.Historical;
import net.impactdev.gts.sponge.listings.SpongeListing;
import net.impactdev.gts.sponge.pricing.provided.MonetaryPrice;
import net.impactdev.gts.sponge.ui.SpongeAsyncPage;
import net.impactdev.gts.sponge.utils.Utilities;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.Text;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.impactdev.gts.sponge.utils.Utilities.PARSER;
import static net.impactdev.gts.sponge.utils.Utilities.readMessageConfigOption;

public class SpongeStashMenu extends SpongeAsyncPage<StashedContent> implements Historical<SpongeMainMenu> {

    private static final ExecutorService CLAIMER = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(),
            new ThreadFactoryBuilder()
                    .setNameFormat("GTS Stash Claiming Executor - #%d")
                    .setDaemon(true)
                    .build()
    );

    public SpongeStashMenu(Player viewer) {
        super(GTSPlugin.getInstance(),
                viewer,
                GTSPlugin.getInstance().getStorage().getStash(viewer.getUniqueId()).thenApply(Stash::getStashContents)
        );

        final Config lang = GTSPlugin.getInstance().getMsgConfig();
        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);
        this.applier(content -> {
            SpongeListing listing = (SpongeListing) content.getListing();

            Display<ItemStack> display = listing.getEntry().getDisplay(viewer.getUniqueId(), listing);
            ItemStack item = display.get();

            Optional<List<Text>> lore = item.get(Keys.ITEM_LORE);
            lore.ifPresent(texts -> texts.addAll(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_LISTING_DETAIL_SEPARATOR))));

            Supplier<List<Text>> append = () -> {
                List<Text> result = Lists.newArrayList();
                if(listing instanceof Auction) {
                    Auction auction = (Auction) listing;
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
                } else if(listing instanceof BuyItNow) {
                    BuyItNow bin = (BuyItNow) listing;

                    List<String> input = lang.get(MsgConfigKeys.UI_BIN_DETAILS);
                    List<Supplier<Object>> sources = Lists.newArrayList(() -> bin);
                    result.addAll(service.parse(input, sources));
                }
                return result;
            };
            List<Text> result = lore.orElse(Lists.newArrayList());
            result.addAll(append.get());
            item.offer(Keys.ITEM_LORE, result);

            SpongeIcon icon = new SpongeIcon(item);
            icon.addListener(clickable -> {
                new SpongeSelectedListingMenu(this.getViewer(), listing, () -> new SpongeStashMenu(this.getViewer()), true, false).open();
            });
            return icon;
        });
    }

    @Override
    protected Text getTitle() {
        return Utilities.PARSER.parse(GTSPlugin.getInstance().getMsgConfig().get(MsgConfigKeys.UI_MENU_STASH_TITLE), Lists.newArrayList(this::getViewer));
    }

    @Override
    protected Map<Page.PageIconType, Page.PageIcon<ItemType>> getPageIcons() {
        Map<Page.PageIconType, Page.PageIcon<ItemType>> options = Maps.newHashMap();
        options.put(Page.PageIconType.PREV, new Page.PageIcon<>(ItemTypes.ARROW, 37));
        options.put(Page.PageIconType.NEXT, new Page.PageIcon<>(ItemTypes.ARROW, 43));

        return options;
    }

    @Override
    protected InventoryDimensions getContentZone() {
        return new InventoryDimensions(7, 2);
    }

    @Override
    protected Tuple<Integer, Integer> getOffsets() {
        return new Tuple<>(1, 1);
    }

    @Override
    protected Tuple<Long, TimeUnit> getTimeout() {
        return new Tuple<>(3L, TimeUnit.SECONDS);
    }

    @Override
    protected SpongeLayout design() {
        SpongeLayout.SpongeLayoutBuilder slb = SpongeLayout.builder();
        slb.rows(SpongeIcon.BORDER, 0, 3).columns(SpongeIcon.BORDER, 0, 8).slots(SpongeIcon.BORDER, 38, 42);

        SpongeIcon collect = new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.HOPPER)
                .add(Keys.DISPLAY_NAME, Utilities.PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_ICON_STASH_COLLECT_ALL_TITLE)))
                .add(Keys.ITEM_LORE, Utilities.PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_ICON_STASH_COLLECT_ALL_LORE)))
                .build()
        );
        collect.addListener(clickable -> {
            this.getViewer().sendMessage(Utilities.PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_BEGIN_PROCESSING_REQUEST)));
            this.getView().close(this.getViewer());
            CompletableFutureManager.makeFuture(() -> {
                // Lets use this to ensure our debug messages are lined up accordingly
                AtomicInteger successful = new AtomicInteger();
                AtomicBoolean ready = new AtomicBoolean(true);
                CountDownLatch finished = new CountDownLatch(this.getContents().size());

                for(StashedContent entry : ImmutableList.copyOf(this.getContents())) {
                    while(!ready.get()) {}
                    ready.set(false);

                    Listing listing = entry.getListing();
                    boolean lister = listing.getLister().equals(this.getViewer().getUniqueId());

                    GTSPlugin.getInstance().getMessagingService().requestClaim(
                            listing.getID(),
                            this.getViewer().getUniqueId(),
                            null,
                            listing instanceof Auction
                    ).thenAccept(response -> {
                        // Handle message accordingly
                        //
                        // NOTE: We don't care about non-successful requests here, as they shouldn't affect the
                        // user's stash and will therefore not end up counted.
                        if (response.wasSuccessful()) {
                            if(lister) {
                                Impactor.getInstance().getScheduler().executeSync(() -> {
                                    try {
                                        if (response.isAuction()) {
                                            ClaimMessage.Response.AuctionResponse actual = (ClaimMessage.Response.AuctionResponse) response;

                                            Auction auction = (Auction) listing;
                                            boolean hasBids = !auction.getBids().isEmpty();

                                            // If an auction has bids, return the highest bid value.
                                            // Otherwise, return the entry element
                                            if (hasBids) {
                                                if (new MonetaryPrice(auction.getHighBid().get().getSecond().getAmount()).reward(this.getViewer().getUniqueId())) {
                                                    successful.incrementAndGet();
                                                } else {
                                                    // Re-append our data as the claim request will have been updated
                                                    GTSPlugin.getInstance().getStorage().appendOldClaimStatus(
                                                            auction.getID(),
                                                            actual.hasListerClaimed(),
                                                            actual.hasWinnerClaimed(),
                                                            actual.getAllOtherClaimers()
                                                    );
                                                }
                                            } else {
                                                // No bids, return the listing instead
                                                if (listing.getEntry().give(this.getViewer().getUniqueId())) {
                                                    successful.incrementAndGet();
                                                } else {
                                                    // Re-append data as our claim request will have deleted it
                                                    GTSPlugin.getInstance().getStorage().appendOldClaimStatus(
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
                                                if (bin.getPrice().reward(this.getViewer().getUniqueId())) {
                                                    successful.incrementAndGet();
                                                } else {
                                                    // Re-append data as our claim request will have deleted it
                                                    GTSPlugin.getInstance().getStorage().publishListing(BuyItNow.builder()
                                                            .from(bin)
                                                            .purchased()
                                                            .expiration(LocalDateTime.now())
                                                            .build()
                                                    );
                                                }
                                            } else {
                                                // Listing expired
                                                if (listing.getEntry().give(this.getViewer().getUniqueId())) {
                                                    successful.incrementAndGet();
                                                } else {
                                                    // Re-append data as our claim request will have deleted it
                                                    GTSPlugin.getInstance().getStorage().publishListing(BuyItNow.builder()
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
                                if(response.isAuction()) {
                                    Auction auction = (Auction) listing;
                                    ClaimMessage.Response.AuctionResponse actual = (ClaimMessage.Response.AuctionResponse) response;

                                    Impactor.getInstance().getScheduler().executeSync(() -> {
                                        try {
                                            if(entry.getContext() == TriState.UNDEFINED) {
                                                Auction.Bid bid = auction.getCurrentBid(this.getViewer().getUniqueId()).orElseThrow(() -> new IllegalStateException("Unable to locate bid for user where required"));

                                                MonetaryPrice value = new MonetaryPrice(bid.getAmount());
                                                value.reward(this.getViewer().getUniqueId());

                                                successful.incrementAndGet();
                                            } else if (listing.getEntry().give(this.getViewer().getUniqueId())) {
                                                successful.incrementAndGet();
                                            } else {
                                                // Re-append data as our claim request will have deleted it
                                                GTSPlugin.getInstance().getStorage().appendOldClaimStatus(
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
                                    if(bin.stashedForPurchaser()) {
                                        Impactor.getInstance().getScheduler().executeSync(() -> {
                                            try {
                                                if(listing.getEntry().give(this.getViewer().getUniqueId())) {
                                                    successful.getAndIncrement();
                                                } else {
                                                    // Re-append data as our claim request will have deleted it
                                                    GTSPlugin.getInstance().getStorage().publishListing(BuyItNow.builder()
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
                    finished.countDown();
                }

                try {
                    finished.await();
                } catch (Exception ignored) {}

                while(!ready.get()) {}
                this.getViewer().sendMessage(Utilities.PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.STASH_COLLECT_ALL_RESULTS), Lists.newArrayList(successful::get)));
            }, CLAIMER);
        });
        slb.slot(collect, 41);

        SpongeIcon back = new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.BARRIER)
                .add(Keys.DISPLAY_NAME, Utilities.PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_GENERAL_BACK), Lists.newArrayList(this::getViewer)))
                .build()
        );
        back.addListener(clickable -> {
            this.cancelIfRunning();
            this.getParent().ifPresent(parent -> parent.get().open());
        });
        slb.slot(back, 39);

        return slb.build();
    }

    @Override
    protected SpongeUI build(SpongeLayout layout) {
        return SpongeUI.builder()
                .title(this.title)
                .dimension(InventoryDimension.of(9, 5))
                .build()
                .define(this.layout);
    }

    @Override
    protected SpongeIcon getLoadingIcon() {
        return new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.STAINED_GLASS_PANE)
                .add(Keys.DISPLAY_NAME, Utilities.PARSER.parse(
                        Utilities.readMessageConfigOption(MsgConfigKeys.UI_MENU_LISTINGS_SPECIAL_LOADING),
                        Lists.newArrayList(this::getViewer)
                ))
                .add(Keys.DYE_COLOR, DyeColors.YELLOW)
                .build()
        );
    }

    @Override
    protected SpongeIcon getTimeoutIcon() {
        TitleLorePair pair = Utilities.readMessageConfigOption(MsgConfigKeys.UI_MENU_LISTINGS_SPECIAL_TIMED_OUT);
        return new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.STAINED_GLASS_PANE)
                .add(Keys.DISPLAY_NAME, Utilities.PARSER.parse(pair.getTitle(), Lists.newArrayList(this::getViewer)))
                .add(Keys.DYE_COLOR, DyeColors.RED)
                .add(Keys.ITEM_LORE, Utilities.PARSER.parse(pair.getLore(), Lists.newArrayList(this::getViewer)))
                .build()
        );
    }

    @Override
    protected Consumer<List<StashedContent>> applyWhenReady() {
        return stash -> {};
    }

    @Override
    public Optional<Supplier<SpongeMainMenu>> getParent() {
        return Optional.of(() -> new SpongeMainMenu(this.getViewer()));
    }
}
