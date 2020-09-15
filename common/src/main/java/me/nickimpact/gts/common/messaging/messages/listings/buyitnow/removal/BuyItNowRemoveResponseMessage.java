package me.nickimpact.gts.common.messaging.messages.listings.buyitnow.removal;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nickimpact.impactor.api.json.factory.JObject;
import com.nickimpact.impactor.api.utilities.Builder;
import me.nickimpact.gts.api.messaging.message.errors.ErrorCode;
import me.nickimpact.gts.api.messaging.message.type.listings.BuyItNowMessage;
import me.nickimpact.gts.common.messaging.GTSMessagingService;
import me.nickimpact.gts.common.messaging.messages.AbstractMessage;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;
import java.util.UUID;

public class BuyItNowRemoveResponseMessage extends AbstractMessage.Response implements BuyItNowMessage.Remove.Response {

    public static final String TYPE = "BIN - Remove Response";

    public static BuyItNowRemoveResponseMessage decode(@Nullable JsonElement content, UUID id) {
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

        UUID receiver = Optional.ofNullable(raw.get("receiver"))
                .map(x -> UUID.fromString(x.getAsString()))
                .orElse(null);

        return BuyItNowRemoveResponseMessage.builder()
                .id(id)
                .request(request)
                .listing(listing)
                .actor(actor)
                .receiver(receiver)
                .shouldReceive(shouldReceive)
                .successful(successful)
                .build();
    }

    private final UUID listing;
    private final UUID actor;
    private final UUID receiver;
    private final boolean shouldReceive;

    private final boolean successful;
    private final ErrorCode error;

    private BuyItNowRemoveResponseMessage(ResponseBuilder builder) {
        super(builder.id, builder.request);
        this.listing = builder.listing;
        this.actor = builder.actor;
        this.receiver = builder.receiver;
        this.shouldReceive = builder.shouldReceive;
        this.successful = builder.successful;
        this.error = builder.error;
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
    public UUID getListingID() {
        return this.listing;
    }

    @Override
    public UUID getActor() {
        return this.actor;
    }

    @Override
    public Optional<UUID> getRecipient() {
        return Optional.ofNullable(this.receiver);
    }

    @Override
    public boolean shouldReturnListing() {
        return this.shouldReceive;
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
                            if(this.receiver != null) {
                                o.add("receiver", this.receiver.toString());
                            }
                        })
                        .add("shouldReceive", this.shouldReceive)
                        .add("successful", this.wasSuccessful())
                        .consume(o -> {
                            if(this.error != null) {
                                o.add("error", this.error.getID());
                            }
                        })
                        .toJson()
        );
    }

    public static ResponseBuilder builder() {
        return new ResponseBuilder();
    }

    public static class ResponseBuilder implements Builder<BuyItNowRemoveResponseMessage, ResponseBuilder> {

        private UUID id;
        private UUID request;

        private UUID listing;
        private UUID actor;
        private UUID receiver;
        private boolean shouldReceive;

        private boolean successful;
        private ErrorCode error;

        public ResponseBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public ResponseBuilder request(UUID request) {
            this.request = request;
            return this;
        }

        public ResponseBuilder listing(UUID listing) {
            this.listing = listing;
            return this;
        }

        public ResponseBuilder actor(UUID actor) {
            this.actor = actor;
            return this;
        }

        public ResponseBuilder receiver(UUID receiver) {
            this.receiver = receiver;
            return this;
        }

        public ResponseBuilder shouldReceive(boolean shouldReceive) {
            this.shouldReceive = shouldReceive;
            return this;
        }

        public ResponseBuilder successful(boolean successful) {
            this.successful = successful;
            return this;
        }

        public ResponseBuilder error(ErrorCode error) {
            this.error = error;
            return this;
        }

        @Override
        public ResponseBuilder from(BuyItNowRemoveResponseMessage buyItNowRemoveResponseMessage) {
            return null;
        }

        @Override
        public BuyItNowRemoveResponseMessage build() {
            return new BuyItNowRemoveResponseMessage(this);
        }
    }
}
