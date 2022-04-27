package net.impactdev.gts.sponge.utils.items;

import org.spongepowered.api.data.Keys;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.property.ProfileProperty;

import java.util.UUID;

public class SkullCreator {

	public static ItemStack fromBase64(String base) {
		return fromProfile(GameProfile.of(UUID.randomUUID())
				.withProperty(ProfileProperty.of(
						"textures",
						"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv"
								+ base
				)));
	}

	public static ItemStack fromProfile(GameProfile profile) {
		return ItemStack.builder()
				.itemType(ItemTypes.PLAYER_HEAD)
				.add(Keys.GAME_PROFILE, profile)
				.build();
	}

}
