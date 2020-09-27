package me.nickimpact.gts.api.stashes;

import com.nickimpact.impactor.api.Impactor;
import com.nickimpact.impactor.api.utilities.Builder;
import com.nickimpact.impactor.api.utilities.mappings.Tuple;
import me.nickimpact.gts.api.listings.Listing;

import java.util.List;
import java.util.UUID;

/**
 * Represents a stash of contents that a player may currently possess due to not being able
 * to currently receive an item. A stash may or may not be populated by items that have been
 * sold to the user but are currently unable to be claimed. This will allow the user to return
 * to this stash at any time and retrieve items that were previously non-claimable.
 */
public interface Stash {

	/**
	 * Specifies the size of the stash as it is currently.
	 *
	 * @return The current size of the stash
	 */
	default int getSize() {
		return this.getStashContents().size();
	}

	/**
	 * Specifies whether or not the user's stash is currently empty. If the stash is not empty, logic
	 * should be performed on player login to indicate to the user that they have items available
	 * to be retrieved.
	 *
	 * @return True if the stash is empty, false otherwise
	 */
	default boolean isEmpty() {
		return this.getStashContents().isEmpty();
	}

	/**
	 * Retrieves a list of stashed items within this stash. These items can then be processed, should any exist,
	 * to attempt returning the items to the player requesting it.
	 *
	 * @return The list of items contained by this Stash
	 */
	List<Tuple<Listing, Boolean>> getStashContents();

	/**
	 * Allows the player to claim a set of items from the stash. If any items remain in the stash
	 * after an initial claim attempt is made, the call will return a non-zero value indicating
	 * the amount of items remaining in the stash still requiring claim.
	 *
	 * @param claimer The user claiming items from the stash
	 * @param listing The ID of the listing being claimed
	 * @return True if the claim attempt was successful, false otherwise
	 */
	boolean claim(UUID claimer, UUID listing);

	static StashBuilder builder() {
		return Impactor.getInstance().getRegistry().createBuilder(StashBuilder.class);
	}

	interface StashBuilder extends Builder<Stash, StashBuilder> {

		StashBuilder append(Listing listing, boolean purchased);

	}

}
