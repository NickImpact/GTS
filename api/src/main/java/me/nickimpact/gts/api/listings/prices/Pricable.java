package me.nickimpact.gts.api.listings.prices;

/**
 * Represents the instance that makes up a price. A pricable should make sure that whatever it is charging can
 * be serialized, so objects that employ recursive structures should have a wrapper which can create them be their
 * delegate for this option
 *
 * @param <T> The serializable instance used to handle this price.
 */
public interface Pricable<T> {

	/**
	 * Returns the instance used as the price model.
	 *
	 * @return The price model
	 */
	T getPricable();

	/**
	 * States whether or not this price supports offline receiving. In other words, if an object type can't normally
	 * be given to a user, this type will then avoid any concept of being given to an offline user, and will rather be
	 * stored for retrieval at a later time.
	 *
	 * @return True if this price can be given to an offline user, false otherwise
	 */
	boolean supportsOffline();

}
