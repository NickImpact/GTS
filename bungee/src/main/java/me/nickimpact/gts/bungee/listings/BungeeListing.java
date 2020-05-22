package me.nickimpact.gts.bungee.listings;

import lombok.Builder;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.common.listings.JsonStoredEntry;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Builder
public class BungeeListing implements Listing {

	private UUID id;
	private UUID lister;
	private JsonStoredEntry entry;
	private LocalDateTime published;
	private LocalDateTime expiration;

	@Override
	public UUID getID() {
		return this.id;
	}

	@Override
	public UUID getLister() {
		return this.lister;
	}

	@Override
	public JsonStoredEntry getEntry() {
		return this.entry;
	}

	@Override
	public LocalDateTime getPublishTime() {
		return this.published;
	}

	@Override
	public Optional<LocalDateTime> getExpiration() {
		return Optional.ofNullable(this.expiration);
	}

}
