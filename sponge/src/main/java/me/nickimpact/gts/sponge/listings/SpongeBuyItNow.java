package me.nickimpact.gts.sponge.listings;

import com.google.common.base.Preconditions;
import me.nickimpact.gts.api.listings.buyitnow.BuyItNow;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.prices.Price;
import me.nickimpact.gts.sponge.listings.makeup.SpongeEntry;
import me.nickimpact.gts.sponge.pricing.SpongePrice;

import java.time.LocalDateTime;
import java.util.UUID;

public class SpongeBuyItNow extends SpongeListing implements BuyItNow {

	private SpongePrice price;

	private SpongeBuyItNow(SpongeListingBuilder builder) {
		super(builder.id, builder.lister, builder.entry, builder.expiration);
		this.price = (SpongePrice) builder.price;
	}

	@Override
	public Price<?, ?> getPrice() {
		return this.price;
	}

	@Override
	public LocalDateTime getPublishTime() {
		return null;
	}

	public static class SpongeListingBuilder implements ListingBuilder<SpongeBuyItNow, SpongeListingBuilder> {

		private UUID id;
		private UUID lister;
		private SpongeEntry entry;
		private Price price;
		private LocalDateTime expiration;

		@Override
		public SpongeListingBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		@Override
		public SpongeListingBuilder lister(UUID lister) {
			this.lister = lister;
			return this;
		}

		@Override
		public SpongeListingBuilder entry(Entry entry) {
			Preconditions.checkArgument(entry instanceof SpongeEntry, "Mixing of incompatible platform types");
			this.entry = (SpongeEntry) entry;
			return this;
		}

		@Override
		public SpongeListingBuilder price(Price price) {
			Preconditions.checkArgument(price instanceof SpongePrice, "Mixing of incompatible platform types");
			this.price = price;
			return this;
		}

		@Override
		public SpongeListingBuilder expiration(LocalDateTime expiration) {
			this.expiration = expiration;
			return this;
		}

		@Override
		public SpongeListingBuilder from(SpongeBuyItNow input) {
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
