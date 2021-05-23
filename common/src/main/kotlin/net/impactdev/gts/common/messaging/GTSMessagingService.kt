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

import com.google.common.collect.Maps
import com.google.common.collect.TreeMultimap
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import net.impactdev.gts.api.events.PingEvent
import net.impactdev.gts.api.listings.Listing
import net.impactdev.gts.api.messaging.IncomingMessageConsumer
import net.impactdev.gts.api.messaging.Messenger
import net.impactdev.gts.api.messaging.MessengerProvider
import net.impactdev.gts.api.messaging.message.OutgoingMessage
import net.impactdev.gts.api.messaging.message.errors.ErrorCode
import net.impactdev.gts.api.messaging.message.errors.ErrorCodes
import net.impactdev.gts.api.messaging.message.exceptions.MessagingException
import net.impactdev.gts.api.messaging.message.type.MessageType
import net.impactdev.gts.api.messaging.message.type.admin.ForceDeleteMessage
import net.impactdev.gts.api.messaging.message.type.auctions.AuctionMessage
import net.impactdev.gts.api.messaging.message.type.listings.BuyItNowMessage
import net.impactdev.gts.api.messaging.message.type.listings.BuyItNowMessage.Purchase
import net.impactdev.gts.api.messaging.message.type.listings.ClaimMessage
import net.impactdev.gts.api.messaging.message.type.utility.PingMessage
import net.impactdev.gts.api.util.PrettyPrinter
import net.impactdev.gts.api.util.ThrowingRunnable
import net.impactdev.gts.common.messaging.messages.admin.ForceDeleteMessageImpl.ForceDeleteRequest
import net.impactdev.gts.common.messaging.messages.listings.ClaimMessageImpl.ClaimRequestImpl
import net.impactdev.gts.common.messaging.messages.listings.ClaimMessageImpl.ClaimResponseImpl
import net.impactdev.gts.common.messaging.messages.listings.PublishListingMessageImpl
import net.impactdev.gts.common.messaging.messages.listings.auctions.impl.AuctionBidMessage
import net.impactdev.gts.common.messaging.messages.listings.auctions.impl.AuctionCancelMessage
import net.impactdev.gts.common.messaging.messages.listings.buyitnow.purchase.BINPurchaseMessage
import net.impactdev.gts.common.messaging.messages.listings.buyitnow.removal.BINRemoveMessage
import net.impactdev.gts.common.messaging.messages.utility.PingPongMessage
import net.impactdev.gts.common.plugin.GTSPlugin
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter
import net.impactdev.gts.common.utils.future.CompletableFutureManager
import net.impactdev.impactor.api.Impactor
import net.impactdev.impactor.api.json.factory.JObject
import java.time.Instant
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import java.util.function.BiFunction
import java.util.function.Consumer
import kotlin.collections.MutableMap
import kotlin.collections.get
import kotlin.collections.set

class GTSMessagingService(
    private val plugin: GTSPlugin,
    override val messengerProvider: MessengerProvider,
    consumer: IncomingMessageConsumer?
) : InternalMessagingService {
    override val messenger: Messenger
    private val decoders: MutableMap<String, BiFunction<JsonElement, UUID, out OutgoingMessage>> = Maps.newHashMap()
    override val name: String?
        get() = messengerProvider.name

    override fun close() {
        messenger.close()
    }

    override fun <T : OutgoingMessage?> registerDecoder(type: String, decoder: BiFunction<JsonElement, UUID, T>) {
        decoders[type] = decoder
    }

    override fun getDecoder(type: String?): BiFunction<JsonElement?, UUID?, out OutgoingMessage?>? {
        return decoders.get(type)
    }

    override fun generatePingID(): UUID {
        val uuid = UUID.randomUUID()
        messenger.messageConsumer!!.cacheReceivedID(uuid)
        return uuid
    }

    override fun sendPing(): CompletableFuture<PingMessage.Pong?>? {
        val debugger = PrettyPrinter(53).add("Ping/Pong Status").center().hr()
        val reference = AtomicReference<PingMessage.Ping>()
        val start = AtomicLong()
        return CompletableFutureManager.makeFuture {
            start.set(System.nanoTime())
            val ping: PingMessage.Ping = PingPongMessage.Ping(
                generatePingID()
            )
            reference.set(ping)
            Impactor.getInstance().eventBus.postAsync(PingEvent::class.java, reference.get().iD, Instant.now())
            val response = await<PingMessage.Ping, PingMessage.Pong>(ping)
            populate(debugger, reference.get(), response)
            response
        }.applyToEither(
            timeoutAfter<PingMessage.Pong>(5, TimeUnit.SECONDS)
        ) { pong: PingMessage.Pong? ->
            debugger.log(GTSPlugin.Companion.getInstance().getPluginLogger(), PrettyPrinter.Level.DEBUG)
            pong
        }.exceptionally { completion: Throwable ->
            val e = completion.cause
            val error: ErrorCode
            if (e is MessagingException) {
                error = e.error
            } else {
                error = ErrorCodes.FATAL_ERROR
                ExceptionWriter.write(e)
            }
            val request = reference.get()
            val response: PingMessage.Pong = PingPongMessage.Pong(UUID.randomUUID(), request.iD, false, error)
            val end = System.nanoTime()
            response.responseTime = TimeUnit.SECONDS.toMillis(
                if (error == ErrorCodes.REQUEST_TIMED_OUT) 5 else (end - start.get()) / 1000000000
            )
            populate(debugger, request, response)
            debugger.log(GTSPlugin.Companion.getInstance().getPluginLogger(), PrettyPrinter.Level.DEBUG)
            response
        }
    }

    override fun sendPublishNotice(listing: UUID, actor: UUID, auction: Boolean): CompletableFuture<Void?>? {
        return CompletableFutureManager.makeFuture(ThrowingRunnable {
            val message = PublishListingMessageImpl(generatePingID(), listing, actor, auction)
            GTSPlugin.Companion.getInstance().getMessagingService().getMessenger().sendOutgoingMessage(message)
        })
    }

    override fun publishBid(listing: UUID, actor: UUID, bid: Double): CompletableFuture<AuctionMessage.Bid.Response?>? {
        val debugger = PrettyPrinter(80).add("Bid Publishing Request").center().hr()
        val reference = AtomicReference<AuctionMessage.Bid.Request>()
        val start = AtomicLong()
        return CompletableFutureManager.makeFuture {
            start.set(System.nanoTime())
            val request: AuctionMessage.Bid.Request = AuctionBidMessage.Request(
                generatePingID(), listing, actor, bid
            )
            reference.set(request)
            val response = await<AuctionMessage.Bid.Request, AuctionMessage.Bid.Response>(request)
            populate(debugger, request, response)
            response
        }.applyToEither(
            timeoutAfter<AuctionMessage.Bid.Response>(5, TimeUnit.SECONDS)
        ) { response: AuctionMessage.Bid.Response? ->
            debugger.log(GTSPlugin.Companion.getInstance().getPluginLogger(), PrettyPrinter.Level.DEBUG)
            response
        }.exceptionally { completion: Throwable ->
            val e = completion.cause
            val error: ErrorCode
            if (e is MessagingException) {
                error = e.error
            } else {
                error = ErrorCodes.FATAL_ERROR
                ExceptionWriter.write(e)
            }
            val request = reference.get()
            val response: AuctionMessage.Bid.Response = AuctionBidMessage.Response(
                UUID.randomUUID(),
                request.iD,
                listing,
                actor,
                bid,
                false,
                Listing.SERVER_ID,
                TreeMultimap.create(),
                error
            )
            val end = System.nanoTime()
            response.responseTime = TimeUnit.SECONDS.toMillis(
                if (error == ErrorCodes.REQUEST_TIMED_OUT) 5 else (end - start.get()) / 1000000000
            )
            populate(debugger, request, response)
            debugger.log(GTSPlugin.Companion.getInstance().getPluginLogger(), PrettyPrinter.Level.DEBUG)
            response
        }
    }

    override fun requestAuctionCancellation(
        listing: UUID,
        actor: UUID
    ): CompletableFuture<AuctionMessage.Cancel.Response?>? {
        val debugger = PrettyPrinter(53).add("Auction Cancellation Request").center().hr()
        val reference = AtomicReference<AuctionMessage.Cancel.Request>()
        val start = AtomicLong()
        return CompletableFutureManager.makeFuture {
            start.set(System.nanoTime())
            val request: AuctionMessage.Cancel.Request = AuctionCancelMessage.Request(
                generatePingID(), listing, actor
            )
            reference.set(request)
            val response = await<AuctionMessage.Cancel.Request, AuctionMessage.Cancel.Response>(request)
            populate(debugger, request, response)
            response
        }.applyToEither(
            timeoutAfter<AuctionMessage.Cancel.Response>(5, TimeUnit.SECONDS)
        ) { response: AuctionMessage.Cancel.Response? ->
            debugger.log(GTSPlugin.Companion.getInstance().getPluginLogger(), PrettyPrinter.Level.DEBUG)
            response
        }.exceptionally { completion: Throwable ->
            val e = completion.cause
            val error: ErrorCode
            if (e is MessagingException) {
                error = e.error
            } else {
                error = ErrorCodes.FATAL_ERROR
                ExceptionWriter.write(e)
            }
            val request = reference.get()
            val response: AuctionMessage.Cancel.Response? = null
            val end = System.nanoTime()
            response!!.responseTime = TimeUnit.SECONDS.toMillis(
                if (error == ErrorCodes.REQUEST_TIMED_OUT) 5 else (end - start.get()) / 1000000000
            )
            populate(debugger, request, response)
            debugger.log(GTSPlugin.Companion.getInstance().getPluginLogger(), PrettyPrinter.Level.DEBUG)
            response
        }
    }

    override fun requestClaim(
        listing: UUID,
        actor: UUID,
        receiver: UUID?,
        auction: Boolean
    ): CompletableFuture<ClaimMessage.Response?>? {
        val debugger = PrettyPrinter(53).add("Claim Request").center().hr()
        val reference = AtomicReference<ClaimMessage.Request>()
        val start = AtomicLong()
        return CompletableFutureManager.makeFuture {
            start.set(System.nanoTime())
            val request = ClaimRequestImpl(generatePingID(), listing, actor, receiver, auction)
            reference.set(request)
            val response = await<ClaimRequestImpl, ClaimMessage.Response>(request)
            populate(debugger, request, response)
            response
        }.applyToEither(
            timeoutAfter<ClaimMessage.Response>(5, TimeUnit.SECONDS)
        ) { response: ClaimMessage.Response? ->
            debugger.log(GTSPlugin.Companion.getInstance().getPluginLogger(), PrettyPrinter.Level.DEBUG)
            response
        }.exceptionally { completion: Throwable ->
            val e = completion.cause
            val error: ErrorCode
            if (e is MessagingException) {
                error = e.error
            } else {
                error = ErrorCodes.FATAL_ERROR
                ExceptionWriter.write(e)
            }
            val request = reference.get()
            val response: ClaimMessage.Response = ClaimResponseImpl.Companion.builder()
                .id(UUID.randomUUID())
                .request(request.iD)
                .listing(request.listingID)
                .actor(request.actor)
                .receiver(request.receiver!!.orElse(null))
                .error(error)
                .build()
            val end = System.nanoTime()
            response.responseTime = TimeUnit.SECONDS.toMillis(
                if (error == ErrorCodes.REQUEST_TIMED_OUT) 5 else (end - start.get()) / 1000000000
            )
            populate(debugger, request, response)
            debugger.log(GTSPlugin.Companion.getInstance().getPluginLogger(), PrettyPrinter.Level.DEBUG)
            response
        }
    }

    override fun requestBINPurchase(
        listing: UUID?,
        actor: UUID?,
        source: Any?
    ): CompletableFuture<Purchase.Response?>? {
        val debugger = PrettyPrinter(53).add("BIN Purchase Request").center().hr()
        val reference = AtomicReference<Purchase.Request>()
        val start = AtomicLong()
        return CompletableFutureManager.makeFuture {
            start.set(System.nanoTime())
            val request: Purchase.Request = BINPurchaseMessage.Request(
                generatePingID(), listing, actor
            )
            reference.set(request)
            val response = await<Purchase.Request, Purchase.Response>(request)
            populate(debugger, request, response)
            response
        }.applyToEither(
            timeoutAfter<Purchase.Response>(5, TimeUnit.SECONDS)
        ) { response: Purchase.Response? ->
            debugger.log(GTSPlugin.Companion.getInstance().getPluginLogger(), PrettyPrinter.Level.DEBUG)
            response
        }.exceptionally { completion: Throwable ->
            val e = completion.cause
            val error: ErrorCode
            if (e is MessagingException) {
                error = e.error
            } else {
                error = ErrorCodes.FATAL_ERROR
                ExceptionWriter.write(e)
            }
            val request = reference.get()
            val response: Purchase.Response = BINPurchaseMessage.Response(
                UUID.randomUUID(), request.iD, listing, actor, Listing.SERVER_ID, false, error
            )
            val end = System.nanoTime()
            response.responseTime = TimeUnit.SECONDS.toMillis(
                if (error == ErrorCodes.REQUEST_TIMED_OUT) 5 else (end - start.get()) / 1000000000
            )
            populate(debugger, request, response)
            debugger.log(GTSPlugin.Companion.getInstance().getPluginLogger(), PrettyPrinter.Level.DEBUG)
            response
        }
    }

    override fun requestBINRemoveRequest(
        listing: UUID?,
        actor: UUID?,
        receiver: UUID?,
        shouldReceive: Boolean
    ): CompletableFuture<BuyItNowMessage.Remove.Response?>? {
        val debugger = PrettyPrinter(53).add("Buy It Now Removal Request").center().hr()
        val reference = AtomicReference<BuyItNowMessage.Remove.Request>()
        val start = AtomicLong()
        return CompletableFutureManager.makeFuture {
            start.set(System.nanoTime())
            val request: BuyItNowMessage.Remove.Request = BINRemoveMessage.Request(
                generatePingID(), listing, actor, receiver, shouldReceive
            )
            reference.set(request)
            val response = await<BuyItNowMessage.Remove.Request, BuyItNowMessage.Remove.Response>(request)
            populate(debugger, request, response)
            response
        }.applyToEither(
            timeoutAfter<BuyItNowMessage.Remove.Response>(5, TimeUnit.SECONDS)
        ) { response: BuyItNowMessage.Remove.Response? ->
            debugger.log(GTSPlugin.Companion.getInstance().getPluginLogger(), PrettyPrinter.Level.DEBUG)
            response
        }.exceptionally { completion: Throwable ->
            val e = completion.cause
            val error: ErrorCode
            if (e is MessagingException) {
                error = e.error
            } else {
                error = ErrorCodes.FATAL_ERROR
                ExceptionWriter.write(e)
            }
            val request = reference.get()
            val response: BuyItNowMessage.Remove.Response = BINRemoveMessage.Response(
                UUID.randomUUID(), request.iD, listing, actor, receiver, shouldReceive, false, error
            )
            val end = System.nanoTime()
            response.responseTime = TimeUnit.SECONDS.toMillis(
                if (error == ErrorCodes.REQUEST_TIMED_OUT) 5 else (end - start.get()) / 1000000000
            )
            populate(debugger, request, response)
            debugger.log(GTSPlugin.Companion.getInstance().getPluginLogger(), PrettyPrinter.Level.DEBUG)
            response
        }
    }

    override fun requestForcedDeletion(
        listing: UUID,
        actor: UUID,
        give: Boolean
    ): CompletableFuture<ForceDeleteMessage.Response?>? {
        val debugger = PrettyPrinter(53).add("Admin - Forced Deletion").center().hr()
        val reference = AtomicReference<ForceDeleteMessage.Request>()
        val start = AtomicLong()
        return CompletableFutureManager.makeFuture {
            start.set(System.nanoTime())
            val request: ForceDeleteMessage.Request = ForceDeleteRequest(
                generatePingID(), listing, actor, give
            )
            reference.set(request)
            val response = await<ForceDeleteMessage.Request, ForceDeleteMessage.Response>(request)
            populate(debugger, request, response)
            response
        }.applyToEither(
            timeoutAfter<ForceDeleteMessage.Response>(5, TimeUnit.SECONDS)
        ) { response: ForceDeleteMessage.Response? ->
            debugger.log(GTSPlugin.Companion.getInstance().getPluginLogger(), PrettyPrinter.Level.DEBUG)
            response
        }.exceptionally { completion: Throwable ->
            val e = completion.cause
            val error: ErrorCode
            if (e is MessagingException) {
                error = e.error
            } else {
                error = ErrorCodes.FATAL_ERROR
                ExceptionWriter.write(e)
            }
            val request = reference.get()
            val response = ForceDeleteMessage.Response.builder()
                .request(request.iD)
                .listing(request.listingID)
                .actor(request.actor)
                .data(null)
                .successful(false)
                .error(error)
                .build()
            val end = System.nanoTime()
            response!!.responseTime = TimeUnit.SECONDS.toMillis(
                if (error == ErrorCodes.REQUEST_TIMED_OUT) 5 else (end - start.get()) / 1000000000
            )
            populate(debugger, request, response)
            debugger.log(GTSPlugin.Companion.getInstance().getPluginLogger(), PrettyPrinter.Level.DEBUG)
            response
        }
    }

    private fun populate(printer: PrettyPrinter, request: MessageType.Request<*>, response: MessageType.Response?) {
        printer.add()
            .add("Request Information:")
            .hr('-')
            .add(request)
            .add()
            .hr('=')
            .add()
            .add("Response Information:")
            .hr('-')
            .add(response!!)
            .add()
            .consume(Consumer { printer: PrettyPrinter? ->
                response.finalizeReport(
                    printer!!
                )
            })
            .add()
    }

    companion object {
        @kotlin.jvm.JvmField
        val NORMAL = GsonBuilder().disableHtmlEscaping().create()
        fun encodeMessageAsString(type: String?, id: UUID, content: JsonElement?): String {
            val json = JObject()
                .add("id", id.toString())
                .add("type", type)
                .consume { o: JObject ->
                    if (content != null) {
                        o.add("content", content)
                    }
                }
                .toJson()
            return NORMAL.toJson(json)
        }
    }

    init {
        messenger = messengerProvider.obtain(consumer!!)
        Objects.requireNonNull(messenger, "messenger")
    }
}