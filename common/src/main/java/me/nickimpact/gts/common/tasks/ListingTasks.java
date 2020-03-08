package me.nickimpact.gts.common.tasks;

import me.nickimpact.gts.api.listings.Listing;

public interface ListingTasks<T extends Listing> {

	void createExpirationTask();

	boolean expire(T listing);

}
