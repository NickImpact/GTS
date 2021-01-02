package net.impactdev.gts.sponge.listings.makeup;

import net.impactdev.gts.api.listings.makeup.Display;
import org.spongepowered.api.item.inventory.ItemStack;

public class SpongeDisplay implements Display<ItemStack> {

	private final ItemStack display;

	public SpongeDisplay(ItemStack display) {
		this.display = display;
	}

	@Override
	public ItemStack get() {
		return this.display;
	}
}
