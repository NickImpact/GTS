package me.nickimpact.gts.common.messaging.messages.listings.auctions.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nickimpact.impactor.api.json.factory.JObject;
import com.nickimpact.impactor.api.utilities.mappings.Tuple;
import me.nickimpact.gts.api.messaging.message.errors.ErrorCode;
import me.nickimpact.gts.api.messaging.message.type.auctions.AuctionMessage;
import me.nickimpact.gts.api.util.groupings.SimilarPair;
import me.nickimpact.gts.common.messaging.GTSMessagingService;
import me.nickimpact.gts.common.messaging.messages.listings.auctions.AuctionMessageOptions;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class AuctionClaimMessage extends AuctionMessageOptions implements AuctionMessage.Claim {

    /**
     * Constructs the message that'll be sent to all other connected servers.
     *
     * @param id      The message ID that'll be used to ensure the message isn't duplicated
     * @param listing The ID of the listing being bid on
     * @param actor   The ID of the user placing the bid
     */
    protected AuctionClaimMessage(UUID id, UUID listing, UUID actor) {
        super(id, listing, actor);
    }

    public static class ClaimRequest extends AuctionClaimMessage implements AuctionMessage.Claim.Request {

        private static final String TYPE = "Auction/Claim/Request";

        public static ClaimRequest decode(@Nullable JsonElement element, UUID id) {
            Tuple<JsonObject, SimilarPair<UUID>> base = AuctionMessageOptions.decodeBaseAuctionParameters(element);
            JsonObject raw = base.getFirst();
            UUID listing = base.getSecond().getFirst();
            UUID actor = base.getSecond().getSecond();

            boolean isLister = Optional.ofNullable(raw.get("isLister"))
                    .map(JsonElement::getAsBoolean)
                    .orElseThrow(() -> new IllegalStateException("Failed to locate isLister field"));
            return new ClaimRequest(id, listing, actor, isLister);
        }

        private final boolean isLister;

        /**
         * Constructs the message that'll be sent to all other connected servers.
         *
         * @param id      The message ID that'll be used to ensure the message isn't duplicated
         * @param listing The ID of the listing being bid on
         * @param actor   The ID of the user placing the bid
         */
        public ClaimRequest(UUID id, UUID listing, UUID actor, boolean isLister) {
            super(id, listing, actor);
            this.isLister = isLister;
        }

        @Override
        public boolean isLister() {
            return this.isLister;
        }

        @Override
        public @NonNull String asEncodedString() {
            return GTSMessagingService.encodeMessageAsString(
                    TYPE,
                    this.getID(),
                    new JObject()
                            .add("listing", this.getAuctionID().toString())
                            .add("actor", this.getActor().toString())
                            .add("isLister", this.isLister)
                            .toJson()
            );
        }

        @Override
        public CompletableFuture<Claim.Response> respond() {
            return GTSPlugin.getInstance().getStorage().processAuctionClaimRequest(this);
        }
    }

    public static class ClaimResponse extends AuctionClaimMessage implements AuctionMessage.Claim.Response {

        private static final String TYPE = "Auction/Claim/Response";

        public static ClaimResponse decode(@Nullable JsonElement element, UUID id) {
            Tuple<JsonObject, SimilarPair<UUID>> base = AuctionMessageOptions.decodeBaseAuctionParameters(element);
            JsonObject raw = base.getFirst();
            UUID listing = base.getSecond().getFirst();
            UUID actor = base.getSecond().getSecond();

            UUID request = Optional.ofNullable(raw.get("request"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElseThrow(() -> new IllegalStateException("Unable to locate or parse request ID"));
            boolean successful = Optional.ofNullable(raw.get("isLister"))
                    .map(JsonElement::getAsBoolean)
                    .orElseThrow(() -> new IllegalStateException("Failed to locate successful status"));
            return new ClaimResponse(id, request, listing, actor, successful);
        }

        private UUID request;
        private boolean successful;

        /**
         * Constructs the message that'll be sent to all other connected servers.
         *
         * @param id      The message ID that'll be used to ensure the message isn't duplicated
         * @param listing The ID of the listing being bid on
         * @param actor   The ID of the user placing the bid
         */
        public ClaimResponse(UUID id, UUID request, UUID listing, UUID actor, boolean successful) {
            super(id, listing, actor);
            this.request = request;
            this.successful = successful;
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
                            .add("successful", this.successful)
                            .toJson()
            );
        }

        @Override
        public UUID getRequestID() {
            return this.request;
        }

        @Override
        public boolean wasSuccessful() {
            return this.successful;
        }

        @Override
        public Optional<ErrorCode> getErrorCode() {
            return Optional.empty();
        }
    }
}
