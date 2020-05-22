package me.nickimpact.gts.bungee.listings;

import com.google.common.collect.Lists;
import me.nickimpact.gts.api.listings.manager.ListingManager;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BungeeListingManager implements ListingManager<BungeeListing> {

	private List<BungeeListing> listings = Lists.newArrayList();

	@Override
	public List<UUID> getIgnorers() {
		return null;
	}

	@Override
	public boolean addToMarket(UUID lister, BungeeListing listing) {
		return false;
	}

	@Override
	public boolean purchase(UUID buyer, BungeeListing listing) {
		return false;
	}

	@Override
	public void deleteListing(BungeeListing listing) {

	}

	@Override
	public boolean hasMaxListings(UUID lister) {
		return false;
	}

	@Override
	public CompletableFuture<List<BungeeListing>> fetchListings() {
		return null;
	}

}
