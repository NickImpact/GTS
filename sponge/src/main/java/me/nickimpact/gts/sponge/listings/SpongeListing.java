package me.nickimpact.gts.sponge.listings;

import com.google.common.base.Preconditions;
import me.nickimpact.gts.api.listings.direct.QuickPurchase;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.prices.Price;
import me.nickimpact.gts.sponge.pricing.SpongePrice;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class SpongeListing implements QuickPurchase {

	private UUID id;
	private UUID lister;
	private Entry entry;
	private LocalDateTime expiration;
	private SpongePrice price;

	private SpongeListing(SpongeListingBuilder builder) {
		this.id = builder.id;
		this.lister = builder.lister;
		this.entry = builder.entry;
		this.expiration = builder.expiration;
		this.price = (SpongePrice) builder.price;
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
	public Optional<LocalDateTime> getExpiration() {
		return Optional.ofNullable(expiration);
	}

	@Override
	public Price getPrice() {
		return this.price;
	}

	public static class SpongeListingBuilder implements ListingBuilder<SpongeListing, SpongeListingBuilder> {

		private UUID id;
		private UUID lister;
		private Entry entry;
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
			this.entry = entry;
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
		public SpongeListingBuilder from(SpongeListing input) {
			return this.id(input.getID())
					.lister(input.getLister())
					.entry(input.getEntry())
					.price(input.getPrice())
					.expiration(input.getExpiration().orElse(null));
		}

		@Override
		public SpongeListing build() {
			return new SpongeListing(this);
		}
	}
}
