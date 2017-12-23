package com.nickimpact.gts.api.listings.pricing;

/** This interface represents the base functions to a auction-styled listing. */
public interface IAuctionPrice {

	/**
	 * Represents the current status of an entry's auction increment
	 *
	 * @return The current status of an auction's increment price
	 */
	Price getIncrement();

	/**
	 * Updates the current value of an auction's increment based on the following
	 * price passed in.
	 *
	 * @param price The price used to increment the auction's current value
	 */
	void updateIncrement(Price price);
}
