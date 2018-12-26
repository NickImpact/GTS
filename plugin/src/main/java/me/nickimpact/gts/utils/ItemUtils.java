package me.nickimpact.gts.utils;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.SkullTypes;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.profile.GameProfile;
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
		ItemStack.Builder sb = ItemStack.builder()
				.itemType(ItemTypes.SKULL)
				.add(Keys.DISPLAY_NAME, name)
				.add(Keys.ITEM_LORE, lore)
				.add(Keys.SKULL_TYPE, SkullTypes.PLAYER);

		try {
			return sb.add(Keys.REPRESENTED_PLAYER, GameProfile.of(uuid)).build();
		} catch (Exception e) {
			return sb.build();
		}
	}
}
