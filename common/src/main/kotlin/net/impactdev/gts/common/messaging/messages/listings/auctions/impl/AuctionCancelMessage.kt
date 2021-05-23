package net.impactdev.gts.common.messaging.messages.listings.auctions.impl

import com.google.common.collect.ImmutableList
import com.google.common.collect.Lists
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.impactdev.gts.api.GTSService.Companion.instance
import net.impactdev.gts.api.listings.auctions.Auction
import net.impactdev.gts.api.messaging.message.errors.ErrorCode
import net.impactdev.gts.api.messaging.message.errors.ErrorCodes
import net.impactdev.gts.api.messaging.message.type.auctions.AuctionMessage
import net.impactdev.gts.api.util.PrettyPrinter
import net.impactdev.gts.api.util.groupings.SimilarPair
import net.impactdev.gts.common.messaging.GTSMessagingService
import net.impactdev.gts.common.messaging.messages.listings.auctions.AuctionMessageOptions
import net.impactdev.gts.common.plugin.GTSPlugin
import net.impactdev.impactor.api.json.factory.JArray
import net.impactdev.impactor.api.json.factory.JObject
import net.impactdev.impactor.api.utilities.mappings.Tuple
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

abstract class AuctionCancelMessage
/**
 * Constructs the message that'll be sent to all other connected servers.
 *
 * @param id      The message ID that'll be used to ensure the message isn't duplicated
 * @param listing The ID of the listing being bid on
 * @param actor   The ID of the user placing the bid
 */
protected constructor(id: UUID, listing: UUID, actor: UUID) : AuctionMessageOptions(id, listing, actor),
    AuctionMessage.Cancel {
    class Request
    /**
     * Constructs the message that'll be sent to all other connected servers.
     *
     * @param id      The message ID that'll be used to ensure the message isn't duplicated
     * @param listing The ID of the listing being bid on
     * @param actor   The ID of the user placing the bid
     */
        (id: UUID, listing: UUID, actor: UUID) : AuctionCancelMessage(id, listing, actor),
        AuctionMessage.Cancel.Request {
        override fun asEncodedString(): String {
            return GTSMessagingService.Companion.encodeMessageAsString(
                TYPE,
                id,
                JObject()
                    .add("listing", this.getAuctionID().toString())
                    .add("actor", this.getActor().toString())
                    .toJson()
            )
        }

        override fun respond(): CompletableFuture<AuctionMessage.Cancel.Response?>? {
            return GTSPlugin.Companion.getInstance().getStorage().processAuctionCancelRequest(this)
        }

        override fun print(printer: PrettyPrinter) {
            printer.kv("Request ID", id)
                .kv("Auction ID", this.getAuctionID())
                .kv("Actor", this.getActor())
        }

        companion object {
            const val TYPE = "Auction/Cancel/Request"
            @kotlin.jvm.JvmStatic
            fun decode(element: JsonElement?, id: UUID): Request {
                val base: Tuple<JsonObject, SimilarPair<UUID>> =
                    AuctionMessageOptions.Companion.decodeBaseAuctionParameters(element)
                val listing = base.second.first
                val actor = base.second.second
                return Request(id, listing, actor)
            }
        }
    }

    class Response
    /**
     * Constructs the message that'll be sent to all other connected servers.
     *
     * @param id      The message ID that'll be used to ensure the message isn't duplicated
     * @param request The ID of the message that spawned this response
     * @param listing The ID of the listing being bid on
     * @param actor   The ID of the user placing the bid
     * @param bidders The set of individuals who have bid on the listing
     * @param success The state of the response
     * @param error   An error code marking the reason of failure, should it be necessary. Can be null
     */(
        id: UUID,
        override val requestID: UUID,
        override val data: Auction,
        listing: UUID,
        actor: UUID,
        private override val bidders: ImmutableList<UUID>,
        private val success: Boolean,
        private val error: ErrorCode?
    ) : AuctionCancelMessage(id, listing, actor), AuctionMessage.Cancel.Response {
        override var responseTime: Long = 0
        override fun getBidders(): List<UUID> {
            return bidders
        }

        override fun asEncodedString(): String {
            return GTSMessagingService.Companion.encodeMessageAsString(
                TYPE,
                id,
                JObject()
                    .add("request", requestID.toString())
                    .add("listing", this.getAuctionID().toString())
                    .add("actor", this.getActor().toString())
                    .consume(Consumer { o: JObject ->
                        val bidders = JArray()
                        for (bidder in this.bidders) {
                            bidders.add(bidder.toString())
                        }
                        o.add("bidders", bidders)
                    })
                    .add("successful", success)
                    .add("data", data.serialize())
                    .consume(Consumer { o: JObject ->
                        errorCode.ifPresent { error: ErrorCode ->
                            o.add(
                                "error",
                                error.ordinal()
                            )
                        }
                    })
                    .toJson()
            )
        }

        override fun wasSuccessful(): Boolean {
            return success
        }

        override val errorCode: Optional<ErrorCode>
            get() = Optional.ofNullable(error)

        override fun print(printer: PrettyPrinter) {}

        companion object {
            const val TYPE = "Auction/Cancel/Response"
            @kotlin.jvm.JvmStatic
            fun decode(element: JsonElement?, id: UUID): Response {
                val base: Tuple<JsonObject, SimilarPair<UUID>> =
                    AuctionMessageOptions.Companion.decodeBaseAuctionParameters(element)
                val raw = base.first
                val listing = base.second.first
                val actor = base.second.second
                val data = Optional.ofNullable(raw["data"])
                    .map { x: JsonElement ->
                        instance!!.gTSComponentManager
                            .getListingResourceManager(Auction::class.java)
                            .get()
                            .deserializer
                            .deserialize(x.asJsonObject)
                    }
                    .orElseThrow { IllegalStateException("Response lacking auction data") }
                val request = Optional.ofNullable(raw["request"])
                    .map { x: JsonElement -> UUID.fromString(x.asString) }
                    .orElseThrow { IllegalStateException("Unable to locate or parse request ID") }
                val bidders = Optional.ofNullable(raw["bidders"])
                    .map<List<UUID>> { x: JsonElement ->
                        val result: MutableList<UUID> = Lists.newArrayList()
                        val array = x.asJsonArray
                        for (s in array) {
                            result.add(UUID.fromString(s.asString))
                        }
                        result
                    }
                    .orElseThrow { IllegalStateException("Failed to locate bidder information") }
                val successful = Optional.ofNullable(raw["successful"])
                    .map { obj: JsonElement -> obj.asBoolean }
                    .orElseThrow { IllegalStateException("Failed to locate success parameter") }
                val error = Optional.ofNullable(raw["error"])
                    .map { x: JsonElement -> ErrorCodes[x.asInt] }
                    .orElse(null)
                return Response(id, request, data, listing, actor, ImmutableList.copyOf(bidders), successful, error)
            }
        }
    }
}