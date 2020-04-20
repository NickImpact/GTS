package me.nickimpact.gts.api.listings.manager;

import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.services.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ListingManager<E extends Listing> extends Service {

	List<UUID> getIgnorers();

	boolean addToMarket(UUID lister, E listing);

	boolean purchase(UUID buyer, E listing);

	void deleteListing(E listing);

	boolean hasMaxListings(UUID lister);

	CompletableFuture<List<E>> fetchListings();

}
