package me.nickimpact.gts.common.messaging.messages.listings.auctions.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nickimpact.impactor.api.json.factory.JObject;
import me.nickimpact.gts.api.messaging.message.type.auctions.AuctionMessage;
import me.nickimpact.gts.api.util.groupings.SimilarPair;
import me.nickimpact.gts.api.util.groupings.Tuple;
import me.nickimpact.gts.common.messaging.GTSMessagingService;
import me.nickimpact.gts.common.messaging.messages.listings.auctions.AuctionMessageOptions;
import net.kyori.text.TextComponent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;
import java.util.UUID;

public class AuctionPublishMessage extends AuctionMessageOptions implements AuctionMessage.Publish {

	private static final String TYPE = "Auction/Publish";

	public static AuctionPublishMessage decode(@Nullable JsonElement element, UUID id) {
		Tuple<JsonObject, SimilarPair<UUID>> base = AuctionMessageOptions.decodeBaseAuctionParameters(element);
		JsonObject raw = base.getFirst();
		UUID listing = base.getSecond().getFirst();
		UUID actor = base.getSecond().getSecond();
		String broadcast = Optional.ofNullable(raw.get("broadcast"))
				.map(JsonElement::getAsString)
				.orElseThrow(() -> new IllegalStateException("Failed to locate broadcast message"));

		return new AuctionPublishMessage(id, listing, actor);
	}

	/**
	 * Constructs the message that'll be sent to all other connected servers.
	 *
	 * @param id      The message ID that'll be used to ensure the message isn't duplicated
	 * @param listing The ID of the listing being bid on
	 * @param actor   The ID of the user placing the bid
	 */
	public AuctionPublishMessage(UUID id, UUID listing, UUID actor) {
		super(id, listing, actor);
	}

	@Override
	public @NonNull String asEncodedString() {
		return GTSMessagingService.encodeMessageAsString(
				TYPE,
				this.getID(),
				new JObject()
						.add("listing", this.getAuctionID().toString())
						.add("actor", this.getActor().toString())
						.toJson()
		);
	}
}
