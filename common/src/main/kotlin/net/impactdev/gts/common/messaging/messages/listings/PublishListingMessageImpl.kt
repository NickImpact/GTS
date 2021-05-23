package net.impactdev.gts.common.messaging.messages.listings

import com.google.gson.JsonElement
import net.impactdev.gts.api.messaging.message.type.listings.PublishListingMessage
import net.impactdev.gts.api.util.PrettyPrinter
import net.impactdev.gts.common.messaging.GTSMessagingService
import net.impactdev.gts.common.messaging.messages.AbstractMessage
import net.impactdev.impactor.api.json.factory.JObject
import java.util.*

class PublishListingMessageImpl(
    id: UUID,
    override val listingID: UUID,
    override val actor: UUID,
    override val isAuction: Boolean
) : AbstractMessage(id), PublishListingMessage {
    override fun asEncodedString(): String {
        return GTSMessagingService.Companion.encodeMessageAsString(
            TYPE,
            id,
            JObject()
                .add("listing", listingID.toString())
                .add("actor", actor.toString())
                .add("auction", isAuction)
                .toJson()
        )
    }

    override fun print(printer: PrettyPrinter) {
        printer.kv("Request ID", id)
            .kv("Listing ID", listingID)
            .kv("Actor", actor)
            .kv("Is Auction", isAuction)
    }

    companion object {
        const val TYPE = "Listings/Publish"
        @kotlin.jvm.JvmStatic
        fun decode(content: JsonElement?, id: UUID): PublishListingMessageImpl {
            checkNotNull(content) { "Raw JSON data was null" }
            val raw = content.asJsonObject
            val listing = Optional.ofNullable(raw["listing"])
                .map { x: JsonElement -> UUID.fromString(x.asString) }
                .orElseThrow { IllegalStateException("Unable to locate listing ID") }
            val actor = Optional.ofNullable(raw["actor"])
                .map { x: JsonElement -> UUID.fromString(x.asString) }
                .orElseThrow { IllegalStateException("Unable to locate actor ID") }
            val auction = Optional.ofNullable(raw["auction"])
                .map { obj: JsonElement -> obj.asBoolean }
                .orElseThrow { IllegalStateException("Unable to locate auction status marker") }
            return PublishListingMessageImpl(id, listing, actor, auction)
        }
    }
}