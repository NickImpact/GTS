package me.nickimpact.gts.sponge.pricing;

import me.nickimpact.gts.api.listings.prices.Price;
import me.nickimpact.gts.api.listings.prices.PriceValue;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

public interface SpongePrice<T extends PriceValue> extends Price<T, ItemStack> {}
