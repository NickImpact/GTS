package me.nickimpact.gts.api.utils;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;

public class ItemUtils {

	public static ItemType getOrDefaultFromRegistryID(String id) {
		return Sponge.getRegistry().getType(ItemType.class, id).orElse(ItemTypes.BARRIER);
	}
}
