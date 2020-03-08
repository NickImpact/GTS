package me.nickimpact.gts.sponge;

import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.prices.Price;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;

import java.time.LocalDateTime;
import java.util.UUID;

public class SpongeListing extends Listing<SpongeEntry, Player, ItemStack> {

	public SpongeListing(UUID id, UUID owner, SpongeEntry entry, Price price, LocalDateTime expiration) {
		super(id, owner, entry, price, expiration);
	}

	private SpongeListing(SpongeListingBuilder builder) {
		super(builder.id, builder.owner, builder.entry, builder.price, builder.expiration);
	}

	@Override
	public ItemStack getDisplay(Player player) {
		return (ItemStack) this.getEntry().baseItemStack(player, this);
	}

	public static SpongeListingBuilder builder() {
		return new SpongeListingBuilder();
	}

	public static class SpongeListingBuilder implements ListingBuilder {
		private UUID id;
		private UUID owner;
		private SpongeEntry entry;
		private Price price;
		private LocalDateTime expiration;

		@Override
		public SpongeListingBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		@Override
		public SpongeListingBuilder owner(UUID owner) {
			this.owner = owner;
			return this;
		}

		@Override
		public <E extends Entry> SpongeListingBuilder entry(E entry) {
			this.entry = (SpongeEntry) entry;
			return this;
		}

		@Override
		public SpongeListingBuilder price(double price) {
			this.price = new MoneyPrice(price);
			return this;
		}

		@Override
		public SpongeListingBuilder expiration(LocalDateTime expiration) {
			this.expiration = expiration;
			return this;
		}

		@Override
		public SpongeListing build() {
			return new SpongeListing(this);
		}
	}
}
