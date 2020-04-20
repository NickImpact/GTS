package me.nickimpact.gts.sponge.listings;

import com.google.common.base.Preconditions;
import me.nickimpact.gts.api.listings.direct.QuickPurchase;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.prices.Price;
import me.nickimpact.gts.sponge.listings.makeup.SpongeEntry;
import me.nickimpact.gts.sponge.pricing.SpongePrice;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class SpongeQuickPurchase extends SpongeListing implements QuickPurchase {

	private SpongePrice price;

	private SpongeQuickPurchase(SpongeListingBuilder builder) {
		super(builder.id, builder.lister, builder.entry, builder.expiration);
		this.price = (SpongePrice) builder.price;
	}

	@Override
	public Price getPrice() {
		return this.price;
	}

	public static class SpongeListingBuilder implements ListingBuilder<SpongeQuickPurchase, SpongeListingBuilder, SpongeEntry> {

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
		public SpongeListingBuilder entry(SpongeEntry entry) {
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
		public SpongeListingBuilder from(SpongeQuickPurchase input) {
			return this.id(input.getID())
					.lister(input.getLister())
					.entry(input.getEntry())
					.price(input.getPrice())
					.expiration(input.getExpiration().orElse(null));
		}

		@Override
		public SpongeQuickPurchase build() {
			return new SpongeQuickPurchase(this);
		}
	}
}
