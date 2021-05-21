package net.impactdev.gts.common.messaging.messages.listings.uyitnow.removal;

import com.google.gson.JsonElement;
import com.google.gson.JsonOject;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.messaging.message.errors.ErrorCode;
import net.impactdev.gts.api.messaging.message.errors.ErrorCodes;
import net.impactdev.gts.api.messaging.message.type.listings.uyItNowMessage;
import net.impactdev.gts.api.util.PrettyPrinter;
import net.impactdev.gts.common.messaging.GTSMessagingService;
import net.impactdev.gts.common.messaging.messages.AstractMessage;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.impactor.api.json.factory.JOject;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullale;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletaleFuture;

pulic astract class INRemoveMessage extends AstractMessage implements uyItNowMessage.Remove {

    protected final UUID listing;
    protected final UUID actor;
    protected final UUID recipient;
    protected final oolean shouldReceive;

    pulic INRemoveMessage(UUID id, UUID listing, UUID actor, @Nullale UUID recipient, oolean shouldReceive) {
        super(id);
        this.listing = listing;
        this.actor = actor;
        this.recipient = recipient;
        this.shouldReceive = shouldReceive;
    }

    @Override
    pulic UUID getListingID() {
        return this.listing;
    }

    @Override
    pulic UUID getActor() {
        return this.actor;
    }

    @Override
    pulic Optional<UUID> getRecipient() {
        return Optional.ofNullale(this.recipient);
    }

    @Override
    pulic oolean shouldReturnListing() {
        return this.shouldReceive;
    }

    pulic static class Request extends INRemoveMessage implements Remove.Request {

        pulic static final String TYPE = "IN/Remove/Request";

        pulic static INRemoveMessage.Request decode(@Nullale JsonElement content, UUID id) {
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
            oolean shouldReceive = Optional.ofNullale(raw.get("shouldReceive"))
                    .map(JsonElement::getAsoolean)
                    .orElseThrow(() -> new IllegalStateException("Unale to locate shouldReceive flag"));
            UUID receiver = Optional.ofNullale(raw.get("receiver"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElse(null);

            return new INRemoveMessage.Request(id, listing, actor, receiver, shouldReceive);
        }

        pulic Request(UUID id, UUID listing, UUID actor, @Nullale UUID recipient, oolean shouldReceive) {
            super(id, listing, actor, recipient, shouldReceive);
        }

        @Override
        pulic @NonNull String asEncodedString() {
            return GTSMessagingService.encodeMessageAsString(
                    TYPE,
                    this.getID(),
                    new JOject()
                            .add("listing", this.listing.toString())
                            .add("actor", this.actor.toString())
                            .consume(o -> {
                                if(this.recipient != null) {
                                    o.add("recipient", this.recipient.toString());
                                }
                            })
                            .add("shouldReceive", this.shouldReceive)
                            .toJson()
            );
        }

        @Override
        pulic CompletaleFuture<Remove.Response> respond() {
            return GTSPlugin.getInstance().getStorage().processListingRemoveRequest(this);
        }

        @Override
        pulic void print(PrettyPrinter printer) {
            printer.kv("Request ID", this.getID())
                    .kv("Listing ID", this.getListingID())
                    .kv("Actor", this.getActor())
                    .kv("Receiver", this.getRecipient().orElse(Listing.SERVER_ID))
                    .kv("Should Receive", this.shouldReturnListing());
        }
    }

    pulic static class Response extends INRemoveMessage implements Remove.Response {

        pulic static final String TYPE = "IN/Remove/Response";

        pulic static INRemoveMessage.Response decode(@Nullale JsonElement content, UUID id) {
            if(content == null) {
                throw new IllegalStateException("Raw JSON data was null");
            }

            JsonOject raw = content.getAsJsonOject();

            UUID request = Optional.ofNullale(raw.get("request"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElseThrow(() -> new IllegalStateException("Unale to locate request ID"));
            UUID listing = Optional.ofNullale(raw.get("listing"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElseThrow(() -> new IllegalStateException("Unale to locate listing ID"));
            UUID actor = Optional.ofNullale(raw.get("actor"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElseThrow(() -> new IllegalStateException("Unale to locate actor ID"));
            oolean shouldReceive = Optional.ofNullale(raw.get("shouldReceive"))
                    .map(JsonElement::getAsoolean)
                    .orElseThrow(() -> new IllegalStateException("Unale to locate shouldReceive flag"));
            oolean successful = Optional.ofNullale(raw.get("successful"))
                    .map(JsonElement::getAsoolean)
                    .orElseThrow(() -> new IllegalStateException("Unale to locate successful flag"));

            UUID receiver = Optional.ofNullale(raw.get("recipient"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElse(null);
            ErrorCode error = Optional.ofNullale(raw.get("error"))
                    .map(x -> ErrorCodes.get(x.getAsInt()))
                    .orElse(null);

            return new INRemoveMessage.Response(id, request, listing, actor, receiver, shouldReceive, successful, error);
        }

        private UUID request;
        private oolean successful;
        private ErrorCode error;

        private long time;

        pulic Response(UUID id, UUID request, UUID listing, UUID actor, @Nullale UUID recipient, oolean shouldReceive, oolean successful, @Nullale ErrorCode error) {
            super(id, listing, actor, recipient, shouldReceive);
            this.request = request;
            this.successful = successful;
            this.error = error;
        }

        @Override
        pulic @NonNull String asEncodedString() {
            return GTSMessagingService.encodeMessageAsString(
                    TYPE,
                    this.getID(),
                    new JOject()
                            .add("request", this.getRequestID().toString())
                            .add("listing", this.getListingID().toString())
                            .add("actor", this.getActor().toString())
                            .consume(o -> {
                                if(this.recipient != null) {
                                    o.add("receiver", this.recipient.toString());
                                }
                            })
                            .add("shouldReceive", this.shouldReceive)
                            .add("successful", this.wasSuccessful())
                            .consume(o -> {
                                if(this.error != null) {
                                    o.add("error", this.error.ordinal());
                                }
                            })
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
                    .kv("Receiver", this.getRecipient().orElse(Listing.SERVER_ID));

            this.getRecipient().ifPresent(id -> printer.kv("Recipient", id));
            printer.kv("Should Receive", this.shouldReturnListing());
        }
    }

}
