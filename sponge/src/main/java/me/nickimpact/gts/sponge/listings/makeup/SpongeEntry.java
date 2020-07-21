package me.nickimpact.gts.sponge.listings.makeup;

import lombok.AllArgsConstructor;
import me.nickimpact.gts.api.listings.entries.Entry;
import org.spongepowered.api.item.inventory.ItemStack;

@AllArgsConstructor
public abstract class SpongeEntry<T, D> implements Entry<T, D, ItemStack> {

	private final D data;

	@Override
	public D getInternalData() {
		return this.data;
	}

}
