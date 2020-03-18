package me.nickimpact.gts.events;

import me.nickimpact.gts.api.events.ListEvent;
import me.nickimpact.gts.sponge.listings.SpongeListing;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;

import java.util.Optional;

public class SpongeListingEvent implements Event, ListEvent<Player, SpongeListing> {

	private Player player;
	private SpongeListing listing;

	private boolean cancelled;

	public SpongeListingEvent(Player player, SpongeListing listing) {
		this.player = player;
		this.listing = listing;
	}

	@Override
	public Optional<Player> getPlayer() {
		return Optional.ofNullable(this.player);
	}

	@Override
	public SpongeListing getListing() {
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
	public Cause getCause() {
		return Sponge.getCauseStackManager().getCurrentCause();
	}
}
