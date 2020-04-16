package me.nickimpact.gts.api.listings.actions;

import me.nickimpact.gts.api.GTSService;
import me.nickimpact.gts.api.util.Builder;

import java.util.UUID;

public interface ListingAction {

	UUID getListingID();

	UUID getActor();

	ListingActionType getAction();

	static ListingActionBuilder builder() {
		return GTSService.getInstance().getRegistry().createBuilder(ListingActionBuilder.class);
	}

	interface ListingActionBuilder extends Builder<ListingAction, ListingActionBuilder> {

		ListingActionBuilder listing(UUID uuid);

		ListingActionBuilder action(ListingActionType action);

		ListingActionBuilder actor(UUID uuid);

	}

}
