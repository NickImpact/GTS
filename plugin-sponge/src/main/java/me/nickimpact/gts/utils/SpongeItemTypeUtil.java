package me.nickimpact.gts.utils;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;

import java.util.Optional;

public class SpongeItemTypeUtil {

	public static Optional<ItemType> getItemTypeFromID(String id) {
		return Sponge.getRegistry().getType(ItemType.class, id);
	}

	public static ItemType getOrDefaultItemTypeFromID(String id) {
		return getItemTypeFromID(id).orElse(ItemTypes.BARRIER);
	}
}
