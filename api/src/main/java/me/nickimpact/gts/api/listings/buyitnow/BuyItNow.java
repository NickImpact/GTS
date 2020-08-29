package me.nickimpact.gts.api.listings.buyitnow;

import com.nickimpact.impactor.api.Impactor;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.prices.Price;

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
	Price<?, ?> getPrice();

	static BuyItNowBuilder builder() {
		return Impactor.getInstance().getRegistry().createBuilder(BuyItNowBuilder.class);
	}

	interface BuyItNowBuilder extends ListingBuilder<BuyItNow, BuyItNowBuilder> {

		BuyItNowBuilder price(Price<?, ?> price);

	}

}
