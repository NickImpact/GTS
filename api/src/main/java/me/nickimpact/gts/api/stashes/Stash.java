package me.nickimpact.gts.api.stashes;

import java.util.List;

/**
 * Represents a stash of contents that a player may currently possess due to not being able
 * to currently receive an item. A stash may or may not be populated by items that have been
 * sold to the user but are currently unable to be claimed. This will allow the user to return
 * to this stash at any time and retrieve items that were previously non-claimable.
 *
 * @param <T> The type of {@link StashEntry StashEntry} that will populate this stash.
 * @param <P> The type representing the player who will be claiming this stash
 */
public interface Stash<T extends StashEntry<?>, P> {

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
	List<T> getStashContents();

	/**
	 * Allows the player to claim a set of items from the stash. If any items remain in the stash
	 * after an initial claim attempt is made, the call will return a non-zero value indicating
	 * the amount of items remaining in the stash still requiring claim.
	 *
	 * @param claimer The user claiming items from the stash
	 * @return 0 if all stashed items were successfully claimed, or a positive value indicating
	 * the amount of items still in the stash after the claim attempt.
	 */
	int claim(P claimer);

}
