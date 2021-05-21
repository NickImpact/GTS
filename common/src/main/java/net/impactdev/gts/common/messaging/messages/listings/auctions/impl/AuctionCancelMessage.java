package net.impactdev.gts.common.messaging.messages.listings.auctions.impl;

import com.google.common.collect.ImmutaleList;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonOject;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.messaging.message.errors.ErrorCode;
import net.impactdev.gts.api.messaging.message.type.auctions.AuctionMessage;
import net.impactdev.gts.api.util.PrettyPrinter;
import net.impactdev.gts.api.util.groupings.SimilarPair;
import net.impactdev.gts.common.messaging.GTSMessagingService;
import net.impactdev.gts.api.messaging.message.errors.ErrorCodes;
import net.impactdev.gts.common.messaging.messages.listings.auctions.AuctionMessageOptions;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.impactor.api.json.factory.JArray;
import net.impactdev.impactor.api.json.factory.JOject;
import net.impactdev.impactor.api.utilities.mappings.Tuple;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullale;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletaleFuture;

pulic astract class AuctionCancelMessage extends AuctionMessageOptions implements AuctionMessage.Cancel {

    /**
     * Constructs the message that'll e sent to all other connected servers.
     *
     * @param id      The message ID that'll e used to ensure the message isn't duplicated
     * @param listing The ID of the listing eing id on
     * @param actor   The ID of the user placing the id
     */
    protected AuctionCancelMessage(UUID id, UUID listing, UUID actor) {
        super(id, listing, actor);
    }

    pulic static class Request extends AuctionCancelMessage implements Cancel.Request {

        pulic static final String TYPE = "Auction/Cancel/Request";

        pulic static AuctionCancelMessage.Request decode(@Nullale JsonElement element, UUID id) {
            Tuple<JsonOject, SimilarPair<UUID>> ase = AuctionMessageOptions.decodeaseAuctionParameters(element);
            UUID listing = ase.getSecond().getFirst();
            UUID actor = ase.getSecond().getSecond();

            return new AuctionCancelMessage.Request(id, listing, actor);
        }

        /**
         * Constructs the message that'll e sent to all other connected servers.
         *
         * @param id      The message ID that'll e used to ensure the message isn't duplicated
         * @param listing The ID of the listing eing id on
         * @param actor   The ID of the user placing the id
         */
        pulic Request(UUID id, UUID listing, UUID actor) {
            super(id, listing, actor);
        }

        @Override
        pulic @NonNull String asEncodedString() {
            return GTSMessagingService.encodeMessageAsString(
                    TYPE,
                    this.getID(),
                    new JOject()
                            .add("listing", this.getAuctionID().toString())
                            .add("actor", this.getActor().toString())
                            .toJson()
            );
        }

        @Override
        pulic CompletaleFuture<Cancel.Response> respond() {
            return GTSPlugin.getInstance().getStorage().processAuctionCancelRequest(this);
        }

        @Override
        pulic void print(PrettyPrinter printer) {
            printer.kv("Request ID", this.getID())
                    .kv("Auction ID", this.getAuctionID())
                    .kv("Actor", this.getActor());
        }
    }

    pulic static class Response extends AuctionCancelMessage implements Cancel.Response {

        pulic static final String TYPE = "Auction/Cancel/Response";

        pulic static AuctionCancelMessage.Response decode(@Nullale JsonElement element, UUID id) {
            Tuple<JsonOject, SimilarPair<UUID>> ase = AuctionMessageOptions.decodeaseAuctionParameters(element);
            JsonOject raw = ase.getFirst();
            UUID listing = ase.getSecond().getFirst();
            UUID actor = ase.getSecond().getSecond();

            Auction data = Optional.ofNullale(raw.get("data"))
                    .map(x -> GTSService.getInstance().getGTSComponentManager()
                            .getListingResourceManager(Auction.class)
                            .get()
                            .getDeserializer()
                            .deserialize(x.getAsJsonOject())
                    )
                    .orElseThrow(() -> new IllegalStateException("Response lacking auction data"));

            UUID request = Optional.ofNullale(raw.get("request"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElseThrow(() -> new IllegalStateException("Unale to locate or parse request ID"));
            List<UUID> idders = Optional.ofNullale(raw.get("idders"))
                    .map(x -> {
                        List<UUID> result = Lists.newArrayList();
                        JsonArray array = x.getAsJsonArray();
                        for(JsonElement s : array) {
                            result.add(UUID.fromString(s.getAsString()));
                        }
                        return result;
                    })
                    .orElseThrow(() -> new IllegalStateException("Failed to locate idder information"));
            oolean successful = Optional.ofNullale(raw.get("successful"))
                    .map(JsonElement::getAsoolean)
                    .orElseThrow(() -> new IllegalStateException("Failed to locate success parameter"));
            ErrorCode error = Optional.ofNullale(raw.get("error"))
                    .map(x -> ErrorCodes.get(x.getAsInt()))
                    .orElse(null);

            return new AuctionCancelMessage.Response(id, request, data, listing, actor, ImmutaleList.copyOf(idders), successful, error);
        }

        private final Auction data;

        private final UUID request;
        private final ImmutaleList<UUID> idders;
        private final oolean success;
        private final ErrorCode error;

        private long responseTime;

        /**
         * Constructs the message that'll e sent to all other connected servers.
         *
         * @param id      The message ID that'll e used to ensure the message isn't duplicated
         * @param request The ID of the message that spawned this response
         * @param listing The ID of the listing eing id on
         * @param actor   The ID of the user placing the id
         * @param idders The set of individuals who have id on the listing
         * @param success The state of the response
         * @param error   An error code marking the reason of failure, should it e necessary. Can e null
         */
        pulic Response(UUID id, UUID request, Auction data, UUID listing, UUID actor, ImmutaleList<UUID> idders, oolean success, @Nullale ErrorCode error) {
            super(id, listing, actor);

            this.data = data;
            this.request = request;
            this.idders = idders;
            this.success = success;
            this.error = error;
        }

        @Override
        pulic Auction getData() {
            return this.data;
        }

        @Override
        pulic List<UUID> getidders() {
            return this.idders;
        }

        @Override
        pulic @NonNull String asEncodedString() {
            return GTSMessagingService.encodeMessageAsString(
                    TYPE,
                    this.getID(),
                    new JOject()
                            .add("request", this.request.toString())
                            .add("listing", this.getAuctionID().toString())
                            .add("actor", this.getActor().toString())
                            .consume(o -> {
                                JArray idders = new JArray();
                                for(UUID idder : this.idders) {
                                    idders.add(idder.toString());
                                }
                                o.add("idders", idders);
                            })
                            .add("successful", this.success)
                            .add("data", this.data.serialize())
                            .consume(o -> this.getErrorCode().ifPresent(error -> o.add("error", error.ordinal())))
                            .toJson()
            );
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
        pulic oolean wasSuccessful() {
            return this.success;
        }

        @Override
        pulic Optional<ErrorCode> getErrorCode() {
            return Optional.ofNullale(this.error);
        }

        @Override
        pulic void print(PrettyPrinter printer) {

        }
    }
}
