package me.nickimpact.gts.sponge.listings;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nickimpact.impactor.api.json.factory.JArray;
import com.nickimpact.impactor.api.json.factory.JObject;
import me.nickimpact.gts.api.GTSService;
import me.nickimpact.gts.api.listings.auctions.Auction;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.entries.EntryManager;
import me.nickimpact.gts.api.util.groupings.Tuple;
import me.nickimpact.gts.sponge.listings.makeup.SpongeEntry;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

public class SpongeAuction extends SpongeListing implements Auction {

	private double price;
	private float increment;

	/**
	 * This should feature the top bid at the first key, with lowest being the last key.
	 */
	private final NavigableSet<Tuple<UUID, Double>> bids = new ConcurrentSkipListSet<>(Collections.reverseOrder(Comparator.comparing(Tuple::getSecond)));

	private SpongeAuction(SpongeAuctionBuilder builder) {
		super(builder.id, builder.lister, builder.entry, builder.expiration);
		this.price = builder.start;
		this.increment = builder.increment;
		if(builder.bids != null) {
			builder.bids.forEach((uuid, bid) -> {
				this.bids.add(new Tuple<>(uuid, bid));
			});
		}
	}

	@Override
	public Tuple<UUID, Double> getHighBid() {
		return new Tuple<>(this.bids.first().getFirst(), this.bids.first().getSecond());
	}

	@Override
	public double getStartingPrice() {
		return this.price;
	}

	@Override
	public double getCurrentPrice() {
		return this.price + Math.ceil(this.price * this.getIncrement());
	}

	@Override
	public float getIncrement() {
		return this.increment * (this.getBids().size() == 0 ? 0 : 1);
	}

	@Override
	public boolean bid(UUID user, double amount) {
		if(this.bids.size() == 0 || (amount >= this.getHighBid().getSecond() + this.getIncrement())) {
			this.getBids().add(new Tuple<>(user, amount));
			this.price = amount;
			return true;
		}
		return false;
	}

	@Override
	public Optional<Double> getCurrentBid(UUID uuid) {
		return this.bids.stream()
				.filter(entry -> entry.getFirst().equals(uuid))
				.map(Tuple::getSecond)
				.max(Comparator.naturalOrder());
	}

	@Override
	public NavigableSet<Tuple<UUID, Double>> getBids() {
		return this.bids;
	}

	@Override
	public JObject serialize() {
		JObject json = super.serialize();

		JObject bids = new JObject();
		Multimap<UUID, Double> data = ArrayListMultimap.create();
		synchronized (this.bids) {
			for (Tuple<UUID, Double> entry : this.bids) {
				data.put(entry.getFirst(), entry.getSecond());
			}
		}

		for(UUID id : data.keys()) {
			JArray array = new JArray();
			for(double bid : data.get(id).stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList())) {
				array.add(bid);
			}
			bids.add(id.toString(), array);
		}

		JObject pricing = new JObject()
				.add("start", this.getStartingPrice())
				.add("increment", this.getIncrement());

		json.add("auction", new JObject()
				.add("bids", bids)
				.add("pricing", pricing)
		);
		json.add("type", "auction");

		return json;
	}

	public static SpongeAuction deserialize(JsonObject object) {
		SpongeAuctionBuilder builder = (SpongeAuctionBuilder) Auction.builder()
				.id(UUID.fromString(object.get("id").getAsString()))
				.lister(UUID.fromString(object.get("lister").getAsString()))
				.expiration(LocalDateTime.parse(object.getAsJsonObject("timings").get("expiration").getAsString()))
				.start(object.getAsJsonObject("auction").getAsJsonObject("pricing").get("start").getAsDouble())
				.increment(object.getAsJsonObject("auction").getAsJsonObject("pricing").get("increment").getAsFloat());

		JsonObject element = object.getAsJsonObject("entry");
		EntryManager<?, ?> entryManager = GTSService.getInstance().getGTSComponentManager()
				.getEntryDeserializer(element.get("key").getAsString())
				.orElseThrow(() -> new RuntimeException("JSON Data for entry is missing mapping key"));
		builder.entry((SpongeEntry<?>) entryManager.getDeserializer().deserialize(element.getAsJsonObject("content")));

		JsonObject bids = object.getAsJsonObject("auction").getAsJsonObject("bids");
		Multimap<UUID, Double> mapping = ArrayListMultimap.create();
		for(Map.Entry<String, JsonElement> entry : bids.entrySet()) {
			if(entry.getValue().isJsonArray()) {
				entry.getValue().getAsJsonArray().forEach(e -> {
					mapping.put(UUID.fromString(entry.getKey()), e.getAsDouble());
				});
			}
		}

		builder.bids(mapping);
		return builder.build();
	}

	public static class SpongeAuctionBuilder implements AuctionBuilder {

		private UUID id;
		private UUID lister;
		private SpongeEntry<?> entry;
		private LocalDateTime expiration;
		private double start;
		private float increment;
		private Multimap<UUID, Double> bids;

		@Override
		public AuctionBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		@Override
		public AuctionBuilder lister(UUID lister) {
			this.lister = lister;
			return this;
		}

		@Override
		public AuctionBuilder entry(Entry entry) {
			Preconditions.checkArgument(entry instanceof SpongeEntry, "Mixing of invalid types!");
			this.entry = (SpongeEntry) entry;
			return this;
		}

		@Override
		public AuctionBuilder expiration(LocalDateTime expiration) {
			this.expiration = expiration;
			return this;
		}

		@Override
		public AuctionBuilder start(double amount) {
			this.start = amount;
			return this;
		}

		@Override
		public AuctionBuilder increment(float rate) {
			this.increment = rate;
			return this;
		}

		@Override
		public AuctionBuilder bids(Multimap<UUID, Double> bids) {
			this.bids = bids;
			return this;
		}

		@Override
		public AuctionBuilder from(Auction input) {
			Preconditions.checkArgument(input instanceof SpongeAuction, "Mixing of invalid types!");
			this.id = input.getID();
			this.lister = input.getLister();
			this.entry = (SpongeEntry) input.getEntry();
			this.expiration = input.getExpiration();
			this.start = input.getCurrentPrice() - input.getIncrement();

			// Hack around the static 0 for an auction with no bids currently set
			if(input.getBids().size() == 0) {
				UUID tmp = UUID.randomUUID();
				//input.getBids().put(tmp, 0D);
				this.increment = input.getIncrement();
				input.getBids().remove(tmp);
			}

			return this;
		}

		@Override
		public SpongeAuction build() {
			return new SpongeAuction(this);
		}

	}
}
