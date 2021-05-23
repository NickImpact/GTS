package net.impactdev.gts.api.stashes

import net.impactdev.gts.api.listings.Listing
import net.impactdev.gts.api.util.TriState
import net.impactdev.impactor.api.Impactor
import net.impactdev.impactor.api.utilities.Builder
import java.util.*

/**
 * Represents a stash of contents that a player may currently possess due to not being able
 * to currently receive an item. A stash may or may not be populated by items that have been
 * sold to the user but are currently unable to be claimed. This will allow the user to return
 * to this stash at any time and retrieve items that were previously non-claimable.
 */
interface Stash {
    /**
     * Specifies the size of the stash as it is currently.
     *
     * @return The current size of the stash
     */
    val size: Int
        get() = stashContents.size

    /**
     * Specifies whether or not the user's stash is currently empty. If the stash is not empty, logic
     * should be performed on player login to indicate to the user that they have items available
     * to be retrieved.
     *
     * @return True if the stash is empty, false otherwise
     */
    val isEmpty: Boolean
        get() = stashContents.isEmpty()

    /**
     * Retrieves a list of stashed items within this stash. These items can then be processed, should any exist,
     * to attempt returning the items to the player requesting it.
     *
     * @return The list of items contained by this Stash
     */
    val stashContents: List<StashedContent?>

    /**
     * Allows the player to claim a set of items from the stash. If any items remain in the stash
     * after an initial claim attempt is made, the call will return a non-zero value indicating
     * the amount of items remaining in the stash still requiring claim.
     *
     * @param claimer The user claiming items from the stash
     * @param listing The ID of the listing being claimed
     * @return True if the claim attempt was successful, false otherwise
     */
    fun claim(claimer: UUID?, listing: UUID?): Boolean
    interface StashBuilder : Builder<Stash?, StashBuilder?> {
        /**
         * Appends a listing to the stash with the specified context. Context means are as such:
         *
         * `true`: The user purchased the listing
         * `false`: The user listed the listing
         * `undefined`: This listing is an auction and the user bid on the listing, but didn't win
         *
         * @param listing The listing that is apart of the stashed content
         * @param context The contextual reason on what caused this listing to appear in the stash
         * @return The builder as updated following this call
         */
        fun append(listing: Listing?, context: TriState?): StashBuilder?
    }

    companion object {
        @kotlin.jvm.JvmStatic
		fun builder(): StashBuilder? {
            return Impactor.getInstance().registry.createBuilder(StashBuilder::class.java)
        }
    }
}