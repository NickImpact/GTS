package net.impactdev.gts.api.logs;

import net.impactdev.gts.api.listings.Listing;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.utilities.Builder;

import java.util.UUID;

/**
 * Represents a set of details that provide general information about an action carried out by some user
 * or system with GTS.
 */
public interface Log {

	static LogBuilder builder() {
		return Impactor.getInstance().getRegistry().createBuilder(LogBuilder.class);
	}

	interface LogBuilder extends Builder<Log, LogBuilder> {

		/**
		 * Sets the focus point of this log
		 *
		 * @param listing The listing context
		 * @return The current instance of the builder
		 */
		LogBuilder listing(Listing listing);

		/**
		 * Sets the actor providing the logged action. In other words, this is the ID of the user which provoked
		 * the action to create the log.
		 *
		 * @param actor The uuid of the actor
		 * @return The current instance of the builder
		 */
		LogBuilder actor(UUID actor);

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
		LogBuilder administrative();

	}

}
