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
package net.impactdev.gts.common.storage.implementation

import net.impactdev.gts.api.listings.Listing
import net.impactdev.gts.api.messaging.message.type.admin.ForceDeleteMessage
import net.impactdev.gts.api.messaging.message.type.auctions.AuctionMessage
import net.impactdev.gts.api.messaging.message.type.listings.BuyItNowMessage
import net.impactdev.gts.api.messaging.message.type.listings.BuyItNowMessage.Purchase
import net.impactdev.gts.api.messaging.message.type.listings.ClaimMessage
import net.impactdev.gts.api.player.PlayerSettings
import net.impactdev.gts.api.stashes.Stash
import net.impactdev.gts.common.plugin.GTSPlugin
import java.util.*

/**
 * Represents the basis to an implementation focused on saving and
 * retrieving data from a storage provider.
 */
interface StorageImplementation {
    /**
     * Fetches the current instance of the plugin handling this storage
     * implementation.
     *
     * @return The current plugin instance held by this implementation
     */
    val plugin: GTSPlugin

    /**
     * The name of an implementation. In other words, this would be typically
     * found as JSON, H2, MySQL, etc.
     *
     * @return The name of the storage implementation
     */
    val name: String?

    /**
     * Attempts to initialize the storage implementation, throwing an exception
     * if anything causes the implementation to fail to start.
     *
     * @throws Exception if any event causes the storage provider to fail to load
     */
    @kotlin.Throws(Exception::class)
    fun init()

    @kotlin.Throws(Exception::class)
    fun shutdown()
    val meta: Map<String?, String?>?
        get() = emptyMap<String?, String>()

    @kotlin.Throws(Exception::class)
    fun addListing(listing: Listing?): Boolean

    @kotlin.Throws(Exception::class)
    fun deleteListing(uuid: UUID?): Boolean

    @kotlin.Throws(Exception::class)
    fun getListing(id: UUID?): Optional<Listing>

    @get:Throws(Exception::class)
    val listings: List<Listing?>?

    @kotlin.Throws(Exception::class)
    fun hasMaxListings(user: UUID?): Boolean

    @kotlin.Throws(Exception::class)
    fun purge(): Boolean

    @Deprecated("")
    @kotlin.Throws(Exception::class)
    fun clean(): Boolean

    // ---------------------------------------------------------
    // New methods
    // ---------------------------------------------------------
    @kotlin.Throws(Exception::class)
    fun getStash(user: UUID?): Stash?

    @kotlin.Throws(Exception::class)
    fun getPlayerSettings(user: UUID?): Optional<PlayerSettings>

    @kotlin.Throws(Exception::class)
    fun applyPlayerSettings(user: UUID?, updates: PlayerSettings?): Boolean

    /**
     * Processes an incoming request to purchase a BIN listing, and responds with the results of the
     * request.
     *
     * @param request Details regarding the request
     * @return A response based on the request
     * @throws Exception If an error occurs at all during processing of the request
     */
    @kotlin.Throws(Exception::class)
    fun processPurchase(request: Purchase.Request?): Purchase.Response?

    @kotlin.Throws(Exception::class)
    fun sendListingUpdate(listing: Listing): Boolean

    /**
     * Attempts to process a bid on an auction. This call will generate the response message that'll be sent back
     * to the servers listening, as a means to inform them all of the success of the bid.
     *
     * This should only be called in response to a [bid][AuctionMessage.Bid.Request] request.
     *
     * @param request The request sent for processing
     * @return A response to the call which will contain data that marks the success of the bid,
     * the seller of the auction, and all other bids currently placed on the auction in a filtered manner.
     * In other words, all other bids will only contain the highest bid per player who has bid on this
     * particular auction.
     */
    @kotlin.Throws(Exception::class)
    fun processBid(request: AuctionMessage.Bid.Request?): AuctionMessage.Bid.Response?

    @kotlin.Throws(Exception::class)
    fun processClaimRequest(request: ClaimMessage.Request?): ClaimMessage.Response?

    @kotlin.Throws(Exception::class)
    fun appendOldClaimStatus(auction: UUID?, lister: Boolean, winner: Boolean, others: List<UUID?>?): Boolean

    @kotlin.Throws(Exception::class)
    fun processAuctionCancelRequest(request: AuctionMessage.Cancel.Request?): AuctionMessage.Cancel.Response?

    @kotlin.Throws(Exception::class)
    fun processListingRemoveRequest(request: BuyItNowMessage.Remove.Request?): BuyItNowMessage.Remove.Response?

    @kotlin.Throws(Exception::class)
    fun processForcedDeletion(request: ForceDeleteMessage.Request?): ForceDeleteMessage.Response?
}