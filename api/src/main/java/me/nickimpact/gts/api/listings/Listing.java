package me.nickimpact.gts.api.listings;

import com.nickimpact.impactor.api.building.Builder;
import lombok.Getter;
import lombok.Setter;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.prices.Price;
import me.nickimpact.gts.api.plugin.IGTSBacking;
import me.nickimpact.gts.api.plugin.IGTSPlugin;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public abstract class Listing<E extends Entry, P, I> {

	/** The Unique ID of the element */
	@Getter
	private UUID uuid;

	/** The uuid of the element owner */
	@Getter private final UUID ownerUUID;

	@Getter @Setter
	private E entry;

	protected Price price;

	/** When the lot will expire, if the above is true */
	@Getter private LocalDateTime expiration;

	public Listing(UUID id, UUID owner, E entry, Price price, LocalDateTime expiration) {
		this.uuid = id;
		this.price = price;
		this.ownerUUID = owner;
		this.expiration = expiration;
		this.entry = entry;
	}

	public boolean publish(IGTSBacking plugin, UUID uuid) {
		return plugin.getAPIService().getListingManager().addToMarket(uuid,this);
	}

	/**
	 * Checks whether or not a listing has exceeded its allocated listing time.
	 *
	 * @return Whether or not a listing has expired
	 */
	public boolean hasExpired() {
		return getExpiration().isBefore(LocalDateTime.now());
	}

	/**
	 * Returns the name of a lot element. Common uses of this method might be its use in messages displayed
	 * to users.
	 *
	 * @return The name of a lot element.
	 */
	public String getName() {
		return entry.getName();
	}

	/**
	 * Retrieves the ItemStack display for the {@link Entry} attached to this listing
	 *
	 * @param player The player to create the item stack for
	 * @return An itemstack representation of the entry
	 */
	public abstract I getDisplay(P player);

	public Price getPrice() {
		return this.price;
	}

	public static ListingBuilder builder(IGTSPlugin plugin) {
		return plugin.getAPIService().getBuilderRegistry().createFor(ListingBuilder.class);
	}

	public interface ListingBuilder extends Builder<Listing> {

		ListingBuilder id(UUID id);

		ListingBuilder owner(UUID owner);

		<E extends Entry> ListingBuilder entry(E entry);

		ListingBuilder price(double price);

		ListingBuilder expiration(LocalDateTime expiration);
	}
}
