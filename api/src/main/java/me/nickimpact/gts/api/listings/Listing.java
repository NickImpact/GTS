package me.nickimpact.gts.api.listings;

import com.nickimpact.impactor.api.Impactor;
import com.nickimpact.impactor.api.utilities.Builder;
import me.nickimpact.gts.api.listings.prices.Price;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.makeup.Display;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * A listing represents the overall information of something listed onto the GTS.
 */
public interface Listing {

	/** The UUID to use when the server itself creates a listing */
	UUID SERVER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

	Predicate<Listing> NON_EXPIRED_LISTINGS = listing -> !listing.hasExpired();

	/**
	 * Represents the ID of this listing. This is independent of the lister's UUID, which serves as a reference
	 * to the player or system that listed the listing.
	 *
	 * @return The unique ID of this listing
	 */
	UUID getID();

	/**
	 * Represents the user listing this Listing. If this is a player, the UUID will be that of the player.
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
	Entry<?, ?> getEntry();

	/**
	 * Represents the display of the listing. This is essentially how the listing will be displayed to the user
	 * when queried in-game.
	 *
	 * @return The display parameters of this listing
	 */
	default Display<?> getDisplay(UUID viewer) {
		return this.getEntry().getDisplay(viewer, this);
	}

	/**
	 * Details the exact time at which a listing was published to the GTS market. This is namely helpful
	 * for tracking purposes.
	 *
	 * @return The time this listing was published
	 */
	LocalDateTime getPublishTime();

	/**
	 * Represents the time where this listing will expire.
	 *
	 * @return The time this listing will expire
	 */
	LocalDateTime getExpiration();

	/**
	 * Attempts to verify whether or not a listing has expired. If a listing has no expiration, this call will
	 * always be false. If an expiration does exist, this call will verify its validity based on the marked
	 * expiration time with the current time at the time of the call.
	 *
	 * @return True if the listing has an expiration and said expiration is before the current system time,
	 * or false if the expiration is still after the current system time, or this listing has no expiration.
	 */
	default boolean hasExpired() {
		return LocalDateTime.now().isAfter(this.getExpiration());
	}

	@SuppressWarnings("unchecked")
	static ListingBuilder<?, ?> builder() {
		return Impactor.getInstance().getRegistry().createBuilder(ListingBuilder.class);
	}

	interface ListingBuilder<L extends Listing, B extends ListingBuilder<?, ?>> extends Builder<L, B> {

		/**
		 * Represents the ID of a listing. If not specified, this will be auto-generated at the time of constructing.
		 *
		 * @param id The ID to use for the listing
		 * @return The builder modified with this value
		 */
		B id(UUID id);

		/**
		 * Specifies the seller of this listing. The seller is simply a mapping to a
		 *
		 * @param lister The individual creating this listing
		 * @return The builder modified with this value
		 */
		B lister(UUID lister);

		B entry(Entry<?, ?> entry);

		B price(Price<?, ?> price);

		B expiration(LocalDateTime expiration);

	}
}
