package com.nickimpact.gts.api.gui;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;

/**
 * Represents the action taken by a player when they click within an inventory. This class serves
 * as a wrapper holding the Player and event itself. While just the event would do, having an easier
 * call to the player isn't necessarily the worst thing either.
 *
 * @author NickImpact
 */
@SuppressWarnings("WeakerAccess")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class Clickable {

	@Getter private final Player player;
	@Getter private final ClickInventoryEvent event;
}
