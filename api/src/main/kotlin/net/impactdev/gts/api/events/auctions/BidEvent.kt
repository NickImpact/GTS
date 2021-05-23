package net.impactdev.gts.api.events.auctions

import net.impactdev.gts.api.listings.Listing
import net.impactdev.impactor.api.event.ImpactorEvent
import net.impactdev.impactor.api.event.type.Cancellable
import java.util.*

interface BidEvent : ImpactorEvent, Cancellable {
    @get:Param(0)
    val bidder: UUID

    @get:Param(1)
    val listing: Listing

    @get:Param(2)
    val amountBid: Double
}