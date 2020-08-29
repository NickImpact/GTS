package me.nickimpact.gts.sponge.listings;

import com.google.common.base.Preconditions;
import com.nickimpact.impactor.api.json.factory.JObject;
import lombok.RequiredArgsConstructor;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.data.registry.GTSKeyMarker;
import me.nickimpact.gts.sponge.listings.makeup.SpongeEntry;

import java.time.LocalDateTime;
import java.util.UUID;


@RequiredArgsConstructor
public abstract class SpongeListing implements Listing {

	private final UUID id;
	private final UUID lister;
	private final SpongeEntry<?> entry;
	private final LocalDateTime published = LocalDateTime.now();
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
	public LocalDateTime getPublishTime() {
		return this.published;
	}

	@Override
	public LocalDateTime getExpiration() {
		return this.expiration;
	}

	@Override
	public int getVersion() {
		return 1;
	}

	@Override
	public JObject serialize() {
		Preconditions.checkArgument(this.getEntry().getClass().isAnnotationPresent(GTSKeyMarker.class), "An Entry type must be annotated with GTSKeyMarker");

		JObject timings = new JObject()
				.add("published", this.getPublishTime().toString())
				.add("expiration", this.getExpiration().toString());

		JObject entry = new JObject()
				.add("key", this.getEntry().getClass().getAnnotation(GTSKeyMarker.class).value())
				.add("content", this.getEntry().serialize());

		return new JObject()
				.add("id", this.getID().toString())
				.add("lister", this.getLister().toString())
				.add("version", this.getVersion())
				.add("timings", timings)
				.add("entry", entry)
				;
	}

}
