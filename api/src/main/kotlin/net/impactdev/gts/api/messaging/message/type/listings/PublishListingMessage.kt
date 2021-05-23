package net.impactdev.gts.api.messaging.message.type.listings

import net.impactdev.gts.api.messaging.message.OutgoingMessage
import java.util.*

/**
 * This message indicates that a listing has recently been published, and we should attempt
 * to notify all servers of the listing.
 */
interface PublishListingMessage : OutgoingMessage {
    val listingID: UUID?
    val actor: UUID?
    val isAuction: Boolean
}