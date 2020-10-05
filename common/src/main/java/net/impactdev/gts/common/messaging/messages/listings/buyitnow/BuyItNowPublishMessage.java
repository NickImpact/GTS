package net.impactdev.gts.common.messaging.messages.listings.buyitnow;

import net.impactdev.gts.api.messaging.message.type.listings.BuyItNowMessage;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

public class BuyItNowPublishMessage implements BuyItNowMessage.Publish {

	@Override
	public UUID getListingID() {
		return null;
	}

	@Override
	public UUID getActor() {
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
