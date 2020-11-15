package net.impactdev.gts.api.storage;

import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.messaging.message.type.auctions.AuctionMessage;
import net.impactdev.gts.api.messaging.message.type.listings.BuyItNowMessage;
import net.impactdev.gts.api.player.PlayerSettings;
import net.impactdev.gts.api.stashes.Stash;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public interface GTSStorage {

	void init();

	void shutdown();

	Map<String, String> getMeta();

	CompletableFuture<Boolean> publishListing(Listing listing);

	CompletableFuture<Optional<Listing>> getListing(UUID listing);

	/**
	 * Fetches all listings, with no filters applied. This is to allow all listings to be processed and accessible,
	 * despite expiration of a listing or another filter that would be typically applied against it.
	 *
	 * @return Every listing currently stored in the database
	 */
	default CompletableFuture<List<Listing>> fetchListings() {
		return this.fetchListings(Collections.emptyList());
	}

	/**
	 * Retrieves all listings, and applies any filters against the returned set. These filters can be situations
	 * like retrieving only listings that have not yet expired.
	 *
	 * @param filters Any filters to apply against the received set of listings.
	 * @return Every listing meeting the conditions specified by the passed in filters
	 */
	CompletableFuture<List<Listing>> fetchListings(Collection<Predicate<Listing>> filters);

	/**
	 * Retrieves the stash of the user who holds it. The stash of a user contains items purchased
	 * that they couldn't receive at the time, or listings of theirs that have expired over time.
	 *
	 * @param user The user representing the holder of the stash
	 * @return The stash as it is currently
	 */
	CompletableFuture<Stash> getStash(UUID user);

	CompletableFuture<Optional<PlayerSettings>> getPlayerSettings(UUID uuid);

	CompletableFuture<Boolean> applyPlayerSettings(UUID uuid, PlayerSettings settings);

	CompletableFuture<Boolean> purge();


	//------------------------------------------------------------------------------------------------------------------
	//
	//  Any and all actions that might require a message to transmit the request to the storage provider
	//
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Attempts to send a request to the database to process a bid on an auction. Given that the request and response
	 * should feature raw json data, the return type of a message response allows for the data to be passed along
	 * to the requesting server.
	 *
	 * @param request The bid request being made by a connected server
	 * @return A response indicating the success or failure of the bid request, fit with all necessary information
	 */
	CompletableFuture<AuctionMessage.Bid.Response> processBid(AuctionMessage.Bid.Request request);

	CompletableFuture<AuctionMessage.Claim.Response> processAuctionClaimRequest(AuctionMessage.Claim.Request request);

	CompletableFuture<AuctionMessage.Cancel.Response> processAuctionCancelRequest(AuctionMessage.Cancel.Request request);

	CompletableFuture<BuyItNowMessage.Remove.Response> processListingRemoveRequest(BuyItNowMessage.Remove.Request request);

	CompletableFuture<BuyItNowMessage.Purchase.Response> processPurchase(BuyItNowMessage.Purchase.Request request);

}
