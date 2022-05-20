package net.impactdev.gts.common.messaging.messages.listings.buyitnow.removal;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.communication.message.errors.ErrorCode;
import net.impactdev.gts.api.communication.message.errors.ErrorCodes;
import net.impactdev.gts.api.communication.message.type.listings.BuyItNowMessage;
import net.impactdev.gts.common.messaging.GTSMessagingService;
import net.impactdev.gts.common.messaging.messages.AbstractMessage;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.impactor.api.json.factory.JObject;
import net.impactdev.impactor.api.utilities.printing.PrettyPrinter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class BINRemoveMessage extends AbstractMessage implements BuyItNowMessage.Remove {

    protected final UUID listing;
    protected final UUID actor;
    protected final UUID recipient;
    protected final boolean shouldReceive;

    public BINRemoveMessage(UUID id, UUID listing, UUID actor, @Nullable UUID recipient, boolean shouldReceive) {
        super(id);
        this.listing = listing;
        this.actor = actor;
        this.recipient = recipient;
        this.shouldReceive = shouldReceive;
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
    public Optional<UUID> getRecipient() {
        return Optional.ofNullable(this.recipient);
    }

    @Override
    public boolean shouldReturnListing() {
        return this.shouldReceive;
    }

    public static class Request extends BINRemoveMessage implements Remove.Request {

        public static final String TYPE = "BIN/Remove/Request";

        public static BINRemoveMessage.Request decode(@Nullable JsonElement content, UUID id) {
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
            boolean shouldReceive = Optional.ofNullable(raw.get("shouldReceive"))
                    .map(JsonElement::getAsBoolean)
                    .orElseThrow(() -> new IllegalStateException("Unable to locate shouldReceive flag"));
            UUID receiver = Optional.ofNullable(raw.get("receiver"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElse(null);

            return new BINRemoveMessage.Request(id, listing, actor, receiver, shouldReceive);
        }

        public Request(UUID id, UUID listing, UUID actor, @Nullable UUID recipient, boolean shouldReceive) {
            super(id, listing, actor, recipient, shouldReceive);
        }

        @Override
        public @NonNull String asEncodedString() {
            return GTSMessagingService.encodeMessageAsString(
                    TYPE,
                    this.getID(),
                    new JObject()
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
        public CompletableFuture<Remove.Response> respond() {
            return GTSPlugin.instance().storage().processListingRemoveRequest(this);
        }

        @Override
        public void print(PrettyPrinter printer) {
            printer.kv("Request ID", this.getID())
                    .kv("Listing ID", this.getListingID())
                    .kv("Actor", this.getActor())
                    .kv("Receiver", this.getRecipient().orElse(Listing.SERVER_ID))
                    .kv("Should Receive", this.shouldReturnListing());
        }
    }

    public static class Response extends BINRemoveMessage implements Remove.Response {

        public static final String TYPE = "BIN/Remove/Response";

        public static BINRemoveMessage.Response decode(@Nullable JsonElement content, UUID id) {
            if(content == null) {
                throw new IllegalStateException("Raw JSON data was null");
            }

            JsonObject raw = content.getAsJsonObject();

            UUID request = Optional.ofNullable(raw.get("request"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElseThrow(() -> new IllegalStateException("Unable to locate request ID"));
            UUID listing = Optional.ofNullable(raw.get("listing"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElseThrow(() -> new IllegalStateException("Unable to locate listing ID"));
            UUID actor = Optional.ofNullable(raw.get("actor"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElseThrow(() -> new IllegalStateException("Unable to locate actor ID"));
            boolean shouldReceive = Optional.ofNullable(raw.get("shouldReceive"))
                    .map(JsonElement::getAsBoolean)
                    .orElseThrow(() -> new IllegalStateException("Unable to locate shouldReceive flag"));
            boolean successful = Optional.ofNullable(raw.get("successful"))
                    .map(JsonElement::getAsBoolean)
                    .orElseThrow(() -> new IllegalStateException("Unable to locate successful flag"));

            UUID receiver = Optional.ofNullable(raw.get("recipient"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElse(null);
            ErrorCode error = Optional.ofNullable(raw.get("error"))
                    .map(x -> ErrorCodes.get(x.getAsInt()))
                    .orElse(null);

            return new BINRemoveMessage.Response(id, request, listing, actor, receiver, shouldReceive, successful, error);
        }

        private UUID request;
        private boolean successful;
        private ErrorCode error;

        private long time;

        public Response(UUID id, UUID request, UUID listing, UUID actor, @Nullable UUID recipient, boolean shouldReceive, boolean successful, @Nullable ErrorCode error) {
            super(id, listing, actor, recipient, shouldReceive);
            this.request = request;
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
                    .kv("Receiver", this.getRecipient().orElse(Listing.SERVER_ID));

            this.getRecipient().ifPresent(id -> printer.kv("Recipient", id));
            printer.kv("Should Receive", this.shouldReturnListing());
        }
    }

}
