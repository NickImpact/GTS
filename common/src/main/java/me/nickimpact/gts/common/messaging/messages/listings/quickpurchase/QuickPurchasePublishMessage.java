package me.nickimpact.gts.common.messaging.messages.listings.quickpurchase;

import me.nickimpact.gts.api.messaging.message.type.listings.QuickPurchaseMessage;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

public class QuickPurchasePublishMessage implements QuickPurchaseMessage.Publish {

	@Override
	public UUID getListingID() {
		return null;
	}

	@Override
	public UUID getActor() {
		return null;
	}

	@Override
	public String getBroadcastMessage() {
		return null;
	}

	@Override
	public @NonNull String asEncodedString() {
		return null;
	}

	@Override
	public @NonNull UUID getID() {
		return null;
	}
}
