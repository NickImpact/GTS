package net.impactdev.gts.common.messaging.messages.admin

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.impactdev.gts.api.GTSService.Companion.instance
import net.impactdev.gts.api.listings.Listing
import net.impactdev.gts.api.listings.auctions.Auction
import net.impactdev.gts.api.listings.buyitnow.BuyItNow
import net.impactdev.gts.api.messaging.message.errors.ErrorCode
import net.impactdev.gts.api.messaging.message.errors.ErrorCodes
import net.impactdev.gts.api.messaging.message.type.admin.ForceDeleteMessage
import net.impactdev.gts.api.util.PrettyPrinter
import net.impactdev.gts.common.messaging.GTSMessagingService
import net.impactdev.gts.common.messaging.messages.AbstractMessage
import net.impactdev.gts.common.plugin.GTSPlugin
import net.impactdev.impactor.api.json.factory.JObject
import java.util.*
import java.util.concurrent.CompletableFuture

abstract class ForceDeleteMessageImpl(
    id: UUID,
    override val listingID: UUID,
    override val actor: UUID,
    protected val give: Boolean
) : AbstractMessage(id), ForceDeleteMessage {
    override fun shouldGive(): Boolean {
        return give
    }

    class ForceDeleteRequest(id: UUID, listing: UUID, actor: UUID, give: Boolean) :
        ForceDeleteMessageImpl(id, listing, actor, give), ForceDeleteMessage.Request {
        override fun respond(): CompletableFuture<ForceDeleteMessage.Response?>? {
            return GTSPlugin.Companion.getInstance().getStorage().processForcedDeletion(this)
        }

        override fun asEncodedString(): String {
            return GTSMessagingService.Companion.encodeMessageAsString(
                TYPE,
                id,
                JObject()
                    .add("listing", listingID.toString())
                    .add("actor", actor.toString())
                    .add("give", give)
                    .toJson()
            )
        }

        override fun print(printer: PrettyPrinter) {
            printer.kv("Message ID", id)
                .kv("Listing ID", listingID)
                .kv("Actor", actor)
        }

        companion object {
            const val TYPE = "Admin/Delete/Request"
            @kotlin.jvm.JvmStatic
            fun decode(content: JsonElement?, id: UUID): ForceDeleteRequest {
                checkNotNull(content) { "Raw JSON data was null" }
                val raw = content.asJsonObject
                val listing = Optional.ofNullable(raw["listing"])
                    .map { x: JsonElement -> UUID.fromString(x.asString) }
                    .orElseThrow { IllegalStateException("Unable to locate listing ID") }
                val actor = Optional.ofNullable(raw["actor"])
                    .map { x: JsonElement -> UUID.fromString(x.asString) }
                    .orElseThrow { IllegalStateException("Unable to locate actor ID") }
                val give = Optional.ofNullable(raw["give"])
                    .map { obj: JsonElement -> obj.asBoolean }
                    .orElseThrow { IllegalStateException("Failed to locate give parameter") }
                return ForceDeleteRequest(id, listing, actor, give)
            }
        }
    }

    class ForceDeleteResponse(
        id: UUID,
        override val requestID: UUID?,
        listing: UUID?,
        actor: UUID?,
        private val data: Listing?,
        give: Boolean,
        private val successful: Boolean,
        private val error: ErrorCode?
    ) : ForceDeleteMessageImpl(id, listing!!, actor!!, give), ForceDeleteMessage.Response {
        override var responseTime: Long = 0
        override fun wasSuccessful(): Boolean {
            return successful
        }

        override val errorCode: Optional<ErrorCode>
            get() = Optional.ofNullable(error)

        override fun asEncodedString(): String {
            return GTSMessagingService.Companion.encodeMessageAsString(
                TYPE,
                id,
                JObject()
                    .add("request", requestID.toString())
                    .add("listing", listingID.toString())
                    .add("actor", actor.toString())
                    .add("data", data!!.serialize())
                    .add("give", give)
                    .add("successful", successful)
                    .consume { o: JObject -> errorCode.ifPresent { e: ErrorCode -> o.add("error", e.ordinal()) } }
                    .toJson()
            )
        }

        override fun print(printer: PrettyPrinter) {
            printer.kv("Message ID", id)
                .kv("Request ID", requestID)
                .kv("Listing ID", listingID)
                .kv("Actor", actor)
                .kv("Should Return", give)
        }

        override val deletedListing: Optional<Listing>
            get() = Optional.of(data)

        class ForcedDeleteResponseBuilder : ForceDeleteMessage.Response.ResponseBuilder {
            private var request: UUID? = null
            private var listing: UUID? = null
            private var actor: UUID? = null
            private var data: Listing? = null
            private var give = false
            private var successful = false
            private var error: ErrorCode? = null
            override fun request(request: UUID?): ForceDeleteMessage.Response.ResponseBuilder? {
                this.request = request
                return this
            }

            override fun listing(listing: UUID?): ForceDeleteMessage.Response.ResponseBuilder? {
                this.listing = listing
                return this
            }

            override fun actor(actor: UUID?): ForceDeleteMessage.Response.ResponseBuilder? {
                this.actor = actor
                return this
            }

            override fun data(data: Listing?): ForceDeleteMessage.Response.ResponseBuilder? {
                this.data = data
                return this
            }

            override fun give(give: Boolean): ForceDeleteMessage.Response.ResponseBuilder? {
                this.give = give
                return this
            }

            override fun successful(successful: Boolean): ForceDeleteMessage.Response.ResponseBuilder? {
                this.successful = successful
                return this
            }

            override fun error(error: ErrorCode?): ForceDeleteMessage.Response.ResponseBuilder? {
                this.error = error
                return this
            }

            override fun from(response: ForceDeleteMessage.Response): ForceDeleteMessage.Response.ResponseBuilder? {
                return this
            }

            override fun build(): ForceDeleteMessage.Response? {
                return ForceDeleteResponse(
                    GTSPlugin.Companion.getInstance().getMessagingService().generatePingID(),
                    request,
                    listing,
                    actor,
                    data,
                    give,
                    successful,
                    error
                )
            }
        }

        companion object {
            const val TYPE = "Admin/Delete/Response"
            @kotlin.jvm.JvmStatic
            fun decode(content: JsonElement?, id: UUID): ForceDeleteResponse {
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
                val data = Optional.ofNullable(raw["data"])
                    .map { x: JsonElement ->
                        val json = x as JsonObject
                        val type = json["type"].asString
                        if (type == "bin") {
                            return@map instance!!.gTSComponentManager
                                .getListingResourceManager(BuyItNow::class.java)
                                .get()
                                .deserializer
                                .deserialize(json)
                        } else {
                            return@map instance!!.gTSComponentManager
                                .getListingResourceManager(Auction::class.java)
                                .get()
                                .deserializer
                                .deserialize(json)
                        }
                    }
                    .orElseThrow { IllegalStateException("Unable to locate or parse listing data") }
                val give = Optional.ofNullable(raw["give"])
                    .map { obj: JsonElement -> obj.asBoolean }
                    .orElseThrow { IllegalStateException("Failed to locate give parameter") }
                val successful = Optional.ofNullable(raw["successful"])
                    .map { obj: JsonElement -> obj.asBoolean }
                    .orElseThrow { IllegalStateException("Failed to locate success parameter") }
                val error = Optional.ofNullable(raw["error"])
                    .map { x: JsonElement -> ErrorCodes[x.asInt] }
                    .orElse(null)
                return ForceDeleteResponse(id, request, listing, actor, data, give, successful, error)
            }
        }
    }
}