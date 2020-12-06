package net.impactdev.gts.sponge.listings.makeup;

import lombok.RequiredArgsConstructor;
import net.impactdev.gts.api.listings.makeup.Display;
import org.spongepowered.api.item.inventory.ItemStack;

@RequiredArgsConstructor
public class SpongeDisplay implements Display<ItemStack> {

	private final ItemStack display;

	@Override
	public ItemStack get() {
		return this.display;
	}
}
