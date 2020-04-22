package me.nickimpact.gts.manager;

import com.google.common.collect.Lists;
import me.nickimpact.gts.api.listings.manager.ListingManager;
import me.nickimpact.gts.sponge.listings.SpongeListing;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SpongeListingManager implements ListingManager<SpongeListing> {

	@Override
	public List<UUID> getIgnorers() {
		return null;
	}

	@Override
	public boolean addToMarket(UUID lister, SpongeListing listing) {
		return false;
	}

	@Override
	public boolean purchase(UUID buyer, SpongeListing listing) {
		return false;
	}

	@Override
	public void deleteListing(SpongeListing listing) {

	}

	@Override
	public boolean hasMaxListings(UUID lister) {
		return false;
	}

	@Override
	public CompletableFuture<List<SpongeListing>> fetchListings() {
		return CompletableFuture.supplyAsync(() -> {
			try {
				Thread.sleep(2500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return Lists.newArrayList();
		});
	}

}
