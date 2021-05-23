package net.impactdev.gts.common.messaging.messages.listings.auctions

import com.google.common.base.Preconditions
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.impactdev.gts.api.messaging.message.type.auctions.AuctionMessage
import net.impactdev.gts.api.util.groupings.SimilarPair
import net.impactdev.gts.common.messaging.messages.AbstractMessage
import net.impactdev.impactor.api.utilities.mappings.Tuple
import java.util.*

/**
 * The purpose of this message is to inform the servers of a bid placed on an auction. This message is
 * primarily necessary in that players should be informed when their auction has had a new bid placed on it,
 * as well as when a user who has bid becomes outbid.
 *
 * This message will contain the main three components of the bid. These being the player who placed the bid,
 * the ID of the listing being bid on, and the amount bid. From there, the bungee server will then forward this
 * message to all other servers, in which this listing will be processed
 */
abstract class AuctionMessageOptions protected constructor(id: UUID, listing: UUID, actor: UUID) : AbstractMessage(id),
    AuctionMessage {
    /** The listing an auction is being made for  */
    override val auctionID: UUID

    /** The user who bid on the listing  */
    override val actor: UUID

    companion object {
        @kotlin.Throws(IllegalStateException::class)
        protected fun decodeBaseAuctionParameters(element: JsonElement?): Tuple<JsonObject, SimilarPair<UUID>> {
            checkNotNull(element) { "Raw JSON data was null" }
            val raw = element.asJsonObject
            val listing = Optional.ofNullable(raw["listing"])
                .map { e: JsonElement -> UUID.fromString(e.asString) }
                .orElseThrow { IllegalStateException("Failed to locate listing ID") }
            val actor = Optional.ofNullable(raw["actor"])
                .map { e: JsonElement -> UUID.fromString(e.asString) }
                .orElseThrow { IllegalStateException("Failed to locate actor UUID") }
            return Tuple(raw, SimilarPair(listing, actor))
        }
    }

    /**
     * Constructs the message that'll be sent to all other connected servers.
     *
     * @param id      The message ID that'll be used to ensure the message isn't duplicated
     * @param listing The ID of the listing being bid on
     * @param actor   The ID of the user placing the bid
     */
    init {
        Preconditions.checkNotNull(listing, "The listing ID is null")
        Preconditions.checkNotNull(actor, "The actor's UUID is null")
        auctionID = listing
        this.actor = actor
    }
}