package net.impactdev.gts.common.messaging.messages.listings.auctions.impl

import com.google.common.base.Preconditions
import com.google.common.collect.TreeMultimap
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.impactdev.gts.api.listings.auctions.Auction
import net.impactdev.gts.api.messaging.message.errors.ErrorCode
import net.impactdev.gts.api.messaging.message.errors.ErrorCodes
import net.impactdev.gts.api.messaging.message.type.auctions.AuctionMessage
import net.impactdev.gts.api.util.PrettyPrinter
import net.impactdev.gts.api.util.groupings.SimilarPair
import net.impactdev.gts.common.messaging.GTSMessagingService
import net.impactdev.gts.common.messaging.messages.listings.auctions.AuctionMessageOptions
import net.impactdev.gts.common.plugin.GTSPlugin
import net.impactdev.gts.common.utils.EconomicFormatter
import net.impactdev.impactor.api.Impactor
import net.impactdev.impactor.api.json.factory.JArray
import net.impactdev.impactor.api.json.factory.JObject
import net.impactdev.impactor.api.utilities.mappings.Tuple
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.stream.Collectors

abstract class AuctionBidMessage
/**
 * Constructs the message that'll be sent to all other connected servers.
 *
 * @param id      The message ID that'll be used to ensure the message isn't duplicated
 * @param listing The ID of the listing being bid on
 * @param actor   The ID of the user placing the bid
 */(id: UUID, listing: UUID, actor: UUID, override val amountBid: Double) : AuctionMessageOptions(id, listing, actor),
    AuctionMessage.Bid {

    class Request(id: UUID, listing: UUID, actor: UUID, bid: Double) : AuctionBidMessage(id, listing, actor, bid),
        AuctionMessage.Bid.Request {
        override fun asEncodedString(): String {
            return GTSMessagingService.Companion.encodeMessageAsString(
                TYPE,
                id,
                JObject()
                    .add("listing", this.getAuctionID().toString())
                    .add("actor", this.getActor().toString())
                    .add("bid", amountBid)
                    .toJson()
            )
        }

        override fun respond(): CompletableFuture<AuctionMessage.Bid.Response?>? {
            return GTSPlugin.Companion.getInstance().getStorage().processBid(this)
        }

        override fun print(printer: PrettyPrinter) {
            printer.kv("Request ID", id)
                .kv("Auction ID", this.getAuctionID())
                .kv("Actor", this.getActor())
                .kv("Amount Bid", Impactor.getInstance().registry.get(EconomicFormatter::class.java).format(amountBid))
        }

        companion object {
            /** Specifies the typing for this message. AKA, the identifier from a standpoint on knowing the incoming message type  */
            const val TYPE = "Auction/Bid/Request"

            /**
             * Attempts to decode a new AuctionMessageOptions from the raw JSON data. This call will only fail exceptionally
             * when the raw JSON data is either missing or lacking a component that this message should be populated with.
             *
             * @param element The raw JSON data representing this message
             * @param id The ID of the message received
             * @return A deserialized version of the message matching a AuctionMessageOptions
             */
            @kotlin.jvm.JvmStatic
            fun decode(element: JsonElement?, id: UUID): Request {
                val base: Tuple<JsonObject, SimilarPair<UUID>> =
                    AuctionMessageOptions.Companion.decodeBaseAuctionParameters(element)
                val raw = base.first
                val listing = base.second.first
                val actor = base.second.second
                val bid = Optional.ofNullable(raw["bid"])
                    .map { obj: JsonElement -> obj.asDouble }
                    .orElseThrow { IllegalStateException("Failed to locate bid amount") }
                return Request(id, listing, actor, bid)
            }
        }

        /**
         * Constructs the message that'll be sent to all other connected servers.
         *
         * @param id      The message ID that'll be used to ensure the message isn't duplicated
         * @param listing The ID of the listing being bid on
         * @param actor   The ID of the user placing the bid
         * @param bid     The amount that was bid on the listing
         */
        init {
            Preconditions.checkArgument(bid > 0, "The input bid must be positive")
        }
    }

    class Response(
        id: UUID,
        request: UUID,
        listing: UUID,
        actor: UUID,
        bid: Double,
        successful: Boolean,
        seller: UUID,
        bids: TreeMultimap<UUID?, Auction.Bid?>,
        error: ErrorCode
    ) : AuctionBidMessage(id, listing, actor, bid), AuctionMessage.Bid.Response {
        /** The ID of the request message generating this response  */
        override val requestID: UUID

        /** Whether the transaction was successfully placed  */
        private val successful: Boolean

        /** Specifies the seller of the auction  */
        override val seller: UUID

        /** Details a filtered list of all bids placed on this auction, with the highest bid per user filtered in  */
        override val allOtherBids: TreeMultimap<UUID?, Auction.Bid?>

        /** The amount of time it took for this response to be generated  */
        override var responseTime: Long = 0

        /** The error code reported for this response, if any  */
        private val error: ErrorCode
        override fun asEncodedString(): String {
            return GTSMessagingService.Companion.encodeMessageAsString(
                TYPE,
                id,
                JObject()
                    .add("request", requestID.toString())
                    .add("listing", this.getAuctionID().toString())
                    .add("actor", this.getActor().toString())
                    .add("bid", amountBid)
                    .add("successful", wasSuccessful())
                    .add("seller", seller.toString())
                    .consume(Consumer { o: JObject ->
                        val users = JObject()
                        for (id in allOtherBids.keySet()) {
                            val bids = JArray()
                            for (bid in allOtherBids[id].stream()
                                .sorted(Collections.reverseOrder(Comparator.comparing(Auction.Bid::amount))).collect(
                                Collectors.toList()
                            )) {
                                bids.add(bid.serialize())
                            }
                            users.add(id.toString(), bids)
                        }
                        o.add("bids", users)
                    })
                    .consume(Consumer { o: JObject ->
                        errorCode.ifPresent { e: ErrorCode ->
                            o.add(
                                "error",
                                e.ordinal()
                            )
                        }
                    })
                    .toJson()
            )
        }

        override fun wasSuccessful(): Boolean {
            return successful
        }

        override val errorCode: Optional<ErrorCode>
            get() = Optional.ofNullable(error)

        override fun print(printer: PrettyPrinter) {
            printer.kv("Response ID", id)
                .kv("Request ID", requestID)
                .kv("Auction ID", this.getAuctionID())
                .kv("Actor", this.getActor())
                .kv("Amount Bid", Impactor.getInstance().registry.get(EconomicFormatter::class.java).format(amountBid))
                .add()
                .kv("Seller", seller)
            if (allOtherBids.size() > 0) {
                var index = 0
                val amount = allOtherBids.size()
                printer.add()
                    .hr('-')
                    .add("All Bids").center()
                    .table("UUID", "Bid")
                val bids: List<Map.Entry<UUID?, Auction.Bid>> = allOtherBids.entries()
                    .stream()
                    .sorted(Collections.reverseOrder(Comparator.comparing { bid: Map.Entry<UUID?, Auction.Bid> -> bid.value.amount }))
                    .collect(Collectors.toList())
                for ((key, value) in bids) {
                    printer.tr(
                        key, Impactor.getInstance().registry.get(EconomicFormatter::class.java).format(
                            value.amount
                        )
                    )
                    if (++index == 5) {
                        break
                    }
                }
                if (index == 5 && amount - index > 0) {
                    printer.add("and " + (amount - index) + "more...")
                }
                printer.hr('-')
            }
        }

        companion object {
            /** Specifies the typing for this message. AKA, the identifier from a standpoint on knowing the incoming message type  */
            const val TYPE = "Auction/Bid/Response"

            /**
             * Attempts to decode a new AuctionMessageOptions from the raw JSON data. This call will only fail exceptionally
             * when the raw JSON data is either missing or lacking a component that this message should be populated with.
             *
             * @param element The raw JSON data representing this message
             * @param id The ID of the message received
             * @return A deserialized version of the message matching a AuctionMessageOptions
             */
            @kotlin.jvm.JvmStatic
            fun decode(element: JsonElement?, id: UUID): Response {
                val base: Tuple<JsonObject, SimilarPair<UUID>> =
                    AuctionMessageOptions.Companion.decodeBaseAuctionParameters(element)
                val raw = base.first
                val listing = base.second.first
                val actor = base.second.second
                val request = Optional.ofNullable(raw["request"])
                    .map { x: JsonElement -> UUID.fromString(x.asString) }
                    .orElseThrow { IllegalStateException("Unable to locate or parse request ID") }
                val bid = Optional.ofNullable(raw["bid"])
                    .map { obj: JsonElement -> obj.asDouble }
                    .orElseThrow { IllegalStateException("Failed to locate bid amount") }
                val successful = Optional.ofNullable(raw["successful"])
                    .map { obj: JsonElement -> obj.asBoolean }
                    .orElseThrow { IllegalStateException("Failed to locate success parameter") }
                val seller = Optional.ofNullable(raw["seller"])
                    .map { e: JsonElement -> UUID.fromString(e.asString) }
                    .orElseThrow { IllegalStateException("Failed to locate seller") }
                val bids = TreeMultimap.create(
                    Comparator.naturalOrder<UUID?>(),
                    Collections.reverseOrder(Comparator.comparing(Auction.Bid::amount))
                )
                Optional.ofNullable(raw["bids"])
                    .map { e: JsonElement ->
                        val map = e.asJsonObject
                        for ((key, value) in map.entrySet()) {
                            val user = UUID.fromString(key)
                            val userBids = value.asJsonArray
                            for (placedBid in userBids) {
                                val data = placedBid.asJsonObject
                                val parsed = Auction.Bid.builder()
                                    .amount(data["amount"].asDouble)
                                    .timestamp(LocalDateTime.parse(data["timestamp"].asString))
                                    .build()
                                bids.put(user, parsed)
                            }
                        }
                        map
                    }
                    .orElseThrow { IllegalStateException("Failed to locate additional bid information") }
                val error = Optional.ofNullable(raw["error"])
                    .map { x: JsonElement -> ErrorCodes[x.asInt] }
                    .orElse(null)
                return Response(id, request, listing, actor, bid, successful, seller, bids, error)
            }
        }

        /**
         * Constructs the message that'll be sent to all other connected servers.
         *
         * @param id         The message ID that'll be used to ensure the message isn't duplicated
         * @param request    The ID of the request message that generated this response
         * @param listing    The ID of the listing being bid on
         * @param actor      The ID of the user placing the bid
         * @param bid        The amount bid on this auction
         * @param successful If the bid was placed successfully
         * @param seller     The ID of the user who created the auction
         * @param bids       All other bids placed, filtered to contain highest bids per user, as a means of communication
         */
        init {
            Preconditions.checkNotNull(request, "Request message ID cannot be null")
            Preconditions.checkArgument(bid > 0, "The input bid must be positive")
            Preconditions.checkNotNull(seller, "Seller value was left null")
            Preconditions.checkNotNull(bids, "Bid history was left null")
            requestID = request
            this.successful = successful
            this.seller = seller
            allOtherBids = bids
            this.error = error
        }
    }
}