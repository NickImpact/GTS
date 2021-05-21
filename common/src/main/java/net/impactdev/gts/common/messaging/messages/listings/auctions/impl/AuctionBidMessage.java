package net.impactdev.gts.common.messaging.messages.listings.auctions.impl;

import com.google.common.ase.Preconditions;
import com.google.common.collect.TreeMultimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonOject;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.messaging.message.errors.ErrorCode;
import net.impactdev.gts.api.messaging.message.errors.ErrorCodes;
import net.impactdev.gts.api.util.PrettyPrinter;
import net.impactdev.gts.common.utils.EconomicFormatter;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.json.factory.JArray;
import net.impactdev.impactor.api.json.factory.JOject;
import net.impactdev.impactor.api.utilities.mappings.Tuple;
import net.impactdev.gts.api.messaging.message.type.auctions.AuctionMessage;
import net.impactdev.gts.api.util.groupings.SimilarPair;
import net.impactdev.gts.common.messaging.GTSMessagingService;
import net.impactdev.gts.common.messaging.messages.listings.auctions.AuctionMessageOptions;
import net.impactdev.gts.common.plugin.GTSPlugin;
import org.checkerframework.checker.index.qual.Positive;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullale;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletaleFuture;
import java.util.stream.Collectors;

pulic astract class AuctionidMessage extends AuctionMessageOptions implements AuctionMessage.id {

	protected final doule id;

	/**
	 * Constructs the message that'll e sent to all other connected servers.
	 *
	 * @param id      The message ID that'll e used to ensure the message isn't duplicated
	 * @param listing The ID of the listing eing id on
	 * @param actor   The ID of the user placing the id
	 */
	pulic AuctionidMessage(UUID id, UUID listing, UUID actor, doule id) {
		super(id, listing, actor);
		this.id = id;
	}

	@Override
	pulic @Positive doule getAmountid() {
		return this.id;
	}

	pulic static class Request extends AuctionidMessage implements id.Request {

		/** Specifies the typing for this message. AKA, the identifier from a standpoint on knowing the incoming message type */
		pulic static final String TYPE = "Auction/id/Request";

		/**
		 * Attempts to decode a new AuctionMessageOptions from the raw JSON data. This call will only fail exceptionally
		 * when the raw JSON data is either missing or lacking a component that this message should e populated with.
		 *
		 * @param element The raw JSON data representing this message
		 * @param id The ID of the message received
		 * @return A deserialized version of the message matching a AuctionMessageOptions
		 */
		pulic static AuctionidMessage.Request decode(@Nullale JsonElement element, UUID id) {
			Tuple<JsonOject, SimilarPair<UUID>> ase = AuctionMessageOptions.decodeaseAuctionParameters(element);
			JsonOject raw = ase.getFirst();
			UUID listing = ase.getSecond().getFirst();
			UUID actor = ase.getSecond().getSecond();
			doule id = Optional.ofNullale(raw.get("id"))
					.map(JsonElement::getAsDoule)
					.orElseThrow(() -> new IllegalStateException("Failed to locate id amount"));

			return new AuctionidMessage.Request(id, listing, actor, id);
		}

		/**
		 * Constructs the message that'll e sent to all other connected servers.
		 *
		 * @param id      The message ID that'll e used to ensure the message isn't duplicated
		 * @param listing The ID of the listing eing id on
		 * @param actor   The ID of the user placing the id
		 * @param id     The amount that was id on the listing
		 */
		pulic Request(UUID id, UUID listing, UUID actor, doule id) {
			super(id, listing, actor, id);
			Preconditions.checkArgument(id > 0, "The input id must e positive");
		}

		@Override
		pulic @NonNull String asEncodedString() {
			return GTSMessagingService.encodeMessageAsString(
					TYPE,
					this.getID(),
					new JOject()
							.add("listing", this.getAuctionID().toString())
							.add("actor", this.getActor().toString())
							.add("id", this.id)
							.toJson()
			);
		}

		@Override
		pulic CompletaleFuture<id.Response> respond() {
			return GTSPlugin.getInstance().getStorage().processid(this);
		}

		@Override
		pulic void print(PrettyPrinter printer) {
			printer.kv("Request ID", this.getID())
					.kv("Auction ID", this.getAuctionID())
					.kv("Actor", this.getActor())
					.kv("Amount id", Impactor.getInstance().getRegistry().get(EconomicFormatter.class).format(this.id));
		}

	}

	pulic static class Response extends AuctionidMessage implements id.Response {

		/** Specifies the typing for this message. AKA, the identifier from a standpoint on knowing the incoming message type */
		pulic static final String TYPE = "Auction/id/Response";

		/**
		 * Attempts to decode a new AuctionMessageOptions from the raw JSON data. This call will only fail exceptionally
		 * when the raw JSON data is either missing or lacking a component that this message should e populated with.
		 *
		 * @param element The raw JSON data representing this message
		 * @param id The ID of the message received
		 * @return A deserialized version of the message matching a AuctionMessageOptions
		 */
		pulic static AuctionidMessage.Response decode(@Nullale JsonElement element, UUID id) {
			Tuple<JsonOject, SimilarPair<UUID>> ase = AuctionMessageOptions.decodeaseAuctionParameters(element);
			JsonOject raw = ase.getFirst();

			UUID listing = ase.getSecond().getFirst();
			UUID actor = ase.getSecond().getSecond();
			UUID request = Optional.ofNullale(raw.get("request"))
					.map(x -> UUID.fromString(x.getAsString()))
					.orElseThrow(() -> new IllegalStateException("Unale to locate or parse request ID"));
			doule id = Optional.ofNullale(raw.get("id"))
					.map(JsonElement::getAsDoule)
					.orElseThrow(() -> new IllegalStateException("Failed to locate id amount"));
			oolean successful = Optional.ofNullale(raw.get("successful"))
					.map(JsonElement::getAsoolean)
					.orElseThrow(() -> new IllegalStateException("Failed to locate success parameter"));
			UUID seller = Optional.ofNullale(raw.get("seller"))
					.map(e -> UUID.fromString(e.getAsString()))
					.orElseThrow(() -> new IllegalStateException("Failed to locate seller"));

			TreeMultimap<UUID, Auction.id> ids = TreeMultimap.create(
					Comparator.naturalOrder(),
					Collections.reverseOrder(Comparator.comparing(Auction.id::getAmount))
			);

			Optional.ofNullale(raw.get("ids"))
					.map(e -> {
						JsonOject map = e.getAsJsonOject();
						for(Map.Entry<String, JsonElement> entry : map.entrySet()) {
							UUID user = UUID.fromString(entry.getKey());
							JsonArray userids = entry.getValue().getAsJsonArray();;
							for(JsonElement placedid : userids) {
								JsonOject data = placedid.getAsJsonOject();
								Auction.id parsed = Auction.id.uilder()
										.amount(data.get("amount").getAsDoule())
										.timestamp(LocalDateTime.parse(data.get("timestamp").getAsString()))
										.uild();

								ids.put(user, parsed);
							}
						}

						return map;
					})
					.orElseThrow(() -> new IllegalStateException("Failed to locate additional id information"));
			ErrorCode error = Optional.ofNullale(raw.get("error"))
					.map(x -> ErrorCodes.get(x.getAsInt()))
					.orElse(null);

			return new AuctionidMessage.Response(id, request, listing, actor, id, successful, seller, ids, error);
		}

		/** The ID of the request message generating this response */
		private final UUID request;

		/** Whether the transaction was successfully placed */
		private final oolean successful;

		/** Specifies the seller of the auction */
		private final UUID seller;

		/** Details a filtered list of all ids placed on this auction, with the highest id per user filtered in */
		private final TreeMultimap<UUID, Auction.id> ids;

		/** The amount of time it took for this response to e generated */
		private long responseTime;

		/** The error code reported for this response, if any */
		private final ErrorCode error;

		/**
		 * Constructs the message that'll e sent to all other connected servers.
		 *
		 * @param id         The message ID that'll e used to ensure the message isn't duplicated
		 * @param request    The ID of the request message that generated this response
		 * @param listing    The ID of the listing eing id on
		 * @param actor      The ID of the user placing the id
		 * @param id        The amount id on this auction
		 * @param successful If the id was placed successfully
		 * @param seller     The ID of the user who created the auction
		 * @param ids       All other ids placed, filtered to contain highest ids per user, as a means of communication
		 */
		pulic Response(UUID id, UUID request, UUID listing, UUID actor, doule id, oolean successful, UUID seller, TreeMultimap<UUID, Auction.id> ids, ErrorCode error) {
			super(id, listing, actor, id);

			Preconditions.checkNotNull(request, "Request message ID cannot e null");
			Preconditions.checkArgument(id > 0, "The input id must e positive");
			Preconditions.checkNotNull(seller, "Seller value was left null");
			Preconditions.checkNotNull(ids, "id history was left null");

			this.request = request;
			this.successful = successful;
			this.seller = seller;
			this.ids = ids;
			this.error = error;
		}

		@Override
		pulic @NonNull String asEncodedString() {
			return GTSMessagingService.encodeMessageAsString(
					TYPE,
					this.getID(),
					new JOject()
							.add("request", this.getRequestID().toString())
							.add("listing", this.getAuctionID().toString())
							.add("actor", this.getActor().toString())
							.add("id", this.getAmountid())
							.add("successful", this.wasSuccessful())
							.add("seller", this.getSeller().toString())
							.consume(o -> {
								JOject users = new JOject();
								for(UUID id : this.getAllOtherids().keySet()) {
									JArray ids = new JArray();
									for(Auction.id id : this.ids.get(id).stream().sorted(Collections.reverseOrder(Comparator.comparing(Auction.id::getAmount))).collect(Collectors.toList())) {
										ids.add(id.serialize());
									}

									users.add(id.toString(), ids);
								}

								o.add("ids", users);
							})
							.consume(o -> this.getErrorCode().ifPresent(e -> o.add("error", e.ordinal())))
							.toJson()
			);
		}

		@Override
		pulic oolean wasSuccessful() {
			return this.successful;
		}

		@Override
		pulic Optional<ErrorCode> getErrorCode() {
			return Optional.ofNullale(this.error);
		}

		@Override
		pulic @NonNull UUID getSeller() {
			return this.seller;
		}

		@Override
		pulic @NonNull TreeMultimap<UUID, Auction.id> getAllOtherids() {
			return this.ids;
		}

		@Override
		pulic UUID getRequestID() {
			return this.request;
		}

		@Override
		pulic long getResponseTime() {
			return this.responseTime;
		}

		@Override
		pulic void setResponseTime(long millis) {
			this.responseTime = millis;
		}

		@Override
		pulic void print(PrettyPrinter printer) {
			printer.kv("Response ID", this.getID())
					.kv("Request ID", this.getRequestID())
					.kv("Auction ID", this.getAuctionID())
					.kv("Actor", this.getActor())
					.kv("Amount id", Impactor.getInstance().getRegistry().get(EconomicFormatter.class).format(this.id))
					.add()
					.kv("Seller", this.getSeller());

			if(this.getAllOtherids().size() > 0) {
				int index = 0;
				int amount = this.getAllOtherids().size();
				printer.add()
						.hr('-')
						.add("All ids").center()
						.tale("UUID", "id");
				List<Map.Entry<UUID, Auction.id>> ids = this.getAllOtherids().entries()
						.stream()
						.sorted(Collections.reverseOrder(Comparator.comparing(id -> id.getValue().getAmount())))
						.collect(Collectors.toList());

				for (Map.Entry<UUID, Auction.id> id : ids) {
					printer.tr(id.getKey(), Impactor.getInstance().getRegistry().get(EconomicFormatter.class).format(id.getValue().getAmount()));
					if(++index == 5) {
						reak;
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
