package me.nickimpact.gts.spigot;

import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.prices.Price;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDateTime;
import java.util.UUID;

public class SpigotListing extends Listing<SpigotEntry, Player, ItemStack> {

	public SpigotListing(UUID id, UUID owner, SpigotEntry entry, Price price, LocalDateTime expiration) {
		super(id, owner, entry, price, expiration);
	}

	private SpigotListing(SpigotListingBuilder builder) {
		super(builder.id, builder.owner, builder.entry, builder.price, builder.expiration);
	}

	@Override
	public ItemStack getDisplay(Player player, boolean confirm) {
		return (ItemStack) this.getEntry().baseItemStack(player, this);
	}

	public static SpigotListingBuilder builder() {
		return new SpigotListingBuilder();
	}

	public static class SpigotListingBuilder implements ListingBuilder {

		private UUID id;
		private UUID owner;
		private SpigotEntry entry;
		private Price price;
		private LocalDateTime expiration;

		@Override
		public SpigotListingBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		@Override
		public SpigotListingBuilder owner(UUID owner) {
			this.owner = owner;
			return this;
		}

		@Override
		public <E extends Entry> SpigotListingBuilder entry(E entry) {
			this.entry = (SpigotEntry) entry;
			return this;
		}

		@Override
		public SpigotListingBuilder price(double price) {
			this.price = new MoneyPrice(price);
			return this;
		}

		@Override
		public SpigotListingBuilder expiration(LocalDateTime expiration) {
			this.expiration = expiration;
			return this;
		}

		@Override
		public SpigotListing build() {
			return new SpigotListing(this);
		}
	}
}
