package com.nickimpact.gts.api.listings;

import org.spongepowered.api.item.inventory.ItemStack;

public interface Displayable {

	ItemStack getPrimaryDisplay();

	ItemStack getConfirmDisplay();
}
