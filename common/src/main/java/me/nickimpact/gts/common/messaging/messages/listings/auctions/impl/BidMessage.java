package me.nickimpact.gts.common.messaging.messages.listings.auctions.impl;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.nickimpact.gts.api.messaging.message.type.auctions.AuctionMessage;
import me.nickimpact.gts.api.util.groupings.SimilarPair;
import me.nickimpact.gts.api.util.groupings.Tuple;
import me.nickimpact.gts.common.messaging.GTSMessagingService;
import me.nickimpact.gts.common.messaging.messages.listings.auctions.AuctionMessageOptions;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import me.nickimpact.gts.common.utils.gson.JObject;
import org.checkerframework.checker.index.qual.Positive;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.net.SocketAddress;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("Duplicates")
public class BidMessage extends AuctionMessageOptions implements AuctionMessage.Bid.Request {

	/** Specifies the typing for this message. AKA, the identifier from a standpoint on knowing the incoming message type */
	public static final String TYPE = "Auction/Bid";

	/**
	 * Attempts to decode a new AuctionMessageOptions from the raw JSON data. This call will only fail exceptionally
	 * when the raw JSON data is either missing or lacking a component that this message should be populated with.
	 *
	 * @param element The raw JSON data representing this message
	 * @param id The ID of the message received
	 * @return A deserialized version of the message matching a AuctionMessageOptions
	 */
	public static AuctionMessageOptions decode(@Nullable JsonElement element, UUID id) {
		Tuple<JsonObject, SimilarPair<UUID>> base = AuctionMessageOptions.decodeBaseAuctionParameters(element);
		JsonObject raw = base.getFirst();
		UUID listing = base.getSecond().getFirst();
		UUID actor = base.getSecond().getSecond();
		double bid = Optional.ofNullable(raw.get("bid"))
				.map(JsonElement::getAsDouble)
				.orElseThrow(() -> new IllegalStateException("Failed to locate bid amount"));

		return new BidMessage(id, listing, actor, bid);
	}

	/** The amount the user has just bid on the listing */
	private final double bid;

	/**
	 * Constructs the message that'll be sent to all other connected servers.
	 *
	 * @param id      The message ID that'll be used to ensure the message isn't duplicated
	 * @param listing The ID of the listing being bid on
	 * @param actor   The ID of the user placing the bid
	 * @param bid     The amount that was bid on the listing
	 */
	public BidMessage(UUID id, UUID listing, UUID actor, double bid) {
		super(id, listing, actor);
		Preconditions.checkArgument(bid > 0, "The input bid must be positive");

		this.bid = bid;
	}

	@Override
	public @NonNull String asEncodedString() {
		return GTSMessagingService.encodeMessageAsString(
				TYPE,
				this.getID(),
				new JObject()
						.add("listing", this.getAuctionID().toString())
						.add("actor", this.getActor().toString())
						.add("bid", this.bid)
						.toJson()
		);
	}

	@Override
	public @Positive double getAmountBid() {
		return this.bid;
	}

	@Override
	public CompletableFuture<Bid.Response> respond() {
		return GTSPlugin.getInstance().getStorage().processBid(this);
	}
}
