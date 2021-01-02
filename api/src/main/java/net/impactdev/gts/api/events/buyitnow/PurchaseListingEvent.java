package net.impactdev.gts.api.events.buyitnow;

import net.impactdev.impactor.api.event.ImpactorEvent;
import net.impactdev.impactor.api.event.annotations.Param;
import net.impactdev.impactor.api.event.type.Cancellable;
import net.impactdev.gts.api.listings.Listing;

import java.util.UUID;

/**
 * This PurchaseEvent represents the action of a Player purchasing any type of listing from the GTS
 * market, whether it be through auction or simplistic purchase. To access the data of the listing,
 * just simply parse through the fields of the listing variable provided by the event.
 *
 * @author NickImpact
 */
public interface PurchaseListingEvent extends ImpactorEvent, Cancellable {

    @Param(0)
    UUID getBuyer();

    @Param(1)
    Listing getListing();


}
