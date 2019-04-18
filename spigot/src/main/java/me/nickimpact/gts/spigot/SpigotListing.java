package me.nickimpact.gts.spigot;

import me.nickimpact.gts.api.GtsService;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.prices.Price;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Date;
import java.util.UUID;

public class SpigotListing extends Listing<SpigotEntry, Player, ItemStack> {

	public SpigotListing(UUID id, UUID owner, SpigotEntry entry, Price price, Date expiration) {
		super(id, owner, entry, price, expiration);
	}

	public SpigotListing(SpigotListingBuilder builder) {
		super(builder.id, builder.owner, builder.entry, builder.price, builder.expiration);
	}

	@Override
	public ItemStack getDisplay(Player player, boolean confirm) {
		return confirm ? (ItemStack) this.getEntry().confirmItemStack(player, this) : (ItemStack) this.getEntry().baseItemStack(player, this);
	}

	public class SpigotListingBuilder implements ListingBuilder {

		private UUID id;
		private UUID owner;
		private SpigotEntry entry;
		private Price price;
		private Date expiration;

		@Override
		public ListingBuilder id(UUID id) {
			return null;
		}

		@Override
		public ListingBuilder owner(UUID owner) {
			return null;
		}

		@Override
		public <E extends Entry> ListingBuilder entry(E entry) {
			this.entry = (SpigotEntry) entry;
			return this;
		}

		@Override
		public ListingBuilder price(double price) {
			return null;
		}

		@Override
		public ListingBuilder expiration(Date expiration) {
			return null;
		}

		@Override
		public Listing build() {
			return null;
		}
	}
}
