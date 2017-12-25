package com.nickimpact.gts.api.gui;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;

@SuppressWarnings("WeakerAccess")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class Clickable {

	@Getter private final Player player;

	@Getter private final ClickInventoryEvent event;
}
