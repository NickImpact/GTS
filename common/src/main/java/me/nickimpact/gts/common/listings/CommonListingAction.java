package me.nickimpact.gts.common.listings;

import me.nickimpact.gts.api.listings.actions.ListingAction;
import me.nickimpact.gts.api.listings.actions.ListingActionType;

import java.util.UUID;

public class CommonListingAction implements ListingAction {

	@Override
	public UUID getListingID() {
		return null;
	}

	@Override
	public UUID getActor() {
		return null;
	}

	@Override
	public ListingActionType getAction() {
		return null;
	}

}
