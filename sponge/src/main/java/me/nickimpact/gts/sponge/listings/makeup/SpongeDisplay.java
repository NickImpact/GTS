package me.nickimpact.gts.sponge.listings.makeup;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.nickimpact.gts.api.listings.makeup.Display;
import org.spongepowered.api.item.inventory.ItemStack;

@Getter
@RequiredArgsConstructor
public class SpongeDisplay implements Display<ItemStack> {

	private final ItemStack display;

}
