package me.nickimpact.gts.api.storage;

import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.SoldListing;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface GTSStorage {

	void init();

	void shutdown();

	Map<String, String> getMeta();

	CompletableFuture<Boolean> addListing(Listing listing);

	CompletableFuture<Boolean> deleteListing(UUID uuid);

	CompletableFuture<List<Listing>> getListings();

	CompletableFuture<Boolean> addIgnorer(UUID uuid);

	CompletableFuture<Boolean> removeIgnorer(UUID uuid);

	CompletableFuture<List<UUID>> getAllIgnorers();

	CompletableFuture<Boolean> addToSoldListings(UUID owner, SoldListing listing);

	CompletableFuture<List<SoldListing>> getAllSoldListingsForPlayer(UUID uuid);

	CompletableFuture<Boolean> deleteSoldListing(UUID id, UUID owner);

	CompletableFuture<Boolean> purge();
}
