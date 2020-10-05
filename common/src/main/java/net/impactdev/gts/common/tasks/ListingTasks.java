package net.impactdev.gts.common.tasks;

import net.impactdev.gts.api.listings.Listing;

public interface ListingTasks<T extends Listing> {

	void createExpirationTask();

	boolean expire(T listing);

}
