package net.impactdev.gts.common.messaging.messages.listings.auctions.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.TreeMultimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.messaging.message.errors.ErrorCode;
import net.impactdev.gts.api.messaging.message.errors.ErrorCodes;
import net.impactdev.gts.api.util.PrettyPrinter;
import net.impactdev.gts.common.utils.EconomicFormatter;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.json.factory.JArray;
import net.impactdev.impactor.api.json.factory.JObject;
import net.impactdev.impactor.api.utilities.mappings.Tuple;
import net.impactdev.gts.api.messaging.message.type.auctions.AuctionMessage;
import net.impactdev.gts.api.util.groupings.SimilarPair;
import net.impactdev.gts.common.messaging.GTSMessagingService;
import net.impactdev.gts.common.messaging.messages.listings.auctions.AuctionMessageOptions;
import net.impactdev.gts.common.plugin.GTSPlugin;
import org.checkerframework.checker.index.qual.Positive;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public abstract class AuctionBidMessage extends AuctionMessageOptions implements AuctionMessage.Bid {

	protected final double bid;

	/**
	 * Constructs the message that'll be sent to all other connected servers.
	 *
	 * @param id      The message ID that'll be used to ensure the message isn't duplicated
	 * @param listing The ID of the listing being bid on
	 * @param actor   The ID of the user placing the bid
	 */
	public AuctionBidMessage(UUID id, UUID listing, UUID actor, double bid) {
		super(id, listing, actor);
		this.bid = bid;
	}

	@Override
	public @Positive double getAmountBid() {
		return this.bid;
	}

	public static class Request extends AuctionBidMessage implements Bid.Request {

		/** Specifies the typing for this message. AKA, the identifier from a standpoint on knowing the incoming message type */
		public static final String TYPE = "Auction/Bid/Request";

		/**
		 * Attempts to decode a new AuctionMessageOptions from the raw JSON data. This call will only fail exceptionally
		 * when the raw JSON data is either missing or lacking a component that this message should be populated with.
		 *
		 * @param element The raw JSON data representing this message
		 * @param id The ID of the message received
		 * @return A deserialized version of the message matching a AuctionMessageOptions
		 */
		public static AuctionBidMessage.Request decode(@Nullable JsonElement element, UUID id) {
			Tuple<JsonObject, SimilarPair<UUID>> base = AuctionMessageOptions.decodeBaseAuctionParameters(element);
			JsonObject raw = base.getFirst();
			UUID listing = base.getSecond().getFirst();
			UUID actor = base.getSecond().getSecond();
			double bid = Optional.ofNullable(raw.get("bid"))
					.map(JsonElement::getAsDouble)
					.orElseThrow(() -> new IllegalStateException("Failed to locate bid amount"));

			return new AuctionBidMessage.Request(id, listing, actor, bid);
		}

		/**
		 * Constructs the message that'll be sent to all other connected servers.
		 *
		 * @param id      The message ID that'll be used to ensure the message isn't duplicated
		 * @param listing The ID of the listing being bid on
		 * @param actor   The ID of the user placing the bid
		 * @param bid     The amount that was bid on the listing
		 */
		public Request(UUID id, UUID listing, UUID actor, double bid) {
			super(id, listing, actor, bid);
			Preconditions.checkArgument(bid > 0, "The input bid must be positive");
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
		public CompletableFuture<Bid.Response> respond() {
			return GTSPlugin.getInstance().getStorage().processBid(this);
		}

		@Override
		public void print(PrettyPrinter printer) {
			printer.kv("Request ID", this.getID())
					.kv("Auction ID", this.getAuctionID())
					.kv("Actor", this.getActor())
					.kv("Amount Bid", Impactor.getInstance().getRegistry().get(EconomicFormatter.class).format(this.bid));
		}

	}

	public static class Response extends AuctionBidMessage implements Bid.Response {

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
		public static AuctionBidMessage.Response decode(@Nullable JsonElement element, UUID id) {
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

			TreeMultimap<UUID, Auction.Bid> bids = TreeMultimap.create(
					Comparator.naturalOrder(),
					Collections.reverseOrder(Comparator.comparing(Auction.Bid::getAmount))
			);

			Optional.ofNullable(raw.get("bids"))
					.map(e -> {
						JsonObject map = e.getAsJsonObject();
						for(Map.Entry<String, JsonElement> entry : map.entrySet()) {
							UUID user = UUID.fromString(entry.getKey());
							JsonArray userBids = entry.getValue().getAsJsonArray();;
							for(JsonElement placedBid : userBids) {
								JsonObject data = placedBid.getAsJsonObject();
								Auction.Bid parsed = Auction.Bid.builder()
										.amount(data.get("amount").getAsDouble())
										.timestamp(LocalDateTime.parse(data.get("timestamp").getAsString()))
										.build();

								bids.put(user, parsed);
							}
						}

						return map;
					})
					.orElseThrow(() -> new IllegalStateException("Failed to locate additional bid information"));
			ErrorCode error = Optional.ofNullable(raw.get("error"))
					.map(x -> ErrorCodes.get(x.getAsInt()))
					.orElse(null);

			return new AuctionBidMessage.Response(id, request, listing, actor, bid, successful, seller, bids, error);
		}

		/** The ID of the request message generating this response */
		private final UUID request;

		/** Whether the transaction was successfully placed */
		private final boolean successful;

		/** Specifies the seller of the auction */
		private final UUID seller;

		/** Details a filtered list of all bids placed on this auction, with the highest bid per user filtered in */
		private final TreeMultimap<UUID, Auction.Bid> bids;

		/** The amount of time it took for this response to be generated */
		private long responseTime;

		/** The error code reported for this response, if any */
		private final ErrorCode error;

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
		public Response(UUID id, UUID request, UUID listing, UUID actor, double bid, boolean successful, UUID seller, TreeMultimap<UUID, Auction.Bid> bids, ErrorCode error) {
			super(id, listing, actor, bid);

			Preconditions.checkNotNull(request, "Request message ID cannot be null");
			Preconditions.checkArgument(bid > 0, "The input bid must be positive");
			Preconditions.checkNotNull(seller, "Seller value was left null");
			Preconditions.checkNotNull(bids, "Bid history was left null");

			this.request = request;
			this.successful = successful;
			this.seller = seller;
			this.bids = bids;
			this.error = error;
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
							.consume(o -> {
								JObject users = new JObject();
								for(UUID id : this.getAllOtherBids().keySet()) {
									JArray bids = new JArray();
									for(Auction.Bid bid : this.bids.get(id).stream().sorted(Collections.reverseOrder(Comparator.comparing(Auction.Bid::getAmount))).collect(Collectors.toList())) {
										bids.add(bid.serialize());
									}

									users.add(id.toString(), bids);
								}

								o.add("bids", users);
							})
							.consume(o -> this.getErrorCode().ifPresent(e -> o.add("error", e.ordinal())))
							.toJson()
			);
		}

		@Override
		public boolean wasSuccessful() {
			return this.successful;
		}

		@Override
		public Optional<ErrorCode> getErrorCode() {
			return Optional.ofNullable(this.error);
		}

		@Override
		public @NonNull UUID getSeller() {
			return this.seller;
		}

		@Override
		public @NonNull TreeMultimap<UUID, Auction.Bid> getAllOtherBids() {
			return this.bids;
		}

		@Override
		public UUID getRequestID() {
			return this.request;
		}

		@Override
		public long getResponseTime() {
			return this.responseTime;
		}

		@Override
		public void setResponseTime(long millis) {
			this.responseTime = millis;
		}

		@Override
		public void print(PrettyPrinter printer) {
			printer.kv("Response ID", this.getID())
					.kv("Request ID", this.getRequestID())
					.kv("Auction ID", this.getAuctionID())
					.kv("Actor", this.getActor())
					.kv("Amount Bid", Impactor.getInstance().getRegistry().get(EconomicFormatter.class).format(this.bid))
					.add()
					.kv("Seller", this.getSeller());

			if(this.getAllOtherBids().size() > 0) {
				int index = 0;
				int amount = this.getAllOtherBids().size();
				printer.add()
						.hr('-')
						.add("All Bids").center()
						.table("UUID", "Bid");
				List<Map.Entry<UUID, Auction.Bid>> bids = this.getAllOtherBids().entries()
						.stream()
						.sorted(Collections.reverseOrder(Comparator.comparing(bid -> bid.getValue().getAmount())))
						.collect(Collectors.toList());

				for (Map.Entry<UUID, Auction.Bid> bid : bids) {
					printer.tr(bid.getKey(), Impactor.getInstance().getRegistry().get(EconomicFormatter.class).format(bid.getValue().getAmount()));
					if(++index == 5) {
						break;
					}
				}
				if(index == 5 && amount - index > 0) {
					printer.add("and " + (amount - index) + "more...");
				}
				printer.hr('-');
			}
		}

	}

}
