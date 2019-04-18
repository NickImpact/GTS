package me.nickimpact.gts.api.events;

import lombok.Getter;
import me.nickimpact.gts.api.listings.Listing;

import java.util.Optional;

/**
 * This ListEvent represents the action of a Player adding any type of listing into the GTS
 * market. To access the data of the listing, just simply parse through the fields of the listing
 * variable provided by the event. While it may not seem like it, this event does have getter methods
 * thanks to lombok's {@link Getter} annotation.
 *
 * @author NickImpact
 */
public interface ListEvent<T, L extends Listing> {

	Optional<T> getPlayer();

	L getListing();

	boolean isCancelled();

	void setCancelled(boolean flag);

}
