package me.nickimpact.gts.api.storage;

import me.nickimpact.gts.api.listings.Listing;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IGtsStorage {

	void init();

	void shutdown();

	Map<String, String> getMeta();

	CompletableFuture<Boolean> addListing(Listing listing);

	CompletableFuture<Boolean> deleteListing(UUID uuid);

	CompletableFuture<List<Listing>> getListings();

	CompletableFuture<Boolean> addIgnorer(UUID uuid);

	CompletableFuture<Boolean> removeIgnorer(UUID uuid);

	CompletableFuture<Boolean> isIgnoring(UUID uuid);

	CompletableFuture<Boolean> purge();
}
