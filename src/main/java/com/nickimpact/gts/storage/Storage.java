package com.nickimpact.gts.storage;

import com.nickimpact.gts.api.listings.Listing;
import com.nickimpact.gts.logs.Log;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Storage {

    String getName();

    /**
     * This method is to initialize our storage provider, loading anything necessary
     * about the provider.
     */
    void init();

    /**
     * This method is to ensure we properly close our connection when the plugin is either
     * no longer in use or in the middle of server shutdown.
     */
    void shutdown();

    CompletableFuture<Void> addListing(Listing listing);

    CompletableFuture<Void> removeListing(int id);

    /**
     * Fetches a list of all listings from the storage provider, and forms them into a list
     * to represent the cached listings.
     */
    CompletableFuture<List<Listing>> getListings();

	CompletableFuture<Void> addLog(Log log);

	CompletableFuture<Void> removeLog(int id);

	/**
	 * Fetches all logs within the storage data, and adds them to the gts log cache
	 */
	CompletableFuture<List<Log>> getLogs();

    /**
     * This method is meant to clean out the gts, along with logs if the passed variable
     * is <code>true</code>.
     *
     * @return <code>true</code> on successful purge, <code>false</code> otherwise
     */
    CompletableFuture<Void> purge(boolean logs);

    /**
     * Attempts to save all data awaiting an update to the storage provider. However, for flatfile,
     * we will need to ensure we have all data present at time of save to ensure all is saved
     * properly.
     *
     * @return <code>true</code> on successful save, <code>false</code> otherwise
     */
    CompletableFuture<Void> save();
}
