package me.nickimpact.gts.api.events;

import me.nickimpact.gts.api.listings.Listing;

import java.util.Optional;

/**
 * This ListEvent represents the action of a Player adding any type of listing into the GTS
 * market. To access the data of the listing, just simply parse through the fields of the listing
 * variable provided by the event.
 *
 * @author NickImpact
 */
public interface ListEvent<T, L extends Listing> {

	Optional<T> getPlayer();

	L getListing();

	boolean isCancelled();

	void setCancelled(boolean flag);

}
