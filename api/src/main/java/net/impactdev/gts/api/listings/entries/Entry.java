package net.impactdev.gts.api.listings.entries;

import io.leangen.geantyref.TypeToken;
import net.impactdev.gts.api.data.Storable;
import net.impactdev.gts.api.deliveries.Delivery;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.makeup.Display;
import net.kyori.adventure.text.TextComponent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * An Entry is the object that contains the essential information of what is being listed on the GTS. More precisely,
 * it is the instance responsible for providing the listing information of how to process deposits and purchases/
 * withdrawals of the instance being listed on the market.
 *
 * <p>Based on the nature of some objects and how they are defined, the actual items being purchased may not
 * be easily serializable. GTS loves to store objects as JSON representations when it can, and some objects by
 * nature destroy this model. As such, if a listing option hits this barrier, the instance should be broken
 * into two representations, the Store and Out options.</p>
 *
 * @param <T> The actual object that is meant to be represented by this entry
 */
public interface Entry<T, I> extends Storable {

	default TypeToken<T> type() {
		return new TypeToken<T>() {};
	}

	/**
	 * This represents the output element of this entry. As a entry will typically be transient, if not serializable
	 * by default, this call will attempt to fetch from the internal cache of this entry. If that element is not present,
	 * then this call will create that instance for its reference.
	 *
	 * @return The output element built from the internal data-store object.
	 */
	T getOrCreateElement();

	/**
	 * Specifies the name of this entry. This will often just be the name of the entry itself, if it has a name.
	 *
	 * @return The name of this entry
	 */
	TextComponent getName();

	/**
	 * Represents a short description we will output to chat for placeholder usage.
	 *
	 * @return The description we want to use to detail a listing
	 */
	TextComponent getDescription();

	/**
	 * Represents how this entry should be displayed to a user querying this entry.
	 *
	 * @param viewer The ID of the user viewing the display
	 * @return The overall display of the listing.
	 */
	Display<I> getDisplay(UUID viewer);
	/**
	 * Attempts to give the listing to the recipient. If the recipient doesn't currently meet the requirements to
	 * receive this listing, such as having a full inventory, this listing will be cached such that they can receive it
	 * at a later time.
	 *
	 * <p>This call shouldn't attempt to do the caching, the system will handle that itself when this call returns
	 * false. Therefore, it is also essential that an implementation returns the proper result as to avoid duplication.</p>
	 *
	 * @param receiver The recipient of the entry
	 * @return True if the listing was received, false otherwise.
	 */
	boolean give(UUID receiver);

	/**
	 * Attempts to take the listing from the depositor. If the user doesn't actually have what they are trying to sell,
	 * this call will return false to detail that the action has failed. Otherwise, true will state that this object has
	 * been successfully taken from the depositor, and can be placed into the GTS market.
	 *
	 * @param depositor The depositor of the entry
	 * @return True if the listing was taken from the user, false otherwise
	 */
	boolean take(UUID depositor);

	/**
	 * Represents a URL mapping to an image that can represent the listing
	 *
	 * @return A URL representing the listing
	 */
	Optional<String> getThumbnailURL();

	/**
	 * Represents a set of details mapping to the listing
	 *
	 * @return A set of details describing the listing
	 */
	List<String> getDetails();

}
