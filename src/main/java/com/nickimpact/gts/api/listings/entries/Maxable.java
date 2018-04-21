package com.nickimpact.gts.api.listings.entries;

import com.nickimpact.gts.api.listings.pricing.Price;

public interface Maxable<T, E extends Price<T>> {

	T getMax();

	boolean isLowerOrEqual();
}
