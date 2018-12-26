package me.nickimpact.gts.api.listings.entries;

import me.nickimpact.gts.api.listings.pricing.Price;

public interface Maxable<T, E extends Price<T>> {

	T getMax();

	boolean isLowerOrEqual();
}
