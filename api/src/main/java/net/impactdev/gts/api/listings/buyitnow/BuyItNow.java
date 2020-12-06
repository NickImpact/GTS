package net.impactdev.gts.api.listings.buyitnow;

import net.impactdev.gts.api.listings.prices.Price;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.gts.api.listings.Listing;

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

		BuyItNowBuilder price(Price<?, ?, ?> price);

		/**
		 * Indicates that the built listing has been purchased
		 *
		 * @return The builder after marking the listing as purchased
		 */
		BuyItNowBuilder purchased();

	}

}
