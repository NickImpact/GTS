package me.nickimpact.gts.common.messaging.messages.listings.auctions.impl;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.nickimpact.impactor.api.json.factory.JObject;
import com.nickimpact.impactor.api.utilities.mappings.Tuple;
import me.nickimpact.gts.api.messaging.message.errors.ErrorCode;
import me.nickimpact.gts.api.messaging.message.type.auctions.AuctionMessage;
import me.nickimpact.gts.api.util.groupings.SimilarPair;
import me.nickimpact.gts.common.messaging.GTSMessagingService;
import me.nickimpact.gts.common.messaging.messages.listings.auctions.AuctionMessageOptions;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import org.checkerframework.checker.index.qual.Positive;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("Duplicates")
public class BidResponseMessage extends AuctionMessageOptions implements AuctionMessage.Bid.Response {

	/** Specifies the typing for this message. AKA, the identifier from a standpoint on knowing the incoming message type */
	public static final String TYPE = "Auction/Bid/Response";

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
		UUID request = Optional.ofNullable(raw.get("request"))
				.map(x -> UUID.fromString(x.getAsString()))
				.orElseThrow(() -> new IllegalStateException("Unable to locate or parse request ID"));
		double bid = Optional.ofNullable(raw.get("bid"))
				.map(JsonElement::getAsDouble)
				.orElseThrow(() -> new IllegalStateException("Failed to locate bid amount"));
		boolean successful = Optional.ofNullable(raw.get("successful"))
				.map(JsonElement::getAsBoolean)
				.orElseThrow(() -> new IllegalStateException("Failed to locate success parameter"));
		UUID seller = Optional.ofNullable(raw.get("seller"))
				.map(e -> UUID.fromString(e.getAsString()))
				.orElseThrow(() -> new IllegalStateException("Failed to locate seller"));
		Map<UUID, Double> bids = Optional.ofNullable(raw.get("bids"))
				.map(e -> {
					Gson gson = GTSPlugin.getInstance().getGson();
					return gson.<Map<UUID, Double>>fromJson(e, new TypeToken<Map<UUID, Double>>(){}.getType());
				})
				.orElseThrow(() -> new IllegalStateException("Failed to locate additional bid information"));

		return new BidResponseMessage(id, request, listing, actor, bid, successful, seller, bids);
	}

	/** The ID of the request message generating this response */
	private UUID request;

	/** The bid placed on the auction */
	private double bid;

	/** Whether the transaction was successfully placed */
	private boolean successful;

	/** Specifies the seller of the auction */
	private UUID seller;

	/** Details a filtered list of all bids placed on this auction, with the highest bid per user filtered in */
	private Map<UUID, Double> bids;

	/**
	 * Constructs the message that'll be sent to all other connected servers.
	 *
	 * @param id         The message ID that'll be used to ensure the message isn't duplicated
	 * @param request    The ID of the request message that generated this response
	 * @param listing    The ID of the listing being bid on
	 * @param actor      The ID of the user placing the bid
	 * @param bid        The amount bid on this auction
	 * @param successful If the bid was placed successfully
	 * @param seller     The ID of the user who created the auction
	 * @param bids       All other bids placed, filtered to contain highest bids per user, as a means of communication
	 */
	private BidResponseMessage(UUID id, UUID request, UUID listing, UUID actor, double bid, boolean successful, UUID seller, Map<UUID, Double> bids) {
		super(id, listing, actor);

		Preconditions.checkNotNull(request, "Request message ID cannot be null");
		Preconditions.checkArgument(bid > 0, "The input bid must be positive");
		Preconditions.checkNotNull(seller, "Seller value was left null");
		Preconditions.checkNotNull(bids, "Bid history was left null");

		this.request = request;
		this.bid = bid;
		this.successful = successful;
		this.seller = seller;
		this.bids = bids;
	}

	@Override
	public @NonNull String asEncodedString() {
		return GTSMessagingService.encodeMessageAsString(
				TYPE,
				this.getID(),
				new JObject()
						.add("request", this.getRequestID().toString())
						.add("listing", this.getAuctionID().toString())
						.add("actor", this.getActor().toString())
						.add("bid", this.getAmountBid())
						.add("successful", this.wasSuccessful())
						.add("seller", this.getSeller().toString())
						.add("bids", GTSPlugin.getInstance().getGson().toJsonTree(this.getAllOtherBids()))
						.toJson()
		);
	}

	@Override
	public @Positive double getAmountBid() {
		return this.bid;
	}

	@Override
	public boolean wasSuccessful() {
		return this.successful;
	}

	@Override
	public Optional<ErrorCode> getErrorCode() {
		return Optional.empty();
	}

	@Override
	public @NonNull UUID getSeller() {
		return this.seller;
	}

	@Override
	public @NonNull Map<UUID, Double> getAllOtherBids() {
		return this.bids;
	}

	@Override
	public UUID getRequestID() {
		return this.request;
	}

}
