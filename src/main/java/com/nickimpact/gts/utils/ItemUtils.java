package com.nickimpact.gts.utils;

import com.nickimpact.gts.enums.EnumTextures;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.SkullTypes;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.UUID;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class ItemUtils {

	/**
	 * Creates a player head with a texture specific to that of the UUID passed in.
	 *
	 * @param uuid The UUID of the user to place on the skull
	 * @param name The display name of the skull
	 * @param lore The display lore of the skull
	 * @return A skull with the assigned texture
	 */
	public static ItemStack createSkull(UUID uuid, Text name, List<Text> lore) {
		return ItemStack.builder()
				.itemType(ItemTypes.SKULL)
				.add(Keys.DISPLAY_NAME, name)
				.add(Keys.ITEM_LORE, lore)
				.add(Keys.SKULL_TYPE, SkullTypes.PLAYER)
				.add(Keys.REPRESENTED_PLAYER, GameProfile.of(uuid))
				.build();
	}

	/**
	 * Creates a player head with the attached texture applied to the skull.
	 *
	 * @param texture The texture of the skull
	 * @param name The display name of the skull
	 * @param lore The display lore of the skull
	 * @return A skull with the assigned texture
	 */
	public static ItemStack createSkull(EnumTextures texture, Text name, List<Text> lore) {
		GameProfile profile = GameProfile.of(UUID.randomUUID());
		profile.addProperty(ProfileProperty.of("textures", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" + texture.value));
		return ItemStack.builder()
				.itemType(ItemTypes.SKULL)
				.add(Keys.DISPLAY_NAME, name)
				.add(Keys.ITEM_LORE, lore)
				.add(Keys.REPRESENTED_PLAYER, profile)
				.add(Keys.SKULL_TYPE, SkullTypes.PLAYER)
				.build();
	}
}
