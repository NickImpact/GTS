package com.nickimpact.gts.utils;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;

import java.util.Optional;
import java.util.UUID;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class PlayerUtils {

	public static Optional<User> getUserFromUUID(UUID uuid) {
		return Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(uuid);
	}
}
