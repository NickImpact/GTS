package com.nickimpact.gts.api.listings.entries;

import com.nickimpact.gts.api.listings.pricing.PricingException;
import com.nickimpact.gts.entries.prices.MoneyPrice;
import org.spongepowered.api.entity.living.player.Player;

public interface Minable {

	MoneyPrice calcMinPrice() throws PricingException;
}
