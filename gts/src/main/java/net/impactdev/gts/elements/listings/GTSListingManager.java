package net.impactdev.gts.elements.listings;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.impactdev.gts.api.events.factory.GTSEventFactory;
import net.impactdev.gts.api.logging.LogAction;
import net.impactdev.gts.api.logging.LogEntry;
import net.impactdev.gts.api.modules.markets.ListingManager;
import net.impactdev.gts.api.elements.listings.models.Auction;
import net.impactdev.gts.api.elements.listings.models.BuyItNow;
import net.impactdev.gts.api.elements.listings.Listing;
import net.impactdev.gts.api.players.NotificationType;
import net.impactdev.gts.api.registries.players.PlayerSettingsRegistry;
import net.impactdev.gts.events.EventPublisher;
import net.impactdev.gts.locale.Translatables;
import net.impactdev.gts.plugin.GTSPlugin;
import net.impactdev.gts.storage.GTSStorage;
import net.impactdev.gts.util.future.Futures;
import net.impactdev.gts.util.future.SynchronizedLock;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.platform.sources.PlatformPlayer;
import net.impactdev.impactor.api.platform.sources.PlatformPlayerService;
import net.impactdev.impactor.api.utility.Context;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GTSListingManager implements ListingManager {

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(),
            new ThreadFactoryBuilder()
                    .setNameFormat("GTS Listing Manager - #%d")
                    .setDaemon(true)
                    .build()
    );

    private final GTSStorage storage = GTSPlugin.instance().storage();
    private final Cache<UUID, Listing> cache = Caffeine.newBuilder().build();

    public GTSListingManager() {
        this.storage.listings().thenAccept(
                results -> results.forEach(listing -> this.cache.put(listing.id(), listing))
        );
    }

    @Override
    public Set<Listing> fetch(Predicate<Listing> filter) {
        return ImmutableSet.copyOf(this.cache.asMap().values());
    }

    @Override
    public CompletableFuture<Boolean> list(UUID actor, Listing listing) {
        PlatformPlayer lister = PlatformPlayer.getOrCreate(actor);
        Context context = Context.empty();
        context.append(PlatformPlayer.class, lister);
        context.append(Listing.class, listing);

        return Futures.execute(EXECUTOR, () -> {
            Translatables.BEGIN_LISTING_PUBLISH.send(lister, context);

            // Verify a user can actually publish another listing
            boolean canPublish = this.hasMax(actor).join();
            if(!canPublish) {
                Translatables.FAILED_PUBLISH_MAX_LISTINGS.send(lister, context);
                return false;
            }

            // Publish event to listeners
            if(!EventPublisher.post(GTSEventFactory.createPublishListingEvent(lister, listing))) {
                Translatables.FAILED_PUBLISH_LISTENER_FAILURE.send(lister, context);
                return false;
            }

            // TODO - Price validation

            // Take the listing from the lister
            SynchronizedLock lock = SynchronizedLock.create(1);
            Translatables.PUBLISHED_LISTING_COLLECTION.send(lister, context);
            Impactor.instance().scheduler().sync().execute(() -> {
                lock.result(listing.content().take(lister));
                lock.latch().countDown();
            });
            lock.latch().await();
            if(!lock.result()) {
                // TODO - Take failed, inform

                return false;
            }

            this.storage.publishListing(listing).join();

            PlayerSettingsRegistry settings = null;
            Set<PlatformPlayer> notify = Impactor.instance().services()
                    .provide(PlatformPlayerService.class)
                    .online()
                    .stream()
                    .filter(user -> settings.findOrCreate(user.uuid()).join().notification(NotificationType.Publish))
                    .collect(Collectors.toSet());

            if(listing instanceof BuyItNow) {
                Translatables.PUBLISH_BIN_LISTING.send(Audience.audience(notify), context);
            } else {
                Translatables.PUBLISH_AUCTION_LISTING.send(Audience.audience(notify), context);
            }

            LogEntry entry = LogEntry.builder()
                    .timestamp(Instant.now())
                    .source(null)
                    .target(listing)
                    .action(LogAction.CREATE)
                    .build();

            // Forward to messaging service & discord
            return true;
        });
    }

    @Override
    public CompletableFuture<Boolean> purchase(UUID actor, BuyItNow listing, @Nullable Object selection) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> bid(UUID actor, Auction auction, BigDecimal amount) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> remove(UUID actor, UUID listing) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> hasMax(UUID target) {
        GTSStorage storage = GTSPlugin.instance().storage();

        return null;
    }
}
