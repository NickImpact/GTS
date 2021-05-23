package net.impactdev.gts.common.messaging.messages.listings.buyitnow.removal

import com.google.gson.JsonElement
import net.impactdev.gts.api.listings.Listing
import net.impactdev.gts.api.messaging.message.errors.ErrorCode
import net.impactdev.gts.api.messaging.message.errors.ErrorCodes
import net.impactdev.gts.api.messaging.message.type.listings.BuyItNowMessage
import net.impactdev.gts.api.util.PrettyPrinter
import net.impactdev.gts.common.messaging.GTSMessagingService
import net.impactdev.gts.common.messaging.messages.AbstractMessage
import net.impactdev.gts.common.plugin.GTSPlugin
import net.impactdev.impactor.api.json.factory.JObject
import java.util.*
import java.util.concurrent.CompletableFuture

abstract class BINRemoveMessage(
    id: UUID,
    override val listingID: UUID?,
    override val actor: UUID?,
    protected override val recipient: UUID?,
    protected val shouldReceive: Boolean
) : AbstractMessage(id), BuyItNowMessage.Remove {
    override fun getRecipient(): Optional<UUID> {
        return Optional.ofNullable(recipient)
    }

    override fun shouldReturnListing(): Boolean {
        return shouldReceive
    }

    class Request(id: UUID, listing: UUID?, actor: UUID?, recipient: UUID?, shouldReceive: Boolean) :
        BINRemoveMessage(id, listing, actor, recipient, shouldReceive), BuyItNowMessage.Remove.Request {
        override fun asEncodedString(): String {
            return GTSMessagingService.Companion.encodeMessageAsString(
                TYPE,
                id,
                JObject()
                    .add("listing", listingID.toString())
                    .add("actor", actor.toString())
                    .consume { o: JObject ->
                        if (recipient != null) {
                            o.add("recipient", recipient.toString())
                        }
                    }
                    .add("shouldReceive", shouldReceive)
                    .toJson()
            )
        }

        override fun respond(): CompletableFuture<BuyItNowMessage.Remove.Response?>? {
            return GTSPlugin.Companion.getInstance().getStorage().processListingRemoveRequest(this)
        }

        override fun print(printer: PrettyPrinter) {
            printer.kv("Request ID", id)
                .kv("Listing ID", listingID)
                .kv("Actor", actor)
                .kv("Receiver", getRecipient().orElse(Listing.SERVER_ID))
                .kv("Should Receive", shouldReturnListing())
        }

        companion object {
            const val TYPE = "BIN/Remove/Request"
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
                val shouldReceive = Optional.ofNullable(raw["shouldReceive"])
                    .map { obj: JsonElement -> obj.asBoolean }
                    .orElseThrow { IllegalStateException("Unable to locate shouldReceive flag") }
                val receiver = Optional.ofNullable(raw["receiver"])
                    .map { x: JsonElement -> UUID.fromString(x.asString) }
                    .orElse(null)
                return Request(id, listing, actor, receiver, shouldReceive)
            }
        }
    }

    class Response(
        id: UUID,
        override val requestID: UUID,
        listing: UUID?,
        actor: UUID?,
        recipient: UUID?,
        shouldReceive: Boolean,
        private val successful: Boolean,
        private val error: ErrorCode?
    ) : BINRemoveMessage(id, listing, actor, recipient, shouldReceive), BuyItNowMessage.Remove.Response {
        override var responseTime: Long = 0
        override fun asEncodedString(): String {
            return GTSMessagingService.Companion.encodeMessageAsString(
                TYPE,
                id,
                JObject()
                    .add("request", requestID.toString())
                    .add("listing", listingID.toString())
                    .add("actor", actor.toString())
                    .consume { o: JObject ->
                        if (recipient != null) {
                            o.add("receiver", recipient.toString())
                        }
                    }
                    .add("shouldReceive", shouldReceive)
                    .add("successful", wasSuccessful())
                    .consume { o: JObject ->
                        if (error != null) {
                            o.add("error", error.ordinal())
                        }
                    }
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
                .kv("Receiver", getRecipient().orElse(Listing.SERVER_ID))
            getRecipient().ifPresent { id: UUID? -> printer.kv("Recipient", id) }
            printer.kv("Should Receive", shouldReturnListing())
        }

        companion object {
            const val TYPE = "BIN/Remove/Response"
            @kotlin.jvm.JvmStatic
            fun decode(content: JsonElement?, id: UUID): Response {
                checkNotNull(content) { "Raw JSON data was null" }
                val raw = content.asJsonObject
                val request = Optional.ofNullable(raw["request"])
                    .map { x: JsonElement -> UUID.fromString(x.asString) }
                    .orElseThrow { IllegalStateException("Unable to locate request ID") }
                val listing = Optional.ofNullable(raw["listing"])
                    .map { x: JsonElement -> UUID.fromString(x.asString) }
                    .orElseThrow { IllegalStateException("Unable to locate listing ID") }
                val actor = Optional.ofNullable(raw["actor"])
                    .map { x: JsonElement -> UUID.fromString(x.asString) }
                    .orElseThrow { IllegalStateException("Unable to locate actor ID") }
                val shouldReceive = Optional.ofNullable(raw["shouldReceive"])
                    .map { obj: JsonElement -> obj.asBoolean }
                    .orElseThrow { IllegalStateException("Unable to locate shouldReceive flag") }
                val successful = Optional.ofNullable(raw["successful"])
                    .map { obj: JsonElement -> obj.asBoolean }
                    .orElseThrow { IllegalStateException("Unable to locate successful flag") }
                val receiver = Optional.ofNullable(raw["recipient"])
                    .map { x: JsonElement -> UUID.fromString(x.asString) }
                    .orElse(null)
                val error = Optional.ofNullable(raw["error"])
                    .map { x: JsonElement -> ErrorCodes[x.asInt] }
                    .orElse(null)
                return Response(id, request, listing, actor, receiver, shouldReceive, successful, error)
            }
        }
    }
}