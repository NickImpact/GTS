package net.impactdev.gts.api.listings.manager;

import net.impactdev.impactor.api.services.Service;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ListingManager<L extends Listing, A extends Auction, B extends BuyItNow> extends Service {

	CompletableFuture<Boolean> list(UUID lister, L listing);

	CompletableFuture<Boolean> bid(UUID bidder, A listing, double amount);

	<S> CompletableFuture<Boolean> purchase(UUID buyer, B listing, S source);

	CompletableFuture<Boolean> deleteListing(L listing);

	CompletableFuture<Boolean> hasMaxListings(UUID lister);

	CompletableFuture<List<L>> fetchListings();

}
