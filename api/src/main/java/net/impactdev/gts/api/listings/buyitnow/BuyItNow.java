package net.impactdev.gts.api.listings.buyitnow;

import net.impactdev.gts.api.listings.prices.Price;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.gts.api.listings.Listing;

import java.util.UUID;

/**
 * A QuickPurchase listing is the typical listing GTS provides. In general, this listing provides a one-time
 * purchase for users who wish to simply avoid participating in auctions. Therefore, the price set for
 * this listing option will be final and unmodifiable.
 */
public interface BuyItNow extends Listing {

	/**
	 * Represents the price of this listing. This will be what a purchasing player must pay in order to buy this
	 * listing off the GTS.
	 *
	 * @return The price of the listing
	 */
	Price<?, ?, ?> getPrice();

	/**
	 * Represents the user who purchased a BIN listing
	 *
	 * @return The UUID of the user who purchased this BIN listing.
	 */
	UUID purchaser();

	/**
	 * Represents that this BIN listing exists only due to a failed attempt to redeem a listing
	 * directly after a purchase. This listing should only attempt to return the entry, rather than
	 * the price to the listing owner, who in this case will now be the seller.
	 *
	 * @return True if stashed for the purchaser, false otherwise
	 */
	boolean stashedForPurchaser();

	/**
	 * Specifies whether or not this listing has been purchased
	 *
	 * @return True if purchased, false otherwise
	 */
	boolean isPurchased();

	/**
	 * Marks a listing as purchased
	 */
	void markPurchased();

	static BuyItNowBuilder builder() {
		return Impactor.getInstance().getRegistry().createBuilder(BuyItNowBuilder.class);
	}

	interface BuyItNowBuilder extends ListingBuilder<BuyItNow, BuyItNowBuilder> {

		/**
		 * Sets the price of this BIN listing
		 *
		 * @param price The price for the listing
		 * @return The updated builder
		 */
		BuyItNowBuilder price(Price<?, ?, ?> price);

		/**
		 * Indicates that the built listing has been purchased
		 *
		 * @return The updated builder
		 */
		BuyItNowBuilder purchased();

		/**
		 * Sets the user who purchased this listing
		 *
		 * @param purchaser The ID of the purchaser
		 * @return The updated builder
		 */
		BuyItNowBuilder purchaser(UUID purchaser);

		/**
		 * Sets this BIN listing as a listing specifically set to be returned to the purchaser,
		 * likely due to a failure to reward the purchaser with what they purchased.
		 *
		 * @return The updated builder
		 */
		BuyItNowBuilder stashedForPurchaser();

		BuyItNowBuilder from(BuyItNow parent);

	}

}
