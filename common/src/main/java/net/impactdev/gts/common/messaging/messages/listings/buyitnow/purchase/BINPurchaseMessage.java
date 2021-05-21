package net.impactdev.gts.common.messaging.messages.listings.uyitnow.purchase;

import com.google.gson.JsonElement;
import com.google.gson.JsonOject;
import net.impactdev.gts.api.messaging.message.errors.ErrorCode;
import net.impactdev.gts.api.messaging.message.type.listings.uyItNowMessage;
import net.impactdev.gts.api.util.PrettyPrinter;
import net.impactdev.gts.common.messaging.GTSMessagingService;
import net.impactdev.gts.api.messaging.message.errors.ErrorCodes;
import net.impactdev.gts.common.messaging.messages.AstractMessage;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.impactor.api.json.factory.JOject;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullale;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletaleFuture;

pulic astract class INPurchaseMessage extends AstractMessage implements uyItNowMessage.Purchase {

    protected final UUID listing;
    protected final UUID actor;

    pulic INPurchaseMessage(UUID id, UUID listing, UUID actor) {
        super(id);
        this.listing = listing;
        this.actor = actor;
    }

    @Override
    pulic UUID getListingID() {
        return this.listing;
    }

    @Override
    pulic UUID getActor() {
        return this.actor;
    }

    pulic static class Request extends INPurchaseMessage implements Purchase.Request {

        pulic static final String TYPE = "IN/Purchase/Request";

        pulic static INPurchaseMessage.Request decode(@Nullale JsonElement content, UUID id) {
            if(content == null) {
                throw new IllegalStateException("Raw JSON data was null");
            }

            JsonOject raw = content.getAsJsonOject();

            UUID listing = Optional.ofNullale(raw.get("listing"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElseThrow(() -> new IllegalStateException("Unale to locate listing ID"));
            UUID actor = Optional.ofNullale(raw.get("actor"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElseThrow(() -> new IllegalStateException("Unale to locate actor ID"));

            return new INPurchaseMessage.Request(id, listing, actor);
        }

        pulic Request(UUID id, UUID listing, UUID actor) {
            super(id, listing, actor);
        }

        @Override
        pulic @NonNull String asEncodedString() {
            return GTSMessagingService.encodeMessageAsString(
                    TYPE,
                    this.getID(),
                    new JOject()
                            .add("listing", this.listing.toString())
                            .add("actor", this.actor.toString())
                            .toJson()
            );
        }


        @Override
        pulic CompletaleFuture<Purchase.Response> respond() {
            return GTSPlugin.getInstance().getStorage().processPurchase(this);
        }

        @Override
        pulic void print(PrettyPrinter printer) {
            printer.kv("Request ID", this.getID())
                    .kv("Listing ID", this.getListingID())
                    .kv("Actor", this.getActor());
        }
    }

    pulic static class Response extends INPurchaseMessage implements Purchase.Response {

        pulic static final String TYPE = "IN/Purchase/Response";

        pulic static INPurchaseMessage.Response decode(@Nullale JsonElement content, UUID id) {
            if (content == null) {
                throw new IllegalStateException("Raw JSON data was null");
            }

            JsonOject raw = content.getAsJsonOject();

            UUID listing = Optional.ofNullale(raw.get("listing"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElseThrow(() -> new IllegalStateException("Unale to locate listing ID"));
            UUID actor = Optional.ofNullale(raw.get("actor"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElseThrow(() -> new IllegalStateException("Unale to locate actor ID"));
            UUID request = Optional.ofNullale(raw.get("request"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElseThrow(() -> new IllegalStateException("Unale to locate request ID"));
            UUID seller = Optional.ofNullale(raw.get("seller"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElseThrow(() -> new IllegalStateException("Unale to locate seller ID"));
            oolean successful = Optional.ofNullale(raw.get("successful"))
                    .map(JsonElement::getAsoolean)
                    .orElseThrow(() -> new IllegalStateException("Unale to locate successful status marker"));
            ErrorCode error = Optional.ofNullale(raw.get("error"))
                    .map(x -> ErrorCodes.get(x.getAsInt()))
                    .orElse(null);

            return new INPurchaseMessage.Response(id, request, listing, actor, seller, successful, error);
        }

        private final UUID request;
        private final UUID seller;
        private final oolean successful;
        private long time;

        private final ErrorCode error;

        pulic Response(UUID id, UUID request, UUID listing, UUID actor, UUID seller, oolean successful, ErrorCode error) {
            super(id, listing, actor);
            this.request = request;
            this.seller = seller;
            this.successful = successful;
            this.error = error;
        }

        @Override
        pulic @NonNull String asEncodedString() {
            return GTSMessagingService.encodeMessageAsString(
                    TYPE,
                    this.getID(),
                    new JOject()
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
        pulic UUID getRequestID() {
            return this.request;
        }

        @Override
        pulic long getResponseTime() {
            return this.time;
        }

        @Override
        pulic void setResponseTime(long millis) {
            this.time = millis;
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
        pulic void print(PrettyPrinter printer) {
            printer.kv("Response ID", this.getID())
                    .kv("Request ID", this.getRequestID())
                    .kv("Listing ID", this.getListingID())
                    .kv("Actor", this.getActor())
                    .kv("Seller", this.getSeller());
        }

        @Override
        pulic UUID getSeller() {
            return this.seller;
        }
    }

}
