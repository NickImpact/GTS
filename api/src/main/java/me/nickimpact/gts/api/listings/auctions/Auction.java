package me.nickimpact.gts.api.listings.auctions;

import me.nickimpact.gts.api.GTSService;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.makeup.Display;
import me.nickimpact.gts.api.util.Builder;
import me.nickimpact.gts.api.util.Tuple;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.SortedMap;
import java.util.UUID;

/**
 * An auction represents a listing which, instead of being directly purchasable, will be able to fluctuate its price
 * based on any user which desires to place a bid on the listing. These listings will still only have one winner by the
 * end of the expiration period, but it is not limited to just one user making an attempt to purchase the listing.
 *
 * <p>As a note, these types of listings must have an expiration time set. No auction can be marked permanent, as
 * they cannot be directly purchased.</p>
 */
public interface Auction  {

	/**
	 * Represents the ID of this listing. This is independent of the lister's UUID, which serves as a reference
	 * to the player or system that listed the listing.
	 *
	 * @return The unique ID of this listing
	 */
	UUID getID();

	/**
	 * Represents the UUID of the user listing this Listing. If this is a player, the UUID will be that of the player.
	 * If the server creates the listing, then this UUID will match the generic zeroed out ID.
	 *
	 * @return The ID of the lister who created this listing
	 */
	UUID getLister();

	/**
	 * Represents the actual component of the listing that will be contained by this listing. This is what a user will
	 * be purchasing should they purchase the listing.
	 *
	 * @return The entry making up this listing.
	 */
	Entry getEntry();

	/**
	 * Represents the display of the listing. This is essentially how the listing will be displayed to the user
	 * when queried in-game.
	 *
	 * @return The display parameters of this listing
	 */
	Display getDisplay();

	/**
	 * Represents the time where this listing will expire. In general, each listing will have this option specified.
	 * However, a listing that has been made permanent can avoid this check, and simply stay in the system until
	 * it is either purchased or removed.
	 *
	 * @return An optional value containing the exact point in time in which this listing will expire, or empty to
	 * represent that this listing will never expire.
	 */
	LocalDateTime getExpiration();

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
	 * Keeps track of the bids placed on this auction, with the highest bid being the first entry in the sorted map,
	 * and the lowest being the last entry.
	 *
	 * @return A mapping of bids placed on this auction by a user and for how much they bid
	 */
	SortedMap<UUID, Double> getBids();

	/**
	 * Returns the highest bid currently placed on this auction. The high bid at time of expiration marks the winner,
	 * so having an easy call to this allows for simple access later on.
	 *
	 * @return The highest bidder paired with the amount they bid
	 */
	Tuple<UUID, Double> getHighBid();

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
		return GTSService.getInstance().getRegistry().createBuilder(AuctionBuilder.class);
	}

	interface AuctionBuilder extends Builder<Auction, AuctionBuilder> {

		AuctionBuilder id(UUID id);

		AuctionBuilder lister(UUID lister);

		AuctionBuilder entry(Entry entry);

		AuctionBuilder expiration(LocalDateTime expiration);

		AuctionBuilder start(double amount);

		AuctionBuilder increment(float rate);

	}

}
