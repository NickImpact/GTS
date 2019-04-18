package me.nickimpact.gts.events;

import lombok.RequiredArgsConstructor;
import me.nickimpact.gts.api.events.ListEvent;
import me.nickimpact.gts.spigot.SpigotListing;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Optional;

@RequiredArgsConstructor
public class SpigotListingEvent extends Event implements ListEvent<Player, SpigotListing>, Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private final Player player;
	private final SpigotListing listing;
	private boolean cancelled;

	@Override
	public Optional<Player> getPlayer() {
		return Optional.ofNullable(player);
	}

	@Override
	public SpigotListing getListing() {
		return this.listing;
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	@Override
	public void setCancelled(boolean flag) {
		this.cancelled = flag;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
