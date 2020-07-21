package me.nickimpact.gts.api.listings.manager;

import com.nickimpact.impactor.api.services.Service;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.auctions.Auction;
import me.nickimpact.gts.api.listings.buyitnow.BuyItNow;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ListingManager<L extends Listing, A extends Auction, B extends BuyItNow> extends Service {

	List<UUID> getIgnorers();

	CompletableFuture<Boolean> addToMarket(UUID lister, L listing);

	CompletableFuture<Boolean> bid(UUID bidder, A listing, double amount);

	CompletableFuture<Boolean> purchase(UUID buyer, B listing);

	CompletableFuture<Boolean> deleteListing(L listing);

	CompletableFuture<Boolean> hasMaxListings(UUID lister);

	CompletableFuture<List<L>> fetchListings();

}
