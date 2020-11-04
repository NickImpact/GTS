package net.impactdev.gts.api.listings.auctions;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import net.impactdev.gts.api.listings.entries.Entry;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.utilities.Builder;
import net.impactdev.impactor.api.utilities.mappings.Tuple;
import net.impactdev.gts.api.listings.Listing;

import java.time.LocalDateTime;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.UUID;

/**
 * An auction represents a listing which, instead of being directly purchasable, will be able to fluctuate its price
 * based on any user which desires to place a bid on the listing. These listings will still only have one winner by the
 * end of the expiration period, but it is not limited to just one user making an attempt to purchase the listing.
 *
 * <p>As a note, these types of listings must have an expiration time set. No auction can be marked permanent, as
 * they cannot be directly purchased.</p>
 */
public interface Auction extends Listing {

	/**
	 * Attempts to locate the highest bid, or any bid at all, placed by the user on this auction. Only the highest
	 * bid will be returned here. If no bids exist for the user, this will return an empty Optional. Otherwise, this
	 * call will report the amount of money they placed already on the bid.
	 *
	 * @param uuid The ID of the user placing the bid
	 * @return An Optional value containing the bid amount placed by the user, or an empty Optional to signify no bid
	 * has been placed by the user.
	 */
	Optional<Double> getCurrentBid(UUID uuid);

	/**
	 * Specifies the number of bids that have been placed on this auction.
	 *
	 * @return The number of bids placed on the auction
	 */
	default int getNumberOfBidsPlaced() {
		return this.getBids().size();
	}

	/**
	 * Specifies if any bids have been placed on the auction
	 *
	 * @return True if any have been placed, false otherwise
	 */
	default boolean hasAnyBidsPlaced() {
		return this.getNumberOfBidsPlaced() != 0;
	}

	/**
	 * Keeps track of the bids placed on this auction, with the highest bid being the first entry in the sorted map,
	 * and the lowest being the last entry.
	 *
	 * @return A mapping of bids placed on this auction by a user and for how much they bid
	 */
	TreeMultimap<UUID, Double> getBids();

	/**
	 * Returns the highest bid currently placed on this auction. The high bid at time of expiration marks the winner,
	 * so having an easy call to this allows for simple access later on.
	 *
	 * @return The highest bidder paired with the amount they bid
	 */
	Optional<Tuple<UUID, Double>> getHighBid();

	/**
	 * Specifies the starting price of this auction. This is mainly here for tracking, and can be represented
	 * by {@link #getCurrentPrice()}.
	 *
	 * @return The price of the auction when initially created
	 */
	double getStartingPrice();

	/**
	 * Specifies the current price of this auction. If no bids are currently placed, this will return the initial
	 * starting price set on the auction. Otherwise, this will show the highest bid currently placed on the auction.
	 *
	 * @return The current price of the auction
	 */
	double getCurrentPrice();

	/**
	 * Specifies the rate at which continuous bids will be applied. In other words, this value is a percentage value
	 * meant to help scale the higher the price of the item gets. This will always round up to the next whole number,
	 * so, if you start an auction with a price of $1, and have an increment rate of 5%, this will still lead to $2.
	 *
	 * @return The increment to apply to the next bid
	 */
	float getIncrement();

	/**
	 * Allows a user to bid on the listing for the amount specified. As a user could specify a custom amount to bid, this
	 * call must accept a dynamic value for the amount bid. As such, this call should also verify if the amount bid is
	 * actually valid.
	 *
	 * @param user The user placing the bid
	 * @param amount The amount of money they are bidding
	 * @return True if their bid was applied, false otherwise
	 */
	boolean bid(UUID user, double amount);

	static AuctionBuilder builder() {
		return Impactor.getInstance().getRegistry().createBuilder(AuctionBuilder.class);
	}

	interface AuctionBuilder extends Builder<Auction, AuctionBuilder> {

		AuctionBuilder id(UUID id);

		AuctionBuilder lister(UUID lister);

		AuctionBuilder entry(Entry<?, ?> entry);

		AuctionBuilder published(LocalDateTime published);

		AuctionBuilder expiration(LocalDateTime expiration);

		AuctionBuilder start(double amount);

		AuctionBuilder increment(float rate);

		AuctionBuilder current(double current);

		AuctionBuilder bids(Multimap<UUID, Double> bids);

	}

}
