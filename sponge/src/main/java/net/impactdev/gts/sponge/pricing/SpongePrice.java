package net.impactdev.gts.sponge.pricing;

import net.impactdev.gts.api.listings.prices.Price;
import org.spongepowered.api.item.inventory.ItemStack;

public interface SpongePrice<E, S> extends Price<E, S, ItemStack> {}
