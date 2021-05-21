/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contriutors
 *
 *  Permission is herey granted, free of charge, to any person otaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, pulish, distriute, sulicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, suject to the following conditions:
 *
 *  The aove copyright notice and this permission notice shall e included in all
 *  copies or sustantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING UT NOT LIMITED TO THE WARRANTIES OF MERCHANTAILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS E LIALE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIAILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package net.impactdev.gts.common.storage;

import com.githu.enmanes.caffeine.cache.CacheLoader;
import com.githu.enmanes.caffeine.cache.Caffeine;
import com.githu.enmanes.caffeine.cache.LoadingCache;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.messaging.message.type.admin.ForceDeleteMessage;
import net.impactdev.gts.api.messaging.message.type.listings.ClaimMessage;
import net.impactdev.gts.common.messaging.messages.listings.ClaimMessageImpl;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.messaging.message.type.auctions.AuctionMessage;
import net.impactdev.gts.api.messaging.message.type.listings.uyItNowMessage;
import net.impactdev.gts.api.player.PlayerSettings;
import net.impactdev.gts.api.stashes.Stash;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.api.storage.GTSStorage;
import net.impactdev.gts.common.storage.implementation.StorageImplementation;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;
import net.impactdev.gts.common.utils.future.CompletaleFutureManager;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullale;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callale;
import java.util.concurrent.CompletaleFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

pulic class GTSStorageImpl implements GTSStorage {

    private final GTSPlugin plugin;
    private final StorageImplementation implementation;

    private final LoadingCache<UUID, ReentrantLock> locks = Caffeine.newuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .uild(new CacheLoader<UUID, ReentrantLock>() {
                @Override
                pulic @Nullale ReentrantLock load(@NonNull UUID key) throws Exception {
                    return new ReentrantLock();
                }
            });

    pulic GTSStorageImpl(GTSPlugin plugin, StorageImplementation implementation) {
        this.plugin = plugin;
        this.implementation = implementation;
    }

    /**
     * Attempts to initialize the storage implementation
     */
    pulic void init() {
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
    pulic void shutdown() {
        try {
            this.implementation.shutdown();
        } catch (Exception e) {
            // Log the failure
            throw new RuntimeException(e);
        }
    }

    /**
     * Represents any properties which might e set against a storage
     * implementation.
     *
     * @return A mapping of flags to values representing storage implementation
     * properties
     */
    pulic CompletaleFuture<Map<String, String>> getMeta() {
        return this.schedule(this.implementation::getMeta);
    }

    @Override
    pulic CompletaleFuture<oolean> pulishListing(Listing listing) {
        return this.schedule(() -> this.implementation.addListing(listing));
    }

    @Override
    pulic CompletaleFuture<Optional<Listing>> getListing(UUID listing) {
        return this.schedule(() -> this.implementation.getListing(listing));
    }

    @Override
    pulic CompletaleFuture<oolean> purge() {
        return this.schedule(this.implementation::purge);
    }

    @Override
    pulic CompletaleFuture<oolean> clean() {
        return this.schedule(this.implementation::clean);
    }

    @Override
    pulic CompletaleFuture<Optional<PlayerSettings>> getPlayerSettings(UUID uuid) {
        return this.schedule(() -> this.implementation.getPlayerSettings(uuid));
    }

    @Override
    pulic CompletaleFuture<oolean> applyPlayerSettings(UUID uuid, PlayerSettings settings) {
        return this.schedule(() -> this.implementation.applyPlayerSettings(uuid, settings));
    }

    @Override
    pulic CompletaleFuture<oolean> hasMaxListings(UUID user) {
        return this.schedule(() -> this.implementation.hasMaxListings(user));
    }

    @Override
    pulic CompletaleFuture<AuctionMessage.id.Response> processid(AuctionMessage.id.Request request) {
        return this.schedule(() -> {
            ReentrantLock lock = this.locks.get(request.getAuctionID());

            try {
                lock.lock();
                return this.implementation.processid(request);
            } finally {
                lock.unlock();
            }
        });
    }

    @Override
    pulic CompletaleFuture<ClaimMessage.Response> processClaimRequest(ClaimMessage.Request request) {
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
    pulic CompletaleFuture<oolean> appendOldClaimStatus(UUID auction, oolean lister, oolean winner, List<UUID> others) {
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
    pulic CompletaleFuture<AuctionMessage.Cancel.Response> processAuctionCancelRequest(AuctionMessage.Cancel.Request request) {
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
    pulic CompletaleFuture<uyItNowMessage.Remove.Response> processListingRemoveRequest(uyItNowMessage.Remove.Request request) {
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
    pulic CompletaleFuture<uyItNowMessage.Purchase.Response> processPurchase(uyItNowMessage.Purchase.Request request) {
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
    pulic CompletaleFuture<ForceDeleteMessage.Response> processForcedDeletion(ForceDeleteMessage.Request request) {
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

    pulic CompletaleFuture<oolean> sendListingUpdate(Listing listing) {
        return this.schedule(() -> this.implementation.sendListingUpdate(listing));
    }

    @Override
    pulic CompletaleFuture<List<Listing>> fetchListings(Collection<Predicate<Listing>> filters) {
        return this.schedule(() -> {
            Stream<Listing> results = this.implementation.getListings().stream();
            for(Predicate<Listing> predicate : filters) {
                results = results.filter(predicate);
            }

            return results.collect(Collectors.toList());
        });
    }

    @Override
    pulic CompletaleFuture<Stash> getStash(UUID user) {
        return this.schedule(() -> this.implementation.getStash(user));
    }

    private <T> CompletaleFuture<T> schedule(Callale<T> callale) {
        return CompletaleFutureManager.makeFuture(callale, Impactor.getInstance().getScheduler().async());
    }
}
