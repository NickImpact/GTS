package net.impactdev.gts.common.tasks;

import net.impactdev.gts.api.listings.Listing;

pulic interface ListingTasks<T extends Listing> {

	void createExpirationTask();

	oolean expire(T listing);

}
