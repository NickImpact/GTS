package net.impactdev.gts.common.messaging.messages.deliveries;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.deliveries.Delivery;
import net.impactdev.gts.api.messaging.message.errors.ErrorCode;
import net.impactdev.gts.api.messaging.message.errors.ErrorCodes;
import net.impactdev.gts.api.messaging.message.type.deliveries.ClaimDelivery;
import net.impactdev.gts.api.util.PrettyPrinter;
import net.impactdev.gts.common.messaging.GTSMessagingService;
import net.impactdev.gts.common.messaging.messages.AbstractMessage;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.impactor.api.json.factory.JObject;
import net.impactdev.impactor.api.utilities.Builder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class ClaimDeliveryImpl extends AbstractMessage implements ClaimDelivery {

    private final UUID delivery;
    private final UUID actor;

    public ClaimDeliveryImpl(UUID id, UUID delivery, UUID actor) {
        super(id);
        this.delivery = delivery;
        this.actor = actor;
    }

    @Override
    public @NonNull UUID getDeliveryID() {
        return this.delivery;
    }

    @Override
    public @NonNull UUID getActor() {
        return this.actor;
    }

    @Override
    public void print(PrettyPrinter printer) {
        printer.kv("delivery", this.delivery.toString())
                .kv("actor", this.delivery.toString());
    }

    public static class ClaimDeliveryRequestImpl extends ClaimDeliveryImpl implements ClaimDelivery.Request {

        public static final String TYPE = "Delivery/Claim/Request";

        public static ClaimDeliveryRequestImpl decode(@Nullable JsonElement content, UUID id) {
            if (content == null) {
                throw new IllegalStateException("Raw JSON data was null");
            }

            JsonObject raw = content.getAsJsonObject();

            UUID delivery = Optional.ofNullable(raw.get("delivery"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElseThrow(() -> new IllegalStateException("Unable to locate delivery ID"));
            UUID actor = Optional.ofNullable(raw.get("actor"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElseThrow(() -> new IllegalStateException("Unable to locate actor ID"));

            return new ClaimDeliveryRequestImpl(id, delivery, actor);
        }

        public ClaimDeliveryRequestImpl(UUID id, UUID delivery, UUID actor) {
            super(id, delivery, actor);
        }

        @Override
        public CompletableFuture<ClaimDelivery.Response> respond() {
            return GTSPlugin.getInstance().getStorage().claimDelivery(this);
        }

        @Override
        public @NonNull String asEncodedString() {
            return GTSMessagingService.encodeMessageAsString(
                    TYPE,
                    this.getID(),
                    new JObject()
                            .add("delivery", this.getDeliveryID().toString())
                            .add("actor", this.getActor().toString())
                            .toJson()
            );
        }
    }

    public static class ClaimDeliveryResponseImpl extends ClaimDeliveryImpl implements ClaimDelivery.Response {

        public static final String TYPE = "Delivery/Claim/Response";

        public static ClaimDeliveryResponseImpl decode(@Nullable JsonElement content, UUID id) {
            if (content == null) {
                throw new IllegalStateException("Raw JSON data was null");
            }

            JsonObject raw = content.getAsJsonObject();

            UUID request = Optional.ofNullable(raw.get("request"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElseThrow(() -> new IllegalStateException("Unable to locate or parse request ID"));
            UUID delivery = Optional.ofNullable(raw.get("delivery"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElseThrow(() -> new IllegalStateException("Unable to locate delivery ID"));
            UUID actor = Optional.ofNullable(raw.get("actor"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElseThrow(() -> new IllegalStateException("Unable to locate actor ID"));
            boolean successful = Optional.ofNullable(raw.get("successful"))
                    .map(JsonElement::getAsBoolean)
                    .orElseThrow(() -> new IllegalStateException("Failed to locate successful status"));
            ErrorCode error = Optional.ofNullable(raw.get("error"))
                    .map(x -> ErrorCodes.get(x.getAsInt()))
                    .orElse(null);

            DeliveryClaimResponseBuilder builder = builder()
                    .id(id)
                    .request(request)
                    .delivery(delivery)
                    .actor(actor)
                    .error(error);

            if(successful) {
                return builder.successful().build();
            }

            return builder.build();
        }

        private final UUID request;
        private final ErrorCode error;
        private final boolean successful;

        private long time;

        public ClaimDeliveryResponseImpl(DeliveryClaimResponseBuilder builder) {
            super(builder.id, builder.delivery, builder.actor);
            this.request = builder.request;
            this.error = builder.error;
            this.successful = builder.successful;
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
        public @NonNull String asEncodedString() {
            return GTSMessagingService.encodeMessageAsString(
                    TYPE,
                    this.getID(),
                    new JObject()
                            .add("request", this.request.toString())
                            .add("delivery", this.getDeliveryID().toString())
                            .add("actor", this.getActor().toString())
                            .add("successful", this.successful)
                            .consume(o -> {
                                this.getErrorCode().ifPresent(e -> o.add("error", this.error.ordinal()));
                            })
                            .toJson()
            );
        }

        public static DeliveryClaimResponseBuilder builder() {
            return new DeliveryClaimResponseBuilder();
        }



        public static class DeliveryClaimResponseBuilder implements Builder<ClaimDeliveryResponseImpl, DeliveryClaimResponseBuilder> {

            protected UUID id;
            protected UUID delivery;
            protected UUID actor;

            protected UUID request;
            protected boolean successful;
            protected ErrorCode error;

            public DeliveryClaimResponseBuilder id(UUID id) {
                this.id = id;
                return this;
            }

            public DeliveryClaimResponseBuilder delivery(UUID delivery) {
                this.delivery = delivery;
                return this;
            }

            public DeliveryClaimResponseBuilder actor(UUID actor) {
                this.actor = actor;
                return this;
            }

            public DeliveryClaimResponseBuilder request(UUID request) {
                this.request = request;
                return this;
            }

            public DeliveryClaimResponseBuilder successful() {
                this.successful = true;
                return this;
            }

            public DeliveryClaimResponseBuilder error(ErrorCode error) {
                this.error = error;
                return this;
            }

            @Override
            public DeliveryClaimResponseBuilder from(ClaimDeliveryResponseImpl claimDeliveryResponse) {
                return null;
            }

            @Override
            public ClaimDeliveryResponseImpl build() {
                return new ClaimDeliveryResponseImpl(this);
            }
        }
    }

}
