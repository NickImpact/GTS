package net.impactdev.gts.common.messaging.messages.listings.auctions.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.messaging.message.errors.ErrorCode;
import net.impactdev.gts.api.messaging.message.type.auctions.AuctionMessage;
import net.impactdev.gts.api.util.groupings.SimilarPair;
import net.impactdev.gts.common.messaging.GTSMessagingService;
import net.impactdev.gts.api.messaging.message.errors.ErrorCodes;
import net.impactdev.gts.common.messaging.messages.listings.auctions.AuctionMessageOptions;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.impactor.api.json.factory.JArray;
import net.impactdev.impactor.api.json.factory.JObject;
import net.impactdev.impactor.api.utilities.mappings.Tuple;
import net.impactdev.impactor.api.utilities.printing.PrettyPrinter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class AuctionCancelMessage extends AuctionMessageOptions implements AuctionMessage.Cancel {

    /**
     * Constructs the message that'll be sent to all other connected servers.
     *
     * @param id      The message ID that'll be used to ensure the message isn't duplicated
     * @param listing The ID of the listing being bid on
     * @param actor   The ID of the user placing the bid
     */
    protected AuctionCancelMessage(UUID id, UUID listing, UUID actor) {
        super(id, listing, actor);
    }

    public static class Request extends AuctionCancelMessage implements Cancel.Request {

        public static final String TYPE = "Auction/Cancel/Request";

        public static AuctionCancelMessage.Request decode(@Nullable JsonElement element, UUID id) {
            Tuple<JsonObject, SimilarPair<UUID>> base = AuctionMessageOptions.decodeBaseAuctionParameters(element);
            UUID listing = base.getSecond().getFirst();
            UUID actor = base.getSecond().getSecond();

            return new AuctionCancelMessage.Request(id, listing, actor);
        }

        /**
         * Constructs the message that'll be sent to all other connected servers.
         *
         * @param id      The message ID that'll be used to ensure the message isn't duplicated
         * @param listing The ID of the listing being bid on
         * @param actor   The ID of the user placing the bid
         */
        public Request(UUID id, UUID listing, UUID actor) {
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

        @Override
        public CompletableFuture<Cancel.Response> respond() {
            return GTSPlugin.instance().storage().processAuctionCancelRequest(this);
        }

        @Override
        public void print(PrettyPrinter printer) {
            printer.kv("Request ID", this.getID())
                    .kv("Auction ID", this.getAuctionID())
                    .kv("Actor", this.getActor());
        }
    }

    public static class Response extends AuctionCancelMessage implements Cancel.Response {

        public static final String TYPE = "Auction/Cancel/Response";

        public static AuctionCancelMessage.Response decode(@Nullable JsonElement element, UUID id) {
            Tuple<JsonObject, SimilarPair<UUID>> base = AuctionMessageOptions.decodeBaseAuctionParameters(element);
            JsonObject raw = base.getFirst();
            UUID listing = base.getSecond().getFirst();
            UUID actor = base.getSecond().getSecond();

            Auction data = Optional.ofNullable(raw.get("data"))
                    .map(x -> GTSService.getInstance().getGTSComponentManager()
                            .getListingResourceManager(Auction.class)
                            .get()
                            .getDeserializer()
                            .deserialize(x.getAsJsonObject())
                    )
                    .orElseThrow(() -> new IllegalStateException("Response lacking auction data"));

            UUID request = Optional.ofNullable(raw.get("request"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElseThrow(() -> new IllegalStateException("Unable to locate or parse request ID"));
            List<UUID> bidders = Optional.ofNullable(raw.get("bidders"))
                    .map(x -> {
                        List<UUID> result = Lists.newArrayList();
                        JsonArray array = x.getAsJsonArray();
                        for(JsonElement s : array) {
                            result.add(UUID.fromString(s.getAsString()));
                        }
                        return result;
                    })
                    .orElseThrow(() -> new IllegalStateException("Failed to locate bidder information"));
            boolean successful = Optional.ofNullable(raw.get("successful"))
                    .map(JsonElement::getAsBoolean)
                    .orElseThrow(() -> new IllegalStateException("Failed to locate success parameter"));
            ErrorCode error = Optional.ofNullable(raw.get("error"))
                    .map(x -> ErrorCodes.get(x.getAsInt()))
                    .orElse(null);

            return new AuctionCancelMessage.Response(id, request, data, listing, actor, ImmutableList.copyOf(bidders), successful, error);
        }

        private final Auction data;

        private final UUID request;
        private final ImmutableList<UUID> bidders;
        private final boolean success;
        private final ErrorCode error;

        private long responseTime;

        /**
         * Constructs the message that'll be sent to all other connected servers.
         *
         * @param id      The message ID that'll be used to ensure the message isn't duplicated
         * @param request The ID of the message that spawned this response
         * @param listing The ID of the listing being bid on
         * @param actor   The ID of the user placing the bid
         * @param bidders The set of individuals who have bid on the listing
         * @param success The state of the response
         * @param error   An error code marking the reason of failure, should it be necessary. Can be null
         */
        public Response(UUID id, UUID request, Auction data, UUID listing, UUID actor, ImmutableList<UUID> bidders, boolean success, @Nullable ErrorCode error) {
            super(id, listing, actor);

            this.data = data;
            this.request = request;
            this.bidders = bidders;
            this.success = success;
            this.error = error;
        }

        @Override
        public Auction getData() {
            return this.data;
        }

        @Override
        public List<UUID> getBidders() {
            return this.bidders;
        }

        @Override
        public @NonNull String asEncodedString() {
            return GTSMessagingService.encodeMessageAsString(
                    TYPE,
                    this.getID(),
                    new JObject()
                            .add("request", this.request.toString())
                            .add("listing", this.getAuctionID().toString())
                            .add("actor", this.getActor().toString())
                            .consume(o -> {
                                JArray bidders = new JArray();
                                for(UUID bidder : this.bidders) {
                                    bidders.add(bidder.toString());
                                }
                                o.add("bidders", bidders);
                            })
                            .add("successful", this.success)
                            .add("data", this.data.serialize())
                            .consume(o -> this.getErrorCode().ifPresent(error -> o.add("error", error.ordinal())))
                            .toJson()
            );
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
        public boolean wasSuccessful() {
            return this.success;
        }

        @Override
        public Optional<ErrorCode> getErrorCode() {
            return Optional.ofNullable(this.error);
        }

        @Override
        public void print(PrettyPrinter printer) {

        }
    }
}
