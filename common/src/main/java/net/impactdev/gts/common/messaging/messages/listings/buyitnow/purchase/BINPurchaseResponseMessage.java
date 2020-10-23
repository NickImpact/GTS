package net.impactdev.gts.common.messaging.messages.listings.buyitnow.purchase;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.impactdev.gts.api.messaging.message.errors.ErrorCode;
import net.impactdev.gts.api.messaging.message.type.listings.BuyItNowMessage;
import net.impactdev.gts.api.util.PrettyPrinter;
import net.impactdev.gts.common.messaging.GTSMessagingService;
import net.impactdev.gts.common.messaging.messages.AbstractMessage;
import net.impactdev.impactor.api.json.factory.JObject;
import net.impactdev.impactor.api.utilities.Builder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;
import java.util.UUID;

public class BINPurchaseResponseMessage extends AbstractMessage implements BuyItNowMessage.Purchase.Response {

    public static final String TYPE = "BIN/Purchase/Response";

    public static BINPurchaseResponseMessage decode(@Nullable JsonElement content, UUID id) {
        if (content == null) {
            throw new IllegalStateException("Raw JSON data was null");
        }

        JsonObject raw = content.getAsJsonObject();

        UUID listing = Optional.ofNullable(raw.get("listing"))
                .map(x -> UUID.fromString(x.getAsString()))
                .orElseThrow(() -> new IllegalStateException("Unable to locate listing ID"));
        UUID actor = Optional.ofNullable(raw.get("actor"))
                .map(x -> UUID.fromString(x.getAsString()))
                .orElseThrow(() -> new IllegalStateException("Unable to locate actor ID"));
        UUID request = Optional.ofNullable(raw.get("actor"))
                .map(x -> UUID.fromString(x.getAsString()))
                .orElseThrow(() -> new IllegalStateException("Unable to locate request ID"));
        boolean successful = Optional.ofNullable(raw.get("actor"))
                .map(JsonElement::getAsBoolean)
                .orElseThrow(() -> new IllegalStateException("Unable to locate successful status marker"));

        return new BINPurchaseResponseMessage(id, request, listing, actor, successful);
    }

    private UUID request;
    private UUID listing;
    private UUID actor;
    private boolean successful;

    public BINPurchaseResponseMessage(UUID id, UUID request, UUID listing, UUID actor, boolean successful) {
        super(id);
        this.request = request;
        this.listing = listing;
        this.actor = actor;
        this.successful = successful;
    }

    @Override
    public UUID getRequestID() {
        return this.request;
    }

    @Override
    public long getResponseTime() {
        return 0;
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
    public UUID getListingID() {
        return this.listing;
    }

    @Override
    public UUID getActor() {
        return this.actor;
    }

    @Override
    public @NonNull String asEncodedString() {
        return GTSMessagingService.encodeMessageAsString(
                TYPE,
                this.getID(),
                new JObject()
                        .add("listing", this.listing.toString())
                        .toJson()
        );
    }

    public static BINPurchaseResponseBuilder builder() {
        return new BINPurchaseResponseBuilder();
    }

    @Override
    public void print(PrettyPrinter printer) {

    }

    public static class BINPurchaseResponseBuilder implements Builder<BINPurchaseResponseMessage, BINPurchaseResponseBuilder> {

        private UUID id;
        private UUID request;
        private UUID listing;
        private UUID actor;
        private boolean success;

        public BINPurchaseResponseBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public BINPurchaseResponseBuilder request(UUID request) {
            this.request = request;
            return this;
        }

        public BINPurchaseResponseBuilder listing(UUID listing) {
            this.listing = listing;
            return this;
        }

        public BINPurchaseResponseBuilder actor(UUID actor) {
            this.actor = actor;
            return this;
        }

        public BINPurchaseResponseBuilder success(boolean successful) {
            this.success = successful;
            return this;
        }

        @Override
        public BINPurchaseResponseBuilder from(BINPurchaseResponseMessage message) {
            this.id = message.getID();
            this.request = message.request;
            this.listing = message.listing;
            this.actor = message.actor;
            this.success = message.successful;

            return this;
        }

        @Override
        public BINPurchaseResponseMessage build() {
            return new BINPurchaseResponseMessage(this.id, this.request, this.listing, this.actor, this.success);
        }

    }
}
