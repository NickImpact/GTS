package me.nickimpact.gts.api.storage;

import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.SoldListing;
import me.nickimpact.gts.api.messaging.message.type.auctions.AuctionMessage;

import java.net.SocketAddress;
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
}
