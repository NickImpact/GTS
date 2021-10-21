package net.impactdev.gts.api.storage;

import net.impactdev.gts.api.deliveries.Delivery;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.messaging.message.type.admin.ForceDeleteMessage;
import net.impactdev.gts.api.messaging.message.type.auctions.AuctionMessage;
import net.impactdev.gts.api.messaging.message.type.deliveries.ClaimDelivery;
import net.impactdev.gts.api.messaging.message.type.listings.BuyItNowMessage;
import net.impactdev.gts.api.messaging.message.type.listings.ClaimMessage;
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

	CompletableFuture<Map<String, String>> getMeta();

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
	 * Sends the delivery out to its intended recipient, storing it within their stash for them
	 * to claim when they wish to.
	 *
	 * @param delivery The delivery to send
	 * @return A completable future, which has no distinct return value
	 */
	CompletableFuture<Boolean> sendDelivery(Delivery delivery);

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

	CompletableFuture<Boolean> hasMaxListings(UUID user);

	CompletableFuture<Boolean> purge();

	/**
	 * Purges remaining data from the legacy database if it persists.
	 *
	 * @return True if the table existed and was deleted, false otherwise
	 * @deprecated This is a temporary function, and is scheduled to be removed at earliest convenience
	 */
	@Deprecated
	CompletableFuture<Boolean> clean();

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

	/**
	 * Indicates a user is attempting to claim something from a listing within their stash. This message
	 * is common to both auctions and BIN listings, with auctions receiving an extended response, via
	 * {@link ClaimMessage.Response.AuctionResponse}.
	 *
	 * @param request
	 * @return
	 */
	CompletableFuture<ClaimMessage.Response> processClaimRequest(ClaimMessage.Request request);

	CompletableFuture<Boolean> appendOldClaimStatus(UUID auction, boolean lister, boolean winner, List<UUID> others);

	CompletableFuture<AuctionMessage.Cancel.Response> processAuctionCancelRequest(AuctionMessage.Cancel.Request request);

	CompletableFuture<BuyItNowMessage.Remove.Response> processListingRemoveRequest(BuyItNowMessage.Remove.Request request);

	CompletableFuture<BuyItNowMessage.Purchase.Response> processPurchase(BuyItNowMessage.Purchase.Request request);

	CompletableFuture<ClaimDelivery.Response> claimDelivery(ClaimDelivery.Request request);

	//------------------------------------------------------------------------------------------------------------------
	//
	//  Admin based message processing
	//
	//------------------------------------------------------------------------------------------------------------------
	CompletableFuture<ForceDeleteMessage.Response> processForcedDeletion(ForceDeleteMessage.Request request);
}
