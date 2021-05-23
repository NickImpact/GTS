package net.impactdev.gts.api.messaging.message.type.admin

import net.impactdev.gts.api.listings.Listing
import net.impactdev.gts.api.messaging.message.OutgoingMessage
import net.impactdev.gts.api.messaging.message.errors.ErrorCode
import net.impactdev.gts.api.messaging.message.type.MessageType
import net.impactdev.impactor.api.Impactor
import net.impactdev.impactor.api.utilities.Builder
import java.util.*

/**
 * Represents the message that'll be sent when an administrator decides to force delete a listing off the
 * GTS market. Realistically, returning the data to a user is outside the scope of the message, and should
 * be handled by the requester.
 */
interface ForceDeleteMessage : OutgoingMessage {
    /**
     * Represents the ID of the auction being acted on. This ID is the primary key to locating an auction,
     * and as such, should be unique to one specific auction.
     *
     * @return The ID of the auction
     */
    val listingID: UUID

    /**
     * Represents the UUID of a player or another source that is applying the action to this auction.
     *
     * @return The UUID of the source applying the action
     */
    val actor: UUID

    /**
     * States if this request should return the data contained in the listing if it is deleted.
     *
     * @return True if the entry should be returned to the user on deletion
     */
    fun shouldGive(): Boolean
    interface Request : ForceDeleteMessage, MessageType.Request<Response?>
    interface Response : ForceDeleteMessage, MessageType.Response {
        /**
         * Represents the UUID of the user who had their listing deleted. Since the listing will have been
         * deleted before the response is made available to the requesting server, we will need to supply
         * information back to the server about the listing.
         *
         * Note that if the request was marked unsuccessful, this value will not be populated.
         *
         * @return The ID of the user who made the listing
         */
        val deletedListing: Optional<Listing?>?

        interface ResponseBuilder : Builder<Response?, ResponseBuilder?> {
            fun request(request: UUID?): ResponseBuilder?
            fun listing(listing: UUID?): ResponseBuilder?
            fun actor(actor: UUID?): ResponseBuilder?
            fun data(data: Listing?): ResponseBuilder?
            fun give(give: Boolean): ResponseBuilder?
            fun successful(successful: Boolean): ResponseBuilder?
            fun error(error: ErrorCode?): ResponseBuilder?
        }

        companion object {
            @kotlin.jvm.JvmStatic
            fun builder(): ResponseBuilder? {
                return Impactor.getInstance().registry.createBuilder(
                    ResponseBuilder::class.java
                )
            }
        }
    }
}