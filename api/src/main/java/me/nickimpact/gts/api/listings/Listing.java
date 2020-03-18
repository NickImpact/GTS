package me.nickimpact.gts.api.listings;

import me.nickimpact.gts.api.GtsService;
import me.nickimpact.gts.api.listings.prices.Price;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.makeup.Display;
import me.nickimpact.gts.api.registry.GTSRegistry;
import me.nickimpact.gts.api.util.Builder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * A listing represents the overall information of something listed onto the GTS.
 */
public interface Listing {

	/** The UUID to use when the server itself creates a listing */
	UUID SERVER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

	/**
	 * Represents the ID of this listing. This is independent of the lister's UUID, which serves as a reference
	 * to the player or system that listed the listing.
	 *
	 * @return The unique ID of this listing
	 */
	UUID getID();

	/**
	 * Represents the UUID of the user listing this Listing. If this is a player, the UUID will be that of the player.
	 * If the server creates the listing, then this UUID will match the generic zeroed out ID.
	 *
	 * @return The ID of the lister who created this listing
	 */
	UUID getLister();

	/**
	 * Represents the actual component of the listing that will be contained by this listing. This is what a user will
	 * be purchasing should they purchase the listing.
	 *
	 * @return The entry making up this listing.
	 */
	Entry getEntry();

	/**
	 * Represents the display of the listing. This is essentially how the listing will be displayed to the user
	 * when queried in-game.
	 *
	 * @return The display parameters of this listing
	 */
	default Display getDisplay() {
		return this.getEntry().getDisplay();
	}

	/**
	 * Represents the time where this listing will expire. In general, each listing will have this option specified.
	 * However, a listing that has been made permanent can avoid this check, and simply stay in the system until
	 * it is either purchased or removed.
	 *
	 * @return An optional value containing the exact point in time in which this listing will expire, or empty to
	 * represent that this listing will never expire.
	 */
	Optional<LocalDateTime> getExpiration();

	/**
	 * Represents the price of this listing. This will be what a purchasing player must pay in order to buy this
	 * listing off the GTS.
	 *
	 * @return The price of the listing
	 */
	Price getPrice();

	static ListingBuilder builder() {
		return GtsService.getInstance().getRegistry().createBuilder(ListingBuilder.class);
	}

	interface ListingBuilder extends Builder<Listing, ListingBuilder> {

		ListingBuilder id(UUID id);

		ListingBuilder lister(UUID lister);

		ListingBuilder entry(Entry entry);

		ListingBuilder price(Price price);

		ListingBuilder expiration(LocalDateTime expiration);

	}
}
