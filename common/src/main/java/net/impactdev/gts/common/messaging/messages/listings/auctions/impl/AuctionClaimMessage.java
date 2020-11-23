package net.impactdev.gts.common.messaging.messages.listings.auctions.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.impactdev.gts.api.util.PrettyPrinter;
import net.impactdev.gts.api.messaging.message.errors.ErrorCodes;
import net.impactdev.impactor.api.json.factory.JObject;
import net.impactdev.impactor.api.utilities.mappings.Tuple;
import net.impactdev.gts.api.messaging.message.errors.ErrorCode;
import net.impactdev.gts.api.messaging.message.type.auctions.AuctionMessage;
import net.impactdev.gts.api.util.groupings.SimilarPair;
import net.impactdev.gts.common.messaging.GTSMessagingService;
import net.impactdev.gts.common.messaging.messages.listings.auctions.AuctionMessageOptions;
import net.impactdev.gts.common.plugin.GTSPlugin;
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

        public static final String TYPE = "Auction/Claim/Request";

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

        @Override
        public void print(PrettyPrinter printer) {
            printer.kv("Request ID", this.getID())
                    .kv("Auction ID", this.getAuctionID())
                    .kv("Actor", this.getActor());
        }
    }

    public static class ClaimResponse extends AuctionClaimMessage implements AuctionMessage.Claim.Response {

        public static final String TYPE = "Auction/Claim/Response";

        public static ClaimResponse decode(@Nullable JsonElement element, UUID id) {
            Tuple<JsonObject, SimilarPair<UUID>> base = AuctionMessageOptions.decodeBaseAuctionParameters(element);
            JsonObject raw = base.getFirst();
            UUID listing = base.getSecond().getFirst();
            UUID actor = base.getSecond().getSecond();

            UUID request = Optional.ofNullable(raw.get("request"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElseThrow(() -> new IllegalStateException("Unable to locate or parse request ID"));
            boolean lister = Optional.ofNullable(raw.get("lister"))
                    .map(JsonElement::getAsBoolean)
                    .orElseThrow(() -> new IllegalStateException("Failed to locate lister status"));
            boolean winner = Optional.ofNullable(raw.get("winner"))
                    .map(JsonElement::getAsBoolean)
                    .orElseThrow(() -> new IllegalStateException("Failed to locate winner status"));
            boolean successful = Optional.ofNullable(raw.get("successful"))
                    .map(JsonElement::getAsBoolean)
                    .orElseThrow(() -> new IllegalStateException("Failed to locate successful status"));
            ErrorCode error = Optional.ofNullable(raw.get("error"))
                    .map(x -> ErrorCodes.get(x.getAsInt()))
                    .orElse(null);
            return new ClaimResponse(id, request, listing, actor, lister, winner, successful, error);
        }

        /** The ID of the request message generating this response */
        private final UUID request;

        /** Whether the lister has claimed their portion of the auction */
        private final boolean lister;

        /** Whether the winner has claimed their portion of the auction */
        private final boolean winner;

        /** Whether the transaction was successfully placed */
        private final boolean successful;

        /** The amount of time it took for this response to be generated */
        private long responseTime;

        /** The error code reported for this response, if any */
        private final ErrorCode error;

        /**
         * Constructs the message that'll be sent to all other connected servers.
         *
         * @param id      The message ID that'll be used to ensure the message isn't duplicated
         * @param listing The ID of the listing being bid on
         * @param actor   The ID of the user placing the bid
         */
        public ClaimResponse(UUID id, UUID request, UUID listing, UUID actor, boolean lister, boolean winner, boolean successful, @Nullable ErrorCode error) {
            super(id, listing, actor);
            this.request = request;
            this.lister = lister;
            this.winner = winner;
            this.successful = successful;
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
                            .add("lister", this.lister)
                            .add("winner", this.winner)
                            .add("successful", this.successful)
                            .add("error", this.error.ordinal())
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
        public boolean hasListerClaimed() {
            return this.lister;
        }

        @Override
        public boolean hasWinnerClaimed() {
            return this.winner;
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
        public void print(PrettyPrinter printer) {
            printer.kv("Response ID", this.getID())
                    .kv("Request ID", this.getRequestID())
                    .kv("Auction ID", this.getAuctionID())
                    .kv("Actor", this.getActor())
                    .kv("Has Lister Claimed", this.lister)
                    .kv("Has Winner Claimed", this.winner);
        }

    }

}
