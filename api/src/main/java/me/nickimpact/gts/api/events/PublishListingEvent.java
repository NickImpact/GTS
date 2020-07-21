package me.nickimpact.gts.api.events;

import com.nickimpact.impactor.api.event.ImpactorEvent;
import com.nickimpact.impactor.api.event.annotations.Param;
import com.nickimpact.impactor.api.event.type.Cancellable;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.auctions.Auction;
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
