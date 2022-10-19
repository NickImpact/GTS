package net.impactdev.gts.api.storage;

import net.impactdev.gts.api.components.deliveries.Delivery;
import net.impactdev.gts.api.components.listings.models.Listing;
import net.impactdev.gts.api.components.stash.Stash;
import net.impactdev.gts.api.communication.message.type.admin.ForceDeleteMessage;
import net.impactdev.gts.api.communication.message.type.auctions.AuctionMessage;
import net.impactdev.gts.api.communication.message.type.deliveries.ClaimDelivery;
import net.impactdev.gts.api.communication.message.type.listings.BuyItNowMessage;
import net.impactdev.gts.api.communication.message.type.listings.ClaimMessage;
import net.impactdev.gts.api.players.PlayerPreferences;
import net.impactdev.gts.api.storage.communication.CommunicationProvider;
import net.kyori.adventure.text.ComponentLike;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public interface StorageProvider {

	/**
	 * Initializes the storage provider. This is responsible for establishing any connections necessary
	 * to work with the target storage platform.
	 */
	void init();

	/**
	 * Responsible for shutting down the storage provider. This should clean up any resources in use
	 * in order to enact a safe close of communication.
	 */
	void shutdown();

	/**
	 * Represents a layer of communication for the storage provider which is responsible for
	 * processing request-based actions.
	 *
	 * @return
	 */
	CommunicationProvider communications();

	CompletableFuture<Map<String, ComponentLike>> meta();

	CompletableFuture<Boolean> publish(Listing listing);

	CompletableFuture<Optional<Listing>> locate(UUID listing);

	/**
	 * Fetches all listings, with no filters applied. This is to allow all listings to be processed and accessible,
	 * despite expiration of a listing or another filter that would be typically applied against it.
	 *
	 * @return Every listing currently stored in the database
	 */
	default CompletableFuture<List<Listing>> receive() {
		return this.receive(Collections.emptyList());
	}

	/**
	 * Retrieves all listings, and applies any filters against the returned set. These filters can be situations
	 * like retrieving only listings that have not yet expired.
	 *
	 * @param filters Any filters to apply against the received set of listings.
	 * @return Every listing meeting the conditions specified by the passed in filters
	 */
	CompletableFuture<List<Listing>> receive(Collection<Predicate<Listing>> filters);

	/**
	 * Sends the delivery out to its intended recipient, storing it within their stash for them
	 * to claim when they wish to.
	 *
	 * @param delivery The delivery to send
	 * @return A completable future, which has no distinct return value
	 */
	CompletableFuture<Boolean> deliver(Delivery delivery);

	/**
	 * Retrieves the stash of the user who holds it. The stash of a user contains items purchased
	 * that they couldn't receive at the time, or listings of theirs that have expired over time.
	 *
	 * @param user The user representing the holder of the stash
	 * @return The stash as it is currently
	 */
	CompletableFuture<Stash> stash(UUID user);

	CompletableFuture<Optional<PlayerPreferences>> settings(UUID uuid);

	CompletableFuture<Boolean> applySettings(UUID uuid, PlayerPreferences settings);

	CompletableFuture<Boolean> hasMaxListings(UUID user);

	//------------------------------------------------------------------------------------------------------------------
	//
	//  Any and all actions that might require a message to transmit the request to the storage provider
	//
	//------------------------------------------------------------------------------------------------------------------


}
