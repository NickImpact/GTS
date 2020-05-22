package me.nickimpact.gts.util.items;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.SkullTypes;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.property.ProfileProperty;

import java.util.UUID;

public class SkullCreator {

	public static ItemStack fromBase64(String base) {
		return ItemStack.builder()
				.itemType(ItemTypes.SKULL)
				.add(Keys.SKULL_TYPE, SkullTypes.PLAYER)
				.add(Keys.REPRESENTED_PLAYER, GameProfile.of(UUID.randomUUID())
						.addProperty("textures", ProfileProperty.of(
								"globe",
								"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv"
								+ base
						))
				)
				.build();
	}

}
