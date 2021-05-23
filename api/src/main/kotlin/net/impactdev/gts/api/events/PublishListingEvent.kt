package net.impactdev.gts.api.events

import net.impactdev.gts.api.listings.Listing
import net.impactdev.gts.api.listings.auctions.Auction
import net.impactdev.impactor.api.event.ImpactorEvent
import net.impactdev.impactor.api.event.type.Cancellable
import java.util.*

/**
 * Represents when a user publishes a listing to the GTS.
 *
 * @author NickImpact
 */
interface PublishListingEvent : ImpactorEvent, Cancellable {
    @get:Param(0)
    val lister: UUID

    @get:Param(1)
    val listing: Listing
    val isAuction: Boolean
        get() = listing is Auction
}