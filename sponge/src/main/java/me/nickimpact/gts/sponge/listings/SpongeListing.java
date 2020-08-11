package me.nickimpact.gts.sponge.listings;

import lombok.RequiredArgsConstructor;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.sponge.listings.makeup.SpongeEntry;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;


@RequiredArgsConstructor
public abstract class SpongeListing implements Listing {

	private final UUID id;
	private final UUID lister;
	private final SpongeEntry<?> entry;
	private final LocalDateTime expiration;

	@Override
	public UUID getID() {
		return this.id;
	}

	@Override
	public UUID getLister() {
		return this.lister;
	}

	@Override
	public SpongeEntry<?> getEntry() {
		return this.entry;
	}

	@Override
	public LocalDateTime getExpiration() {
		return this.expiration;
	}
}
