package net.impactdev.gts.common.messaging.messages.listings.buyitnow.purchase;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.impactdev.gts.api.messaging.message.errors.ErrorCode;
import net.impactdev.gts.api.messaging.message.type.listings.BuyItNowMessage;
import net.impactdev.gts.common.messaging.GTSMessagingService;
import net.impactdev.gts.api.messaging.message.errors.ErrorCodes;
import net.impactdev.gts.common.messaging.messages.AbstractMessage;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.impactor.api.json.factory.JObject;
import net.impactdev.impactor.api.utilities.printing.PrettyPrinter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class BINPurchaseMessage extends AbstractMessage implements BuyItNowMessage.Purchase {

    protected final UUID listing;
    protected final UUID actor;

    public BINPurchaseMessage(UUID id, UUID listing, UUID actor) {
        super(id);
        this.listing = listing;
        this.actor = actor;
    }

    @Override
    public UUID getListingID() {
        return this.listing;
    }

    @Override
    public UUID getActor() {
        return this.actor;
    }

    public static class Request extends BINPurchaseMessage implements Purchase.Request {

        public static final String TYPE = "BIN/Purchase/Request";

        public static BINPurchaseMessage.Request decode(@Nullable JsonElement content, UUID id) {
            if(content == null) {
                throw new IllegalStateException("Raw JSON data was null");
            }

            JsonObject raw = content.getAsJsonObject();

            UUID listing = Optional.ofNullable(raw.get("listing"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElseThrow(() -> new IllegalStateException("Unable to locate listing ID"));
            UUID actor = Optional.ofNullable(raw.get("actor"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElseThrow(() -> new IllegalStateException("Unable to locate actor ID"));

            return new BINPurchaseMessage.Request(id, listing, actor);
        }

        public Request(UUID id, UUID listing, UUID actor) {
            super(id, listing, actor);
        }

        @Override
        public @NonNull String asEncodedString() {
            return GTSMessagingService.encodeMessageAsString(
                    TYPE,
                    this.getID(),
                    new JObject()
                            .add("listing", this.listing.toString())
                            .add("actor", this.actor.toString())
                            .toJson()
            );
        }


        @Override
        public CompletableFuture<Purchase.Response> respond() {
            return GTSPlugin.instance().storage().processPurchase(this);
        }

        @Override
        public void print(PrettyPrinter printer) {
            printer.kv("Request ID", this.getID())
                    .kv("Listing ID", this.getListingID())
                    .kv("Actor", this.getActor());
        }
    }

    public static class Response extends BINPurchaseMessage implements Purchase.Response {

        public static final String TYPE = "BIN/Purchase/Response";

        public static BINPurchaseMessage.Response decode(@Nullable JsonElement content, UUID id) {
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
            UUID request = Optional.ofNullable(raw.get("request"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElseThrow(() -> new IllegalStateException("Unable to locate request ID"));
            UUID seller = Optional.ofNullable(raw.get("seller"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElseThrow(() -> new IllegalStateException("Unable to locate seller ID"));
            boolean successful = Optional.ofNullable(raw.get("successful"))
                    .map(JsonElement::getAsBoolean)
                    .orElseThrow(() -> new IllegalStateException("Unable to locate successful status marker"));
            ErrorCode error = Optional.ofNullable(raw.get("error"))
                    .map(x -> ErrorCodes.get(x.getAsInt()))
                    .orElse(null);

            return new BINPurchaseMessage.Response(id, request, listing, actor, seller, successful, error);
        }

        private final UUID request;
        private final UUID seller;
        private final boolean successful;
        private long time;

        private final ErrorCode error;

        public Response(UUID id, UUID request, UUID listing, UUID actor, UUID seller, boolean successful, ErrorCode error) {
            super(id, listing, actor);
            this.request = request;
            this.seller = seller;
            this.successful = successful;
            this.error = error;
        }

        @Override
        public @NonNull String asEncodedString() {
            return GTSMessagingService.encodeMessageAsString(
                    TYPE,
                    this.getID(),
                    new JObject()
                            .add("request", this.request.toString())
                            .add("listing", this.listing.toString())
                            .add("actor", this.actor.toString())
                            .add("seller", this.seller.toString())
                            .add("successful", this.successful)
                            .consume(o -> this.getErrorCode().ifPresent(e -> o.add("error", e.ordinal())))
                            .toJson()
            );
        }

        @Override
        public UUID getRequestID() {
            return this.request;
        }

        @Override
        public long getResponseTime() {
            return this.time;
        }

        @Override
        public void setResponseTime(long millis) {
            this.time = millis;
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
                    .kv("Listing ID", this.getListingID())
                    .kv("Actor", this.getActor())
                    .kv("Seller", this.getSeller());
        }

        @Override
        public UUID getSeller() {
            return this.seller;
        }
    }

}
