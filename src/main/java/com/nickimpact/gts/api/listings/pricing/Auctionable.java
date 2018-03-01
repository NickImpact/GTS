package com.nickimpact.gts.api.listings.pricing;

/**
 * Specifies that a price is able to be used for auctions. For instance, using money as the price
 * is fairly simple to increment, versus pokemon and items have no definitive structure for
 * incrementing, as everyone looks at everything a different way in that category.
 *
 * @author NickImpact
 * @since February 28, 2018
 */
public interface Auctionable<T extends Price> {

	void add(T price);
}
