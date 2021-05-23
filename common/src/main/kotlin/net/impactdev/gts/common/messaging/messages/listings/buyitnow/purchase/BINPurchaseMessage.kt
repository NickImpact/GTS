package net.impactdev.gts.common.messaging.messages.listings.buyitnow.purchase

import com.google.gson.JsonElement
import net.impactdev.gts.api.messaging.message.errors.ErrorCode
import net.impactdev.gts.api.messaging.message.errors.ErrorCodes
import net.impactdev.gts.api.messaging.message.type.listings.BuyItNowMessage.Purchase
import net.impactdev.gts.api.util.PrettyPrinter
import net.impactdev.gts.common.messaging.GTSMessagingService
import net.impactdev.gts.common.messaging.messages.AbstractMessage
import net.impactdev.gts.common.plugin.GTSPlugin
import net.impactdev.impactor.api.json.factory.JObject
import java.util.*
import java.util.concurrent.CompletableFuture

abstract class BINPurchaseMessage(id: UUID, override val listingID: UUID?, override val actor: UUID?) :
    AbstractMessage(id), Purchase {

    class Request(id: UUID, listing: UUID?, actor: UUID?) : BINPurchaseMessage(id, listing, actor), Purchase.Request {
        override fun asEncodedString(): String {
            return GTSMessagingService.Companion.encodeMessageAsString(
                TYPE,
                id,
                JObject()
                    .add("listing", listingID.toString())
                    .add("actor", actor.toString())
                    .toJson()
            )
        }

        override fun respond(): CompletableFuture<Purchase.Response?>? {
            return GTSPlugin.Companion.getInstance().getStorage().processPurchase(this)
        }

        override fun print(printer: PrettyPrinter) {
            printer.kv("Request ID", id)
                .kv("Listing ID", listingID)
                .kv("Actor", actor)
        }

        companion object {
            const val TYPE = "BIN/Purchase/Request"
            @kotlin.jvm.JvmStatic
            fun decode(content: JsonElement?, id: UUID): Request {
                checkNotNull(content) { "Raw JSON data was null" }
                val raw = content.asJsonObject
                val listing = Optional.ofNullable(raw["listing"])
                    .map { x: JsonElement -> UUID.fromString(x.asString) }
                    .orElseThrow { IllegalStateException("Unable to locate listing ID") }
                val actor = Optional.ofNullable(raw["actor"])
                    .map { x: JsonElement -> UUID.fromString(x.asString) }
                    .orElseThrow { IllegalStateException("Unable to locate actor ID") }
                return Request(id, listing, actor)
            }
        }
    }

    class Response(
        id: UUID,
        override val requestID: UUID,
        listing: UUID?,
        actor: UUID?,
        override val seller: UUID?,
        private val successful: Boolean,
        private val error: ErrorCode?
    ) : BINPurchaseMessage(id, listing, actor), Purchase.Response {
        override var responseTime: Long = 0
        override fun asEncodedString(): String {
            return GTSMessagingService.Companion.encodeMessageAsString(
                TYPE,
                id,
                JObject()
                    .add("request", requestID.toString())
                    .add("listing", listingID.toString())
                    .add("actor", actor.toString())
                    .add("seller", seller.toString())
                    .add("successful", successful)
                    .consume { o: JObject -> errorCode.ifPresent { e: ErrorCode -> o.add("error", e.ordinal()) } }
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
                .kv("Listing ID", listingID)
                .kv("Actor", actor)
                .kv("Seller", seller)
        }

        companion object {
            const val TYPE = "BIN/Purchase/Response"
            @kotlin.jvm.JvmStatic
            fun decode(content: JsonElement?, id: UUID): Response {
                checkNotNull(content) { "Raw JSON data was null" }
                val raw = content.asJsonObject
                val listing = Optional.ofNullable(raw["listing"])
                    .map { x: JsonElement -> UUID.fromString(x.asString) }
                    .orElseThrow { IllegalStateException("Unable to locate listing ID") }
                val actor = Optional.ofNullable(raw["actor"])
                    .map { x: JsonElement -> UUID.fromString(x.asString) }
                    .orElseThrow { IllegalStateException("Unable to locate actor ID") }
                val request = Optional.ofNullable(raw["request"])
                    .map { x: JsonElement -> UUID.fromString(x.asString) }
                    .orElseThrow { IllegalStateException("Unable to locate request ID") }
                val seller = Optional.ofNullable(raw["seller"])
                    .map { x: JsonElement -> UUID.fromString(x.asString) }
                    .orElseThrow { IllegalStateException("Unable to locate seller ID") }
                val successful = Optional.ofNullable(raw["successful"])
                    .map { obj: JsonElement -> obj.asBoolean }
                    .orElseThrow { IllegalStateException("Unable to locate successful status marker") }
                val error = Optional.ofNullable(raw["error"])
                    .map { x: JsonElement -> ErrorCodes[x.asInt] }
                    .orElse(null)
                return Response(id, request, listing, actor, seller, successful, error)
            }
        }
    }
}