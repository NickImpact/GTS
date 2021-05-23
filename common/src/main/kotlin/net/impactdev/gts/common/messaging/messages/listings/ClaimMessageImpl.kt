package net.impactdev.gts.common.messaging.messages.listings

import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.google.gson.JsonElement
import net.impactdev.gts.api.messaging.message.errors.ErrorCode
import net.impactdev.gts.api.messaging.message.errors.ErrorCodes
import net.impactdev.gts.api.messaging.message.type.listings.ClaimMessage
import net.impactdev.gts.api.messaging.message.type.listings.ClaimMessage.Response.AuctionResponse
import net.impactdev.gts.api.util.PrettyPrinter
import net.impactdev.gts.api.util.TriState
import net.impactdev.gts.common.messaging.GTSMessagingService
import net.impactdev.gts.common.messaging.messages.AbstractMessage
import net.impactdev.gts.common.messaging.messages.listings.ClaimMessageImpl.ClaimResponseImpl.AuctionClaimResponseImpl.AuctionClaimResponseBuilder
import net.impactdev.gts.common.plugin.GTSPlugin
import net.impactdev.impactor.api.json.factory.JArray
import net.impactdev.impactor.api.json.factory.JObject
import net.impactdev.impactor.api.utilities.Builder
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach
import kotlin.collections.get
import kotlin.collections.set

abstract class ClaimMessageImpl protected constructor(
    id: UUID?,
    override val listingID: UUID,
    override val actor: UUID,
    protected override val receiver: UUID?,
    override val isAuction: Boolean
) : AbstractMessage(
    id!!
), ClaimMessage {
    override fun getReceiver(): Optional<UUID> {
        return Optional.ofNullable(receiver)
    }

    override fun print(printer: PrettyPrinter) {
        printer.kv("Message ID", id)
            .kv("Listing ID", listingID)
            .kv("Actor", actor)
            .consume(Consumer { p: PrettyPrinter? -> getReceiver().ifPresent { r: UUID? -> p!!.kv("Receiver", r) } })
            .kv("Is Auction", isAuction)
    }

    class ClaimRequestImpl(id: UUID?, listing: UUID, actor: UUID, receiver: UUID?, auction: Boolean) :
        ClaimMessageImpl(id, listing, actor, receiver, auction), ClaimMessage.Request {
        override fun respond(): CompletableFuture<ClaimMessage.Response?>? {
            // Divert to storage system to respond, there it should response with either the base
            // response type, or one specifically meant for auctions
            return GTSPlugin.Companion.getInstance().getStorage().processClaimRequest(this)
        }

        override fun asEncodedString(): String {
            return GTSMessagingService.Companion.encodeMessageAsString(
                TYPE,
                id,
                JObject()
                    .add("listing", listingID.toString())
                    .add("actor", actor.toString())
                    .consume { o: JObject -> getReceiver().ifPresent { r: UUID -> o.add("receiver", r.toString()) } }
                    .add("auction", isAuction)
                    .toJson()
            )
        }

        companion object {
            const val TYPE = "Listing/Claim/Request"
            @kotlin.jvm.JvmStatic
            fun decode(content: JsonElement?, id: UUID?): ClaimRequestImpl {
                checkNotNull(content) { "Raw JSON data was null" }
                val raw = content.asJsonObject
                val listing = Optional.ofNullable(raw["listing"])
                    .map { x: JsonElement -> UUID.fromString(x.asString) }
                    .orElseThrow { IllegalStateException("Unable to locate listing ID") }
                val actor = Optional.ofNullable(raw["actor"])
                    .map { x: JsonElement -> UUID.fromString(x.asString) }
                    .orElseThrow { IllegalStateException("Unable to locate actor ID") }
                val receiver = Optional.ofNullable(raw["receiver"])
                    .map { x: JsonElement -> UUID.fromString(x.asString) }
                    .orElse(null)
                val auction = Optional.ofNullable(raw["auction"])
                    .map { obj: JsonElement -> obj.asBoolean }
                    .orElseThrow { IllegalStateException("Failed to locate auction check field") }
                return ClaimRequestImpl(id, listing, actor, receiver, auction)
            }
        }
    }

    open class ClaimResponseImpl(builder: ClaimResponseBuilder) : ClaimMessageImpl(
        builder.id,
        builder.listing!!,
        builder.actor!!,
        builder.actor,
        builder is AuctionClaimResponseBuilder
    ), ClaimMessage.Response {
        override val requestID: UUID?
        protected val successful: Boolean
        protected val error: ErrorCode?
        override var responseTime: Long = 0
        override fun wasSuccessful(): Boolean {
            return successful
        }

        override val errorCode: Optional<ErrorCode>
            get() = Optional.ofNullable(error)

        override fun print(printer: PrettyPrinter) {
            super.print(printer)
            printer.add().kv("Request ID", requestID)
        }

        override fun asEncodedString(): String {
            return GTSMessagingService.Companion.encodeMessageAsString(
                TYPE,
                id,
                JObject()
                    .add("request", requestID.toString())
                    .add("listing", listingID.toString())
                    .add("actor", actor.toString())
                    .consume { o: JObject -> getReceiver().ifPresent { r: UUID -> o.add("receiver", r.toString()) } }
                    .add("auction", isAuction)
                    .add("successful", successful)
                    .consume { o: JObject -> errorCode.ifPresent { e: ErrorCode -> o.add("error", e.ordinal()) } }
                    .toJson()
            )
        }

        class AuctionClaimResponseImpl(builder: AuctionClaimResponseBuilder) : ClaimResponseImpl(builder),
            AuctionResponse {
            private val lister: Boolean
            private val winner: Boolean
            private val others: Map<UUID, Boolean>
            override fun hasListerClaimed(): Boolean {
                return lister
            }

            override fun hasWinnerClaimed(): Boolean {
                return winner
            }

            override fun hasOtherBidderClaimed(uuid: UUID?): TriState? {
                return Optional.ofNullable(others[uuid])
                    .map { obj: Boolean? -> TriState.fromBoolean() }
                    .orElse(TriState.UNDEFINED)
            }

            override val allOtherClaimers: List<UUID>
                get() = Lists.newArrayList(others.keys)

            override fun print(printer: PrettyPrinter) {
                super.print(printer)
                printer.add()
                printer.add("Lister Claimed: " + lister)
                printer.add("Winner Claimed: " + winner)
                printer.add("Others:")
                for (id in others.keys) {
                    printer.add("  - $id")
                }
            }

            override fun asEncodedString(): String {
                return GTSMessagingService.Companion.encodeMessageAsString(
                    TYPE,
                    id,
                    JObject()
                        .add("request", requestID.toString())
                        .add("listing", listingID.toString())
                        .add("actor", actor.toString())
                        .consume { o: JObject ->
                            getReceiver().ifPresent { r: UUID ->
                                o.add(
                                    "receiver",
                                    r.toString()
                                )
                            }
                        }
                        .add("auction", isAuction)
                        .add("lister", lister)
                        .add("winner", winner)
                        .consume { o: JObject ->
                            val others = JArray()
                            this.others.forEach { (user: UUID, state: Boolean?) ->
                                others.add(
                                    JObject().add(
                                        user.toString(),
                                        state
                                    )
                                )
                            }
                            o.add("others", others)
                        }
                        .add("successful", successful)
                        .consume { o: JObject -> errorCode.ifPresent { e: ErrorCode -> o.add("error", e.ordinal()) } }
                        .toJson()
                )
            }

            class AuctionClaimResponseBuilder : ClaimResponseBuilder() {
                var lister = false
                var winner = false
                var others: Map<UUID, Boolean> = Maps.newHashMap()
                fun lister(state: Boolean): AuctionClaimResponseBuilder {
                    lister = state
                    return this
                }

                fun winner(state: Boolean): AuctionClaimResponseBuilder {
                    winner = state
                    return this
                }

                fun others(others: Map<UUID, Boolean>): AuctionClaimResponseBuilder {
                    this.others = others
                    return this
                }

                override fun from(response: ClaimMessage.Response): AuctionClaimResponseBuilder? {
                    id = response.iD
                    listing = response.listingID
                    actor = response.actor
                    receiver = response.receiver!!.orElse(null)
                    request = response.requestID
                    successful = response.wasSuccessful()
                    error = response.errorCode.orElse(null)
                    return this
                }

                override fun build(): AuctionClaimResponseImpl {
                    return AuctionClaimResponseImpl(this)
                }
            }

            companion object {
                fun builder(): AuctionClaimResponseBuilder {
                    return AuctionClaimResponseBuilder()
                }
            }

            init {
                lister = builder.lister
                winner = builder.winner
                others = builder.others
            }
        }

        open class ClaimResponseBuilder : Builder<ClaimMessage.Response, ClaimResponseBuilder?> {
            var id: UUID? = null
            var listing: UUID? = null
            var actor: UUID? = null
            protected var receiver: UUID? = null
            var request: UUID? = null
            var successful = false
            var error: ErrorCode? = null
            fun id(id: UUID?): ClaimResponseBuilder {
                this.id = id
                return this
            }

            fun listing(listing: UUID?): ClaimResponseBuilder {
                this.listing = listing
                return this
            }

            fun actor(actor: UUID?): ClaimResponseBuilder {
                this.actor = actor
                return this
            }

            fun receiver(receiver: UUID?): ClaimResponseBuilder {
                this.receiver = receiver
                return this
            }

            fun auction(): AuctionClaimResponseBuilder? {
                return AuctionClaimResponseImpl.builder().from(build())
            }

            fun request(request: UUID?): ClaimResponseBuilder {
                this.request = request
                return this
            }

            fun successful(): ClaimResponseBuilder {
                successful = true
                return this
            }

            fun error(error: ErrorCode?): ClaimResponseBuilder {
                this.error = error
                return this
            }

            override fun from(response: ClaimMessage.Response): ClaimResponseBuilder? {
                return null
            }

            override fun build(): ClaimResponseImpl {
                return ClaimResponseImpl(this)
            }
        }

        companion object {
            const val TYPE = "Listing/Claim/Response"
            @kotlin.jvm.JvmStatic
            fun decode(content: JsonElement?, id: UUID?): ClaimMessage.Response {
                checkNotNull(content) { "Raw JSON data was null" }
                val raw = content.asJsonObject
                val request = Optional.ofNullable(raw["request"])
                    .map { x: JsonElement -> UUID.fromString(x.asString) }
                    .orElseThrow { IllegalStateException("Unable to locate or parse request ID") }
                val listing = Optional.ofNullable(raw["listing"])
                    .map { x: JsonElement -> UUID.fromString(x.asString) }
                    .orElseThrow { IllegalStateException("Unable to locate listing ID") }
                val actor = Optional.ofNullable(raw["actor"])
                    .map { x: JsonElement -> UUID.fromString(x.asString) }
                    .orElseThrow { IllegalStateException("Unable to locate actor ID") }
                val receiver = Optional.ofNullable(raw["receiver"])
                    .map { x: JsonElement -> UUID.fromString(x.asString) }
                    .orElse(null)
                val auction = Optional.ofNullable(raw["auction"])
                    .map { obj: JsonElement -> obj.asBoolean }
                    .orElseThrow { IllegalStateException("Failed to locate auction check field") }
                val successful = Optional.ofNullable(raw["successful"])
                    .map { obj: JsonElement -> obj.asBoolean }
                    .orElseThrow { IllegalStateException("Failed to locate successful status") }
                val error = Optional.ofNullable(raw["error"])
                    .map { x: JsonElement -> ErrorCodes[x.asInt] }
                    .orElse(null)
                val builder = builder()
                    .id(id)
                    .request(request)
                    .listing(listing)
                    .actor(actor)
                    .receiver(receiver)
                    .error(error)
                if (successful) {
                    builder.successful()
                }
                return if (auction) {
                    val auc = AuctionClaimResponseImpl.builder().from(builder.build())
                    val lister = Optional.ofNullable(raw["lister"])
                        .map { obj: JsonElement -> obj.asBoolean }
                        .orElseThrow { IllegalStateException("Failed to locate lister status") }
                    val winner = Optional.ofNullable(raw["winner"])
                        .map { obj: JsonElement -> obj.asBoolean }
                        .orElseThrow { IllegalStateException("Failed to locate winner status") }
                    val others = Optional.ofNullable(raw["others"])
                        .map<Map<UUID, Boolean>> { element: JsonElement ->
                            val result: MutableMap<UUID, Boolean> = Maps.newHashMap()
                            val array = element.asJsonArray
                            for (entry in array) {
                                entry.asJsonObject.entrySet().forEach(Consumer { e: Map.Entry<String?, JsonElement> ->
                                    result[UUID.fromString(e.key)] = e.value.asBoolean
                                })
                            }
                            result
                        }
                        .orElseThrow { IllegalStateException("Failed to locate others status") }
                    auc!!.lister(lister).winner(winner).others(others).build()
                } else {
                    builder.build()
                }
            }

            fun builder(): ClaimResponseBuilder {
                return ClaimResponseBuilder()
            }
        }

        init {
            requestID = builder.request
            successful = builder.successful
            error = builder.error
        }
    }
}