package net.impactdev.gts.api.events.buyitnow

import net.impactdev.gts.api.listings.Listing
import net.impactdev.impactor.api.event.ImpactorEvent
import net.impactdev.impactor.api.event.type.Cancellable
import java.util.*

/**
 * This PurchaseEvent represents the action of a Player purchasing any type of listing from the GTS
 * market, whether it be through auction or simplistic purchase. To access the data of the listing,
 * just simply parse through the fields of the listing variable provided by the event.
 *
 * @author NickImpact
 */
interface PurchaseListingEvent : ImpactorEvent, Cancellable {
    @get:Param(0)
    val buyer: UUID?

    @get:Param(1)
    val listing: Listing?
}