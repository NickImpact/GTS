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
package net.impactdev.gts.common.messaging

import com.google.gson.JsonElement
import net.impactdev.gts.api.messaging.Messenger
import net.impactdev.gts.api.messaging.MessengerProvider
import net.impactdev.gts.api.messaging.message.OutgoingMessage
import net.impactdev.gts.api.messaging.message.errors.ErrorCodes
import net.impactdev.gts.api.messaging.message.exceptions.MessagingException
import net.impactdev.gts.api.messaging.message.type.MessageType
import net.impactdev.gts.api.messaging.message.type.admin.ForceDeleteMessage
import net.impactdev.gts.api.messaging.message.type.auctions.AuctionMessage
import net.impactdev.gts.api.messaging.message.type.listings.BuyItNowMessage
import net.impactdev.gts.api.messaging.message.type.listings.BuyItNowMessage.Purchase
import net.impactdev.gts.api.messaging.message.type.listings.ClaimMessage
import net.impactdev.gts.api.messaging.message.type.utility.PingMessage
import net.impactdev.gts.common.plugin.GTSPlugin
import net.impactdev.gts.common.utils.future.CompletableFutureManager
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicReference
import java.util.function.BiFunction
import java.util.function.Consumer

interface InternalMessagingService {
    /**
     * Gets the name of this messaging service
     *
     * @return the name of this messaging service
     */
    val name: String?
    val messenger: Messenger
    val messengerProvider: MessengerProvider

    /**
     * Closes the messaging service
     */
    fun close()
    fun <T : OutgoingMessage?> registerDecoder(type: String, decoder: BiFunction<JsonElement, UUID, T>)
    fun getDecoder(type: String?): BiFunction<JsonElement?, UUID?, out OutgoingMessage?>?

    /**
     * Generates a ping ID that'll represent the message being sent across the servers.
     *
     * @return The ID of the message that is being sent
     */
    fun generatePingID(): UUID

    /**
     * Forces a completable future to timeout its actions after the specified amount of time. This is best used
     * with [acceptEither][CompletableFuture.acceptEither],
     * [applyToEither][CompletableFuture.applyToEither], or any of their respective
     * async companions.
     *
     * @param timeout The amount of time that it should take before we forcibly raise a timeout exception
     * @param unit The time unit to measure our timeout value by
     * @param <W> The intended return type of the completable future (for compatibility with both run and supply)
     * @return A completable future who's sole purpose is to timeout after X amount of time
    </W> */
    fun <W> timeoutAfter(timeout: Long, unit: TimeUnit?): CompletableFuture<W?>? {
        return CompletableFutureManager.makeFutureDelayed({
            throw MessagingException(
                ErrorCodes.REQUEST_TIMED_OUT,
                TimeoutException()
            )
        }, timeout, unit)
    }

    /**
     * More of a utility method, this method processes a request by placing it in the receiver queue for the hooked
     * incoming message receiver. It'll hold the active thread until a response has been received. Once received,
     * it is to return that value.
     *
     * @param request The request being made
     * @param <R> The type of request being made
     * @param <W> The intended return type
     * @return The response as soon as it's available
    </W></R> */
    fun <R, W : MessageType.Response?> await(request: R): W? where R : MessageType.Request<*>?, R : OutgoingMessage? {
        val reference = AtomicReference<W?>(null)
        val start = System.nanoTime()
        GTSPlugin.Companion.getInstance().getMessagingService().getMessenger().messageConsumer.registerRequest(
            request!!.iD,
            Consumer { newValue: W -> reference.set(newValue) })
        GTSPlugin.Companion.getInstance().getMessagingService().getMessenger().sendOutgoingMessage(request)
        while (reference.get() == null) {
        }
        val finish = System.nanoTime()
        return reference.updateAndGet { response: W? ->
            response!!.responseTime = (finish - start) / 1000000
            response
        }
    }
    //------------------------------------------------------------------------------------
    //
    //  General Plugin Messages
    //
    //------------------------------------------------------------------------------------
    /**
     * Sends a ping to the proxy controller. The proxy will then respond with a pong message, assuming the message
     * is properly processed. This message will route between all servers due to its nature, but will only
     * be parsed by the server that meets the requirements of the pong message. Those being the server
     * address and port for which they were attached at the time of the message being sent.
     */
    fun sendPing(): CompletableFuture<PingMessage.Pong?>?

    /**
     * Sends an update to the messaging service indicating a new listing has been published.
     *
     * @param listing The ID of the listing just published
     * @param actor The ID of the user who published the listing
     * @param auction Whether the listing is an auction or BIN listing
     * @return An empty value, likely just null
     */
    fun sendPublishNotice(listing: UUID, actor: UUID, auction: Boolean): CompletableFuture<Void?>?
    //------------------------------------------------------------------------------------
    //
    //  Auction Based Messages
    //
    //------------------------------------------------------------------------------------
    /**
     * Attempts to publish a bid to the central database for GTS. This message simply controls the process
     * of sending the initial message. Afterwords, the proxy handling the message will respond with a
     * [Bid Response][AuctionMessage.Bid.Response] that'll specify all other required information
     * regarding the bid.
     *
     * @param listing The listing being bid on
     * @param actor   The user who placed the bid
     * @param bid     The amount the user has just bid on the auction for
     * @return A completable future wrapping a response message for a users bid request
     */
    fun publishBid(listing: UUID, actor: UUID, bid: Double): CompletableFuture<AuctionMessage.Bid.Response?>?

    /**
     *
     *
     * @param listing
     * @param actor
     */
    fun requestAuctionCancellation(listing: UUID, actor: UUID): CompletableFuture<AuctionMessage.Cancel.Response?>?

    /**
     *
     *
     * @param listing
     * @param actor
     * @param receiver
     * @param auction
     */
    fun requestClaim(
        listing: UUID,
        actor: UUID,
        receiver: UUID?,
        auction: Boolean
    ): CompletableFuture<ClaimMessage.Response?>?

    //------------------------------------------------------------------------------------
    //
    //  BuyItNow Based Messages
    //
    //------------------------------------------------------------------------------------
    fun requestBINPurchase(listing: UUID?, actor: UUID?, source: Any?): CompletableFuture<Purchase.Response?>?

    /**
     *
     *
     * @param listing
     * @param actor
     * @return
     */
    fun requestBINRemoveRequest(listing: UUID?, actor: UUID?): CompletableFuture<BuyItNowMessage.Remove.Response?>? {
        return this.requestBINRemoveRequest(listing, actor, null, true)
    }

    fun requestBINRemoveRequest(
        listing: UUID?,
        actor: UUID?,
        receiver: UUID?,
        shouldReceive: Boolean
    ): CompletableFuture<BuyItNowMessage.Remove.Response?>?

    fun requestForcedDeletion(
        listing: UUID,
        actor: UUID,
        give: Boolean
    ): CompletableFuture<ForceDeleteMessage.Response?>?
}