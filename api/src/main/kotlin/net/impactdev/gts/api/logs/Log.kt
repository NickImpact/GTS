package net.impactdev.gts.api.logs

import net.impactdev.gts.api.listings.Listing
import net.impactdev.impactor.api.Impactor
import net.impactdev.impactor.api.utilities.Builder
import java.util.*

/**
 * Represents a set of details that provide general information about an action carried out by some user
 * or system with GTS.
 */
interface Log {
    interface LogBuilder : Builder<Log?, LogBuilder?> {
        /**
         * Sets the focus point of this log
         *
         * @param listing The listing context
         * @return The current instance of the builder
         */
        fun listing(listing: Listing?): LogBuilder?

        /**
         * Sets the actor providing the logged action. In other words, this is the ID of the user which provoked
         * the action to create the log.
         *
         * @param actor The uuid of the actor
         * @return The current instance of the builder
         */
        fun actor(actor: UUID?): LogBuilder?
        /**
         * Marks the action carried out for this listing
         *
         * @param action The action performed
         * @return The current instance of the builder
         */
        //LogBuilder action(ListingActionType action);
        /**
         * Marks a logged action as an administrative action
         *
         * @return The current instance of the builder
         */
        fun administrative(): LogBuilder?
    }

    companion object {
        fun builder(): LogBuilder? {
            return Impactor.getInstance().registry.createBuilder(LogBuilder::class.java)
        }
    }
}