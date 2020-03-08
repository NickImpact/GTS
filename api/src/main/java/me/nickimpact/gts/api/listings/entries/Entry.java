package me.nickimpact.gts.api.listings.entries;

import me.nickimpact.gts.api.listings.interactors.Depositor;
import me.nickimpact.gts.api.listings.interactors.Recipient;
import me.nickimpact.gts.api.listings.makeup.Display;

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
 * @param <Store> The object type that'll be used to serialize the data into the GTS Storage Provider
 * @param <Out> The actual object that is meant to be represented by this entry
 */
public interface Entry<Store, Out> {

	/**
	 * Represents the internal data-store that will be used to create the output element. This is what will
	 * be saved to the storage system when an entry is written out, whereas the output element should realistically
	 * be transient. This helps to ensure that objects that can't be serialized properly due to recursive structures
	 * can be serialized in a safe manner.
	 *
	 * @return The data-store that will be used to represent this entry from a storage provider perspective.
	 */
	Store getElement();

	/**
	 * This represents the output element of this entry. As a entry will typically be transient, if not serializable
	 * by default, this call will attempt to fetch from the internal cache of this entry. If that element is not present,
	 * then this call will create that instance for its reference.
	 *
	 * @return The output element built from the internal data-store object.
	 */
	Out getOrCreateEntry();

	/**
	 * Specifies the name of this entry. This will often just be the name of the entry itself, if it has a name.
	 *
	 * @return The name of this entry
	 */
	String getName();

	/**
	 * Represents how this entry should be displayed to a user querying this listing.
	 *
	 * @return The overall display of the listing.
	 */
	Display getDisplay();

	/**
	 * States whether or not this entry supports offline receiving. Some object types may not be able to be retrieved
	 * when the user is offline, and therefore, this call should specify if they can or can't be received by an offline
	 * user.
	 *
	 * @return True if this listing can be given to an offline user, false otherwise
	 */
	boolean supportsOffline();

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
	boolean give(Recipient receiver);

	/**
	 * Attempts to take the listing from the depositor. If the user doesn't actually have what they are trying to sell,
	 * this call will return false to detail that the action has failed. Otherwise, true will state that this object has
	 * been successfully taken from the depositor, and can be placed into the GTS market.
	 *
	 * @param depositor The depositor of the entry
	 * @return True if the listing was taken from the user, false otherwise
	 */
	boolean take(Depositor depositor);

}
