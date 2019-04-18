package me.nickimpact.gts.api.listings;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ListingManager<E extends Listing> {

	Optional<E> getListingByID(UUID uuid);

	List<E> getListings();

	boolean addToMarket(UUID lister, E listing);

	boolean hasMaxListings(UUID lister);

	void readStorage();
}
