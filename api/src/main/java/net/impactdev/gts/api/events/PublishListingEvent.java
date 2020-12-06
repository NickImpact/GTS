package net.impactdev.gts.api.events;

import net.impactdev.impactor.api.event.ImpactorEvent;
import net.impactdev.impactor.api.event.annotations.Param;
import net.impactdev.impactor.api.event.type.Cancellable;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.auctions.Auction;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

/**
 * Represents when a user publishes a listing to the GTS.
 *
 * @author NickImpact
 */
public interface PublishListingEvent extends ImpactorEvent, Cancellable {

	@Param(0)
	@NonNull UUID getLister();

	@Param(1)
	@NonNull
	Listing getListing();

	default boolean isAuction() {
		return this.getListing() instanceof Auction;
	}

}
