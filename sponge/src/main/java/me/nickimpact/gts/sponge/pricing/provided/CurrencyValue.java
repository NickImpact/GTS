package me.nickimpact.gts.sponge.pricing.provided;

import lombok.RequiredArgsConstructor;
import me.nickimpact.gts.api.listings.prices.PriceValue;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class CurrencyValue implements PriceValue<BigDecimal> {

	private final BigDecimal value;

	@Override
	public BigDecimal getPricable() {
		return this.value;
	}

	@Override
	public boolean supportsOffline() {
		return true;
	}

}
