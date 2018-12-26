package me.nickimpact.gts.api.listings.entries;

import me.nickimpact.gts.entries.prices.MoneyPrice;

import java.math.BigDecimal;

public interface Minable {

	MoneyPrice calcMinPrice();

	default boolean isValid(BigDecimal proposed) {
		return proposed.compareTo(calcMinPrice().getPrice()) >= 0;
	}
}
