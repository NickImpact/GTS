package net.impactdev.gts.api.events;

import net.impactdev.impactor.api.event.ImpactorEvent;
import net.impactdev.impactor.api.event.type.Cancellable;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.auctions.Auction;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.util.annotation.eventgen.GenerateFactoryMethod;

import java.util.UUID;

/**
 * Represents when a user publishes a listing to the GTS.
 *
 * @author NickImpact
 */
@GenerateFactoryMethod
public interface PublishListingEvent extends ImpactorEvent, Cancellable {

	@NonNull UUID getLister();

	@NonNull
	Listing getListing();

	default boolean isAuction() {
		return this.getListing() instanceof Auction;
	}

}
