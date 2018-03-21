package com.nickimpact.gts.api.listings.entries;

import com.nickimpact.gts.api.listings.pricing.PricingException;
import com.nickimpact.gts.entries.prices.MoneyPrice;

public interface Minable {

	MoneyPrice calcMinPrice() throws PricingException;
}
