package net.impactdev.gts.sponge.listings;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import net.impactdev.gts.sponge.listings.makeup.SpongeEntry;
import net.impactdev.gts.sponge.pricing.SpongePrice;
import net.impactdev.impactor.api.json.factory.JObject;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.data.ResourceManager;
import net.impactdev.gts.api.data.Storable;
import net.impactdev.gts.api.data.registry.GTSKeyMarker;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.api.listings.entries.Entry;
import net.impactdev.gts.api.listings.entries.EntryManager;
import net.impactdev.gts.api.listings.prices.Price;

import java.time.LocalDateTime;
import java.util.UUID;

public class SpongeBuyItNow extends SpongeListing implements BuyItNow {

	private final SpongePrice<?, ?> price;

	private SpongeBuyItNow(SpongeBuyItNowBuilder builder) {
		super(builder.id, builder.lister, builder.entry, builder.expiration);
		this.price = builder.price;
	}

	@Override
	public Price<?, ?, ?> getPrice() {
		return this.price;
	}

	@Override
	public JObject serialize() {
		Preconditions.checkArgument(this.getPrice().getClass().isAnnotationPresent(GTSKeyMarker.class), "A Price type must be annotated with GTSKeyMarker");

		JObject json = super.serialize();
		JObject price = new JObject()
				.add("key", this.getPrice().getClass().getAnnotation(GTSKeyMarker.class).value())
				.add("content", this.getPrice().serialize());
		json.add("price", price);
		json.add("type", "bin");

		return json;
	}

	public static SpongeBuyItNow deserialize(JsonObject json) {
		SpongeBuyItNowBuilder builder = (SpongeBuyItNowBuilder) BuyItNow.builder()
				.id(UUID.fromString(json.get("id").getAsString()))
				.lister(UUID.fromString(json.get("lister").getAsString()))
				.expiration(LocalDateTime.parse(json.getAsJsonObject("timings").get("expiration").getAsString()));

		JsonObject element = json.getAsJsonObject("entry");
		EntryManager<?, ?> entryManager = GTSService.getInstance().getGTSComponentManager()
				.getEntryManager(element.get("key").getAsString())
				.orElseThrow(() -> new RuntimeException("JSON Data for entry is missing mapping key"));
		builder.entry((SpongeEntry<?>) entryManager.getDeserializer().deserialize(element.getAsJsonObject("content")));

		JsonObject price = json.getAsJsonObject("price");
		Storable.Deserializer<Price<?, ?, ?>> deserializer = GTSService.getInstance().getGTSComponentManager()
				.getPriceManager(price.get("key").getAsString())
				.map(ResourceManager::getDeserializer)
				.orElseThrow(() -> new RuntimeException("JSON Data for price is missing mapping key"));
		builder.price(deserializer.deserialize(price.getAsJsonObject("content")));
		return builder.build();
	}

	public static class SpongeBuyItNowBuilder implements BuyItNowBuilder {

		private UUID id = UUID.randomUUID();
		private UUID lister;
		private SpongeEntry<?> entry;
		private SpongePrice<?, ?> price;
		private LocalDateTime expiration;

		@Override
		public BuyItNowBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		@Override
		public BuyItNowBuilder lister(UUID lister) {
			this.lister = lister;
			return this;
		}

		@Override
		public BuyItNowBuilder entry(Entry<?, ?> entry) {
			Preconditions.checkArgument(entry instanceof SpongeEntry, "Mixing of incompatible platform types");
			this.entry = (SpongeEntry<?>) entry;
			return this;
		}

		@Override
		public BuyItNowBuilder price(Price<?, ?, ?> price) {
			Preconditions.checkArgument(price instanceof SpongePrice, "Mixing of incompatible platform types");
			this.price = (SpongePrice<?, ?>) price;
			return this;
		}

		@Override
		public BuyItNowBuilder expiration(LocalDateTime expiration) {
			this.expiration = expiration;
			return this;
		}

		@Override
		public BuyItNowBuilder from(BuyItNow input) {
			return this.id(input.getID())
					.lister(input.getLister())
					.entry(input.getEntry())
					.price(input.getPrice())
					.expiration(input.getExpiration());
		}

		@Override
		public SpongeBuyItNow build() {
			return new SpongeBuyItNow(this);
		}
	}
}
