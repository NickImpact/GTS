package me.nickimpact.gts.sponge.listings;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.nickimpact.impactor.api.json.factory.JObject;
import lombok.RequiredArgsConstructor;
import me.nickimpact.gts.api.GTSService;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.entries.EntryKey;
import me.nickimpact.gts.api.listings.entries.EntryManager;
import me.nickimpact.gts.sponge.listings.makeup.SpongeEntry;

import java.time.LocalDateTime;
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

	@Override
	public int getVersion() {
		return 1;
	}

	@Override
	public JObject serialize() {
		Preconditions.checkArgument(this.getEntry().getClass().isAnnotationPresent(EntryKey.class), "An Entry type must be annotated with EntryKey");

		JObject entry = new JObject()
				.add("key", this.getEntry().getClass().getAnnotation(EntryKey.class).value())
				.add("content", this.getEntry().serialize());

		JObject data = new JObject()
				.add("entry", entry)
				.add("price", this.getPrice().serialize());

		JObject timings = new JObject()
				//.add("published", this.getPublishTime().toString())
				.add("expires", this.getExpiration().toString());

		return new JObject()
				.add("id", this.getID().toString())
				.add("lister", this.getLister().toString())
				.add("data", data)
				.add("timings", timings)
				.add("version", this.getVersion())
				;
	}

	@Override
	public Listing deserialize(JsonObject json) {
		JsonObject element = json.getAsJsonObject("data").getAsJsonObject("entry");
		EntryManager<?, ?> entryManager = GTSService.getInstance().getEntryManagerRegistry()
				.get(element.get("key")
				.getAsString())
				.orElseThrow(() -> new RuntimeException("JSON Data for entry is missing mapping key"));

		Entry<?, ?> entry = (Entry<?, ?>) entryManager.getDeserializer().deserialize(element.getAsJsonObject("content"));

		return null;
	}
}
