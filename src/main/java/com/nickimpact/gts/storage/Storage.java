package com.nickimpact.gts.storage;

import com.nickimpact.gts.api.listings.Listing;
import com.nickimpact.gts.api.listings.entries.Entry;
import com.nickimpact.gts.api.listings.entries.EntryHolder;
import com.nickimpact.gts.api.listings.pricing.Price;
import com.nickimpact.gts.api.listings.pricing.PriceHolder;
import com.nickimpact.gts.logs.Log;

import java.util.List;
import java.util.UUID;
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

	/**
	 * Add a listing to the storage provider
	 *
	 * @param listing The target listing
	 * @return A completable future in which a listing is added to the storage provider
	 */
	CompletableFuture<Void> addListing(Listing listing);

	/**
	 * Remove a listing from the storage provider based on the ID of a listing
	 *
	 * @param id The target ID of a listing
	 * @return A completable future in which a listing is removed from the storage provider
	 */
    CompletableFuture<Void> removeListing(int id);

    /**
     * Fetches a list of all listings from the storage provider, and forms them into a list
     * to represent the cached listings.
     */
    CompletableFuture<List<Listing>> getListings();

	/**
	 * Add a log to the storage provider
	 *
	 * @param log The target log
	 * @return A completable future in which a log is added to the storage provider
	 */
	CompletableFuture<Void> addLog(Log log);

	/**
	 * Remove a log from the storage provider based on the ID of a log
	 *
	 * @param id The target ID of a log
	 * @return A completable future in which a log is removed from the storage provider
	 */
	CompletableFuture<Void> removeLog(int id);

	/**
	 * Fetches all logs within the storage data, and adds them to the gts log cache
	 */
	CompletableFuture<List<Log>> getLogs();

	/**
	 * In the event an {@link Entry} is unable to be given to a user, due to it not supporting
	 * offline rewarding, we will store the entry into storage until their next login.
	 * That way, we can ensure the user receives their reward in the event the server
	 * closes down for any reason.
	 *
	 * @param holder A wrapper which holds the user's UUID along with the entry they are to receive
	 * @return A completable future in which an entry is to be stored temporarily within the storage provider
	 */
	CompletableFuture<Void> addHeldElement(EntryHolder holder);

	/**
	 * Removes an {@link Entry} from the storage provider based on the user's UUID and the entry itself.
	 * This method should only be called after a player has received the reward they originally did not.
	 *
	 * @param holder A wrapper which holds the user's UUID along with the entry they are to receive
	 * @return A completable future in which an entry is removed from temporary storage
	 */
	CompletableFuture<Void> removeHeldElement(EntryHolder holder);

	/**
	 * Fetches all of the held elements from the storage provider.
	 *
	 * @return All of the held elements from the storage provider.
	 */
	CompletableFuture<List<EntryHolder>> getHeldElements();

	/**
	 * In the event a {@link Price} is unable to be given to a receiver, due to it not supporting
	 * offline rewarding, we will store the entry into storage until their next login.
	 * That way, we can ensure the user receives their reward in the event the server
	 * closes down for any reason.
	 *
	 * @param holder A wrapper which holds the user's UUID along with the price they are to receive
	 * @return A completable future in which a price is to be stored temporarily within the storage provider
	 */
	CompletableFuture<Void> addHeldPrice(PriceHolder holder);

	/**
	 * Removes a {@link Price} from the storage provider based on the user's UUID and the price itself.
	 * This method should only be called after a player has received the reward they originally did not.
	 *
	 * @param holder A wrapper which holds the user's UUID along with the entry they are to receive
	 * @return A completable future in which an price is removed from temporary storage
	 */
	CompletableFuture<Void> removeHeldPrice(PriceHolder holder);

	/**
	 * Fetches all of the held prices from the storage provider.
	 *
	 * @return All of the held prices from the storage provider.
	 */
	CompletableFuture<List<PriceHolder>> getHeldPrices();

	CompletableFuture<Void> addIgnorer(UUID uuid);

	CompletableFuture<Void> removeIgnorer(UUID uuid);

	/**
	 * Fetches a list of UUIDs that prefer not to be bothered by GTS broadcasts
	 *
	 * @return A set of users ignoring GTS broadcasts
	 */
	CompletableFuture<List<UUID>> getIgnorers();

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
