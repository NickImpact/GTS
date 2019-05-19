package me.nickimpact.gts.api.listings.entries;

import com.nickimpact.impactor.api.json.JsonTyping;
import lombok.Setter;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.prices.Price;

import java.util.List;

/**
 * An element represents the actual elements we add into the GTS listings. Essentially,
 * they provide the backbone information for each type of listing. For example, one
 * element type pertains to pokemon, while another pertains to items.
 *
 *
 *
 * @param <T> The type of object that'll be held by this entry
 */
public abstract class Entry<T, D, P, U, I> {

	protected T element;

	public Entry() {}

	@Deprecated
	public Entry setEntry(T backing) {
		this.element = backing;
		return this;
	}

	public Entry(T element) {
		this.element = element;
	}

	public String getTyping() {
		return this.getClass().getAnnotation(JsonTyping.class).value();
	}

	public abstract D getEntry();

	public abstract String getSpecsTemplate();

	/**
	 * Retrieves the name of a listing
	 *
	 * @return The name of an element
	 */
	public abstract String getName();

	/**
	 * Retrieves extended details about the listing
	 *
	 * @return A list of details relating to the item
	 */
	public abstract List<String> getDetails();

	/**
	 * Represents the ItemStack that will be used to represent the element in the listing display
	 *
	 * @return An ItemStack built to represent an element
	 */
	public abstract I baseItemStack(P player, Listing listing);

	/**
	 * States whether or not a listing can be handled for an offline player.
	 * By default, this setting is set to true for all com.nickimpact.gts.api.listings. However,
	 * things like ItemStacks, since they require an online player to go
	 * to a player's inventory, are unable to support such.
	 *
	 * @return Whether an element typing supports offline rewarding/give back
	 */
	public abstract boolean supportsOffline();

	/**
	 * Attempts to give the contents of a lot element to the passed player. This method ignores any attempt
	 * to make note of an entry's amount size, and returns everything as it currently is.
	 *
	 * @param user The user to give the entry element to
	 * @return <code>true</code> if the entry was given successfully, <code>false</code> otherwise
	 */
	public abstract boolean giveEntry(U user);

	/**
	 * Attempts to take the element away from the player depositing the listing.
	 *
	 * @param player The player who wants to deposit something
	 * @return true on success, false otherwise
	 */
	public abstract boolean doTakeAway(P player);
}
