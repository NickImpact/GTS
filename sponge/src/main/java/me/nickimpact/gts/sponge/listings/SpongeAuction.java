package me.nickimpact.gts.sponge.listings;

import com.google.common.base.Preconditions;
import me.nickimpact.gts.api.listings.auctions.Auction;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.makeup.Display;
import me.nickimpact.gts.api.util.Tuple;
import me.nickimpact.gts.sponge.listings.makeup.SpongeEntry;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.SortedMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class SpongeAuction implements Auction {

	private UUID id;
	private UUID lister;
	private SpongeEntry entry;
	private LocalDateTime expiration;
	private double price;
	private float increment;

	/**
	 * This should feature the top bid at the first key, with lowest being the last key.
	 */
	private ConcurrentNavigableMap<UUID, Double> bids = new ConcurrentSkipListMap<>(Collections.reverseOrder());

	private SpongeAuction(SpongeAuctionBuilder builder) {
		this.id = builder.id;
		this.lister = builder.lister;
		this.entry = builder.entry;
		this.expiration = builder.expiration;
		this.price = builder.start;
		this.increment = builder.increment;
	}

	@Override
	public Tuple<UUID, Double> getHighBid() {
		return new Tuple<>(this.bids.firstKey(), this.bids.get(this.bids.firstKey()));
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
		if(amount >= this.getHighBid().getSecond() + this.getIncrement()) {
			this.getBids().remove(this.getBids().lastKey());
			this.getBids().put(user, amount);
			this.price = amount;
			return true;
		}
		return false;
	}

	@Override
	public UUID getID() {
		return this.id;
	}

	@Override
	public UUID getLister() {
		return this.lister;
	}

	@Override
	public Entry getEntry() {
		return this.entry;
	}

	@Override
	public Display getDisplay() {
		return this.getEntry().getDisplay();
	}

	@Override
	public LocalDateTime getExpiration() {
		return this.expiration;
	}

	@Override
	public Optional<Double> getCurrentBid(UUID uuid) {
		return Optional.empty();
	}

	@Override
	public SortedMap<UUID, Double> getBids() {
		return this.bids;
	}

	public static class SpongeAuctionBuilder implements AuctionBuilder {

		private UUID id;
		private UUID lister;
		private SpongeEntry entry;
		private LocalDateTime expiration;
		private double start;
		private float increment;

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
				input.getBids().put(tmp, 0D);
				this.increment = input.getIncrement();
				input.getBids().remove(tmp);
			}

			return this;
		}

		@Override
		public Auction build() {
			return new SpongeAuction(this);
		}

	}
}
