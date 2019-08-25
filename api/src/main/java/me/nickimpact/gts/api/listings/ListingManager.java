package me.nickimpact.gts.api.listings;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ListingManager<E extends Listing> {

	Optional<E> getListingByID(UUID uuid);

	List<E> getListings();

	List<UUID> getIgnorers();

	boolean addToMarket(UUID lister, E listing);

	boolean purchase(UUID buyer, E listing);

	void deleteListing(E listing);

	boolean hasMaxListings(UUID lister);

	void readStorage();
}
