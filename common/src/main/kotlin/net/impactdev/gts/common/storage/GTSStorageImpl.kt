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
package net.impactdev.gts.common.storage

import com.github.benmanes.caffeine.cache.CacheLoader
import com.github.benmanes.caffeine.cache.Caffeine
import net.impactdev.gts.api.listings.Listing
import net.impactdev.gts.api.messaging.message.type.admin.ForceDeleteMessage
import net.impactdev.gts.api.messaging.message.type.auctions.AuctionMessage
import net.impactdev.gts.api.messaging.message.type.listings.BuyItNowMessage
import net.impactdev.gts.api.messaging.message.type.listings.BuyItNowMessage.Purchase
import net.impactdev.gts.api.messaging.message.type.listings.ClaimMessage
import net.impactdev.gts.api.player.PlayerSettings
import net.impactdev.gts.api.stashes.Stash
import net.impactdev.gts.api.storage.GTSStorage
import net.impactdev.gts.common.plugin.GTSPlugin
import net.impactdev.gts.common.storage.implementation.StorageImplementation
import net.impactdev.gts.common.utils.future.CompletableFutureManager
import net.impactdev.impactor.api.Impactor
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.locks.ReentrantLock
import java.util.function.Predicate
import java.util.stream.Collectors

class GTSStorageImpl(private val plugin: GTSPlugin, private val implementation: StorageImplementation) : GTSStorage {
    private val locks = Caffeine.newBuilder()
        .expireAfterAccess(5, TimeUnit.MINUTES)
        .build<UUID?, ReentrantLock>(CacheLoader<UUID, ReentrantLock?> { ReentrantLock() })

    /**
     * Attempts to initialize the storage implementation
     */
    override fun init() {
        try {
            implementation.init()
        } catch (e: Exception) {
            // Log the failure
            throw RuntimeException(e)
        }
    }

    /**
     * Attempts to shutdown the storage implementation
     */
    override fun shutdown() {
        try {
            implementation.shutdown()
        } catch (e: Exception) {
            // Log the failure
            throw RuntimeException(e)
        }
    }

    /**
     * Represents any properties which might be set against a storage
     * implementation.
     *
     * @return A mapping of flags to values representing storage implementation
     * properties
     */
    override val meta: CompletableFuture<Map<String, String>?>?
        get() = schedule<Map<String, String>?>(Callable<Map<String?, String?>?> { implementation.meta })

    override fun publishListing(listing: Listing?): CompletableFuture<Boolean?>? {
        return schedule(Callable { implementation.addListing(listing) })
    }

    override fun getListing(listing: UUID?): CompletableFuture<Optional<Listing?>?>? {
        return schedule(Callable { implementation.getListing(listing) })
    }

    override fun purge(): CompletableFuture<Boolean?>? {
        return schedule(Callable { implementation.purge() })
    }

    override fun clean(): CompletableFuture<Boolean?>? {
        return schedule(Callable { implementation.clean() })
    }

    override fun getPlayerSettings(uuid: UUID?): CompletableFuture<Optional<PlayerSettings?>?>? {
        return schedule(Callable { implementation.getPlayerSettings(uuid) })
    }

    override fun applyPlayerSettings(uuid: UUID?, settings: PlayerSettings?): CompletableFuture<Boolean?>? {
        return schedule(Callable { implementation.applyPlayerSettings(uuid, settings) })
    }

    override fun hasMaxListings(user: UUID?): CompletableFuture<Boolean?>? {
        return schedule(Callable { implementation.hasMaxListings(user) })
    }

    override fun processBid(request: AuctionMessage.Bid.Request?): CompletableFuture<AuctionMessage.Bid.Response?>? {
        return schedule(Callable<AuctionMessage.Bid.Response?> {
            val lock = locks[request!!.auctionID]
            try {
                lock!!.lock()
                return@schedule implementation.processBid(request)
            } finally {
                lock!!.unlock()
            }
        })
    }

    override fun processClaimRequest(request: ClaimMessage.Request?): CompletableFuture<ClaimMessage.Response?>? {
        return schedule(Callable<ClaimMessage.Response?> {
            val lock = locks[request!!.listingID]
            try {
                lock!!.lock()
                return@schedule implementation.processClaimRequest(request)
            } finally {
                lock!!.unlock()
            }
        })
    }

    override fun appendOldClaimStatus(
        auction: UUID?,
        lister: Boolean,
        winner: Boolean,
        others: List<UUID?>?
    ): CompletableFuture<Boolean?>? {
        return schedule(Callable<Boolean?> {
            val lock = locks[auction!!]
            try {
                lock!!.lock()
                return@schedule implementation.appendOldClaimStatus(auction, lister, winner, others)
            } finally {
                lock!!.unlock()
            }
        })
    }

    override fun processAuctionCancelRequest(request: AuctionMessage.Cancel.Request?): CompletableFuture<AuctionMessage.Cancel.Response?>? {
        return schedule(Callable<AuctionMessage.Cancel.Response?> {
            val lock = locks[request!!.auctionID]
            try {
                lock!!.lock()
                return@schedule implementation.processAuctionCancelRequest(request)
            } finally {
                lock!!.unlock()
            }
        })
    }

    override fun processListingRemoveRequest(request: BuyItNowMessage.Remove.Request?): CompletableFuture<BuyItNowMessage.Remove.Response?>? {
        return schedule(
            Callable<BuyItNowMessage.Remove.Response?> {
                val lock = locks[request!!.listingID!!]
                try {
                    lock!!.lock()
                    return@schedule implementation.processListingRemoveRequest(request)
                } finally {
                    lock!!.unlock()
                }
            })
    }

    override fun processPurchase(request: Purchase.Request?): CompletableFuture<Purchase.Response?>? {
        return schedule(
            Callable<Purchase.Response?> {
                val lock = locks[request!!.listingID!!]
                try {
                    lock!!.lock()
                    return@schedule implementation.processPurchase(request)
                } finally {
                    lock!!.unlock()
                }
            })
    }

    override fun processForcedDeletion(request: ForceDeleteMessage.Request?): CompletableFuture<ForceDeleteMessage.Response?>? {
        return schedule(Callable<ForceDeleteMessage.Response?> {
            val lock = locks[request!!.listingID]
            try {
                lock!!.lock()
                return@schedule implementation.processForcedDeletion(request)
            } finally {
                lock!!.unlock()
            }
        })
    }

    fun sendListingUpdate(listing: Listing): CompletableFuture<Boolean>? {
        return schedule(Callable { implementation.sendListingUpdate(listing) })
    }

    override fun fetchListings(filters: Collection<Predicate<Listing?>?>?): CompletableFuture<List<Listing?>?>? {
        return schedule<List<Listing?>?>(Callable<List<Listing>?> {
            var results = implementation.listings.stream()
            for (predicate in filters!!) {
                results = results.filter(predicate)
            }
            results.collect(Collectors.toList())
        })
    }

    override fun getStash(user: UUID?): CompletableFuture<Stash?>? {
        return schedule(Callable { implementation.getStash(user) })
    }

    private fun <T> schedule(callable: Callable<T>): CompletableFuture<T?>? {
        return CompletableFutureManager.makeFuture(callable, Impactor.getInstance().scheduler.async())
    }
}