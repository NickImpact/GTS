/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package net.impactdev.gts.common.storage;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import net.impactdev.gts.api.deliveries.Delivery;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.communication.message.type.admin.ForceDeleteMessage;
import net.impactdev.gts.api.communication.message.type.deliveries.ClaimDelivery;
import net.impactdev.gts.api.communication.message.type.listings.ClaimMessage;
import net.impactdev.gts.common.messaging.messages.listings.ClaimMessageImpl;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.communication.message.type.auctions.AuctionMessage;
import net.impactdev.gts.api.communication.message.type.listings.BuyItNowMessage;
import net.impactdev.gts.api.player.PlayerSettings;
import net.impactdev.gts.api.stashes.Stash;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.api.storage.GTSStorage;
import net.impactdev.gts.common.storage.implementation.StorageImplementation;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;
import net.impactdev.gts.common.utils.future.CompletableFutureManager;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GTSStorageImpl implements GTSStorage {

    private final StorageImplementation implementation;

    private final LoadingCache<UUID, ReentrantLock> locks = Caffeine.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build(new CacheLoader<UUID, ReentrantLock>() {
                @Override
                public @Nullable ReentrantLock load(@NonNull UUID key) throws Exception {
                    return new ReentrantLock();
                }
            });

    public GTSStorageImpl(GTSPlugin plugin, StorageImplementation implementation) {
        this.implementation = implementation;
    }

    /**
     * Attempts to initialize the storage implementation
     */
    public void init() {
        try {
            this.implementation.init();
        } catch (Exception e) {
            // Log the failure
            throw new RuntimeException(e);
        }
    }

    /**
     * Attempts to shutdown the storage implementation
     */
    public void shutdown() {
        try {
            this.implementation.shutdown();
        } catch (Exception e) {
            // Log the failure
            throw new RuntimeException(e);
        }
    }

    /**
     * Represents any properties which might be set against a storage
     * implementation.
     *
     * @return A mapping of flags to values representing storage implementation
     * properties
     */
    public CompletableFuture<Map<String, String>> getMeta() {
        return this.schedule(this.implementation::getMeta);
    }

    @Override
    public CompletableFuture<Boolean> publishListing(Listing listing) {
        return this.schedule(() -> this.implementation.addListing(listing));
    }

    @Override
    public CompletableFuture<Optional<Listing>> getListing(UUID listing) {
        return this.schedule(() -> this.implementation.getListing(listing));
    }

    @Override
    public CompletableFuture<Boolean> purge() {
        return this.schedule(this.implementation::purge);
    }

    @Override
    public CompletableFuture<Boolean> clean() {
        return this.schedule(this.implementation::clean);
    }

    @Override
    public CompletableFuture<Optional<PlayerSettings>> getPlayerSettings(UUID uuid) {
        return this.schedule(() -> this.implementation.getPlayerSettings(uuid));
    }

    @Override
    public CompletableFuture<Boolean> applyPlayerSettings(UUID uuid, PlayerSettings settings) {
        return this.schedule(() -> this.implementation.applyPlayerSettings(uuid, settings));
    }

    @Override
    public CompletableFuture<Boolean> hasMaxListings(UUID user) {
        return this.schedule(() -> this.implementation.hasMaxListings(user));
    }

    @Override
    public CompletableFuture<AuctionMessage.Bid.Response> processBid(AuctionMessage.Bid.Request request) {
        return this.schedule(() -> {
            ReentrantLock lock = this.locks.get(request.getAuctionID());

            try {
                lock.lock();
                return this.implementation.processBid(request);
            } finally {
                lock.unlock();
            }
        });
    }

    @Override
    public CompletableFuture<ClaimMessage.Response> processClaimRequest(ClaimMessage.Request request) {
        return this.schedule(() -> {
            ReentrantLock lock = this.locks.get(request.getListingID());

            try {
                lock.lock();
                return this.implementation.processClaimRequest(request);
            } finally {
                lock.unlock();
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> appendOldClaimStatus(UUID auction, boolean lister, boolean winner, List<UUID> others) {
        return this.schedule(() -> {
            ReentrantLock lock = this.locks.get(auction);

            try {
                lock.lock();
                return this.implementation.appendOldClaimStatus(auction, lister, winner, others);
            } finally {
                lock.unlock();
            }
        });
    }

    @Override
    public CompletableFuture<AuctionMessage.Cancel.Response> processAuctionCancelRequest(AuctionMessage.Cancel.Request request) {
        return this.schedule(() -> {
            ReentrantLock lock = this.locks.get(request.getAuctionID());

            try {
                lock.lock();
                return this.implementation.processAuctionCancelRequest(request);
            } finally {
                lock.unlock();
            }
        });
    }

    @Override
    public CompletableFuture<BuyItNowMessage.Remove.Response> processListingRemoveRequest(BuyItNowMessage.Remove.Request request) {
        return this.schedule(() -> {
            ReentrantLock lock = this.locks.get(request.getListingID());

            try {
                lock.lock();
                return this.implementation.processListingRemoveRequest(request);
            } finally {
                lock.unlock();
            }
        });
    }

    @Override
    public CompletableFuture<BuyItNowMessage.Purchase.Response> processPurchase(BuyItNowMessage.Purchase.Request request) {
        return this.schedule(() -> {
            ReentrantLock lock = this.locks.get(request.getListingID());

            try {
                lock.lock();
                return this.implementation.processPurchase(request);
            } finally {
                lock.unlock();
            }
        });
    }

    @Override
    public CompletableFuture<ClaimDelivery.Response> claimDelivery(ClaimDelivery.Request request) {
        return this.schedule(() -> {
            ReentrantLock lock = this.locks.get(request.getDeliveryID());

            try {
                lock.lock();
                return this.implementation.claimDelivery(request);
            } finally {
                lock.unlock();
            }
        });
    }

    @Override
    public CompletableFuture<ForceDeleteMessage.Response> processForcedDeletion(ForceDeleteMessage.Request request) {
        return this.schedule(() -> {
            ReentrantLock lock = this.locks.get(request.getListingID());

            try {
                lock.lock();
                return this.implementation.processForcedDeletion(request);
            } finally {
                lock.unlock();
            }
        });
    }

    public CompletableFuture<Boolean> sendListingUpdate(Listing listing) {
        return this.schedule(() -> this.implementation.sendListingUpdate(listing));
    }

    @Override
    public CompletableFuture<List<Listing>> fetchListings(Collection<Predicate<Listing>> filters) {
        return this.schedule(() -> {
            Stream<Listing> results = this.implementation.getListings().stream();
            for(Predicate<Listing> predicate : filters) {
                results = results.filter(predicate);
            }

            return results.collect(Collectors.toList());
        });
    }

    @Override
    public CompletableFuture<Boolean> sendDelivery(Delivery delivery) {
        return this.schedule(() -> this.implementation.sendDelivery(delivery));
    }

    @Override
    public CompletableFuture<Stash> getStash(UUID user) {
        return this.schedule(() -> this.implementation.getStash(user));
    }

    private <T> CompletableFuture<T> schedule(Callable<T> callable) {
        return CompletableFutureManager.makeFuture(callable, Impactor.getInstance().getScheduler().async());
    }
}
