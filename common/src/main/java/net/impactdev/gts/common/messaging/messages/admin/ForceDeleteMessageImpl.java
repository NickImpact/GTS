package net.impactdev.gts.common.messaging.messages.admin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.api.communication.message.errors.ErrorCode;
import net.impactdev.gts.api.communication.message.errors.ErrorCodes;
import net.impactdev.gts.api.communication.message.type.admin.ForceDeleteMessage;
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

public abstract class ForceDeleteMessageImpl extends AbstractMessage implements ForceDeleteMessage {

    protected final UUID listing;
    protected final UUID actor;
    protected final boolean give;

    public ForceDeleteMessageImpl(UUID id, UUID listing, UUID actor, boolean give) {
        super(id);
        this.listing = listing;
        this.actor = actor;
        this.give = give;
    }

    @Override
    public @NonNull UUID getListingID() {
        return this.listing;
    }

    @Override
    public @NonNull UUID getActor() {
        return this.actor;
    }

    @Override
    public boolean shouldGive() {
        return this.give;
    }

    public static class ForceDeleteRequest extends ForceDeleteMessageImpl implements Request {

        public static final String TYPE = "Admin/Delete/Request";

        public static ForceDeleteRequest decode(@Nullable JsonElement content, UUID id) {
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
            boolean give = Optional.ofNullable(raw.get("give"))
                    .map(JsonElement::getAsBoolean)
                    .orElseThrow(() -> new IllegalStateException("Failed to locate give parameter"));

            return new ForceDeleteRequest(id, listing, actor, give);
        }

        public ForceDeleteRequest(UUID id, UUID listing, UUID actor, boolean give) {
            super(id, listing, actor, give);
        }

        @Override
        public CompletableFuture<ForceDeleteMessage.Response> respond() {
            return GTSPlugin.instance().storage().processForcedDeletion(this);
        }

        @Override
        public @NonNull String asEncodedString() {
            return GTSMessagingService.encodeMessageAsString(
                    TYPE,
                    this.getID(),
                    new JObject()
                            .add("listing", this.listing.toString())
                            .add("actor", this.actor.toString())
                            .add("give", this.give)
                            .toJson()
            );
        }

        @Override
        public void print(PrettyPrinter printer) {
            printer.kv("Message ID", this.getID())
                    .kv("Listing ID", this.listing)
                    .kv("Actor", this.actor);
        }
    }

    public static class ForceDeleteResponse extends ForceDeleteMessageImpl implements Response {

        public static final String TYPE = "Admin/Delete/Response";

        public static ForceDeleteResponse decode(@Nullable JsonElement content, UUID id) {
            if(content == null) {
                throw new IllegalStateException("Raw JSON data was null");
            }

            JsonObject raw = content.getAsJsonObject();

            UUID request = Optional.ofNullable(raw.get("request"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElseThrow(() -> new IllegalStateException("Unable to locate or parse request ID"));
            UUID listing = Optional.ofNullable(raw.get("listing"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElseThrow(() -> new IllegalStateException("Unable to locate listing ID"));
            UUID actor = Optional.ofNullable(raw.get("actor"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElseThrow(() -> new IllegalStateException("Unable to locate actor ID"));
            Listing data = Optional.ofNullable(raw.get("data"))
                    .map(x -> {
                        JsonObject json = (JsonObject) x;
                        String type = json.get("type").getAsString();
                        if(type.equals("bin")) {
                            return GTSService.getInstance().getGTSComponentManager()
                                    .getListingResourceManager(BuyItNow.class)
                                    .get()
                                    .getDeserializer()
                                    .deserialize(json);
                        } else {
                            return GTSService.getInstance().getGTSComponentManager()
                                    .getListingResourceManager(Auction.class)
                                    .get()
                                    .getDeserializer()
                                    .deserialize(json);
                        }
                    })
                    .orElseThrow(() -> new IllegalStateException("Unable to locate or parse listing data"));
            boolean give = Optional.ofNullable(raw.get("give"))
                    .map(JsonElement::getAsBoolean)
                    .orElseThrow(() -> new IllegalStateException("Failed to locate give parameter"));
            boolean successful = Optional.ofNullable(raw.get("successful"))
                    .map(JsonElement::getAsBoolean)
                    .orElseThrow(() -> new IllegalStateException("Failed to locate success parameter"));
            ErrorCode error = Optional.ofNullable(raw.get("error"))
                    .map(x -> ErrorCodes.get(x.getAsInt()))
                    .orElse(null);

            return new ForceDeleteResponse(id, request, listing, actor, data, give, successful, error);
        }

        private final UUID request;

        private final Listing data;

        private final boolean successful;
        private final ErrorCode error;

        private long responseTime;

        public ForceDeleteResponse(UUID id, UUID request, UUID listing, UUID actor, Listing data, boolean give, boolean successful, @Nullable ErrorCode error) {
            super(id, listing, actor, give);
            this.request = request;
            this.data = data;
            this.successful = successful;
            this.error = error;
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
                            .add("request", this.getRequestID().toString())
                            .add("listing", this.listing.toString())
                            .add("actor", this.actor.toString())
                            .add("data", this.data.serialize())
                            .add("give", this.give)
                            .add("successful", this.successful)
                            .consume(o -> this.getErrorCode().ifPresent(e -> o.add("error", e.ordinal())))
                            .toJson()
            );
        }

        @Override
        public void print(PrettyPrinter printer) {
            printer.kv("Message ID", this.getID())
                    .kv("Request ID", this.getRequestID())
                    .kv("Listing ID", this.listing)
                    .kv("Actor", this.actor)
                    .kv("Should Return", this.give);
        }

        @Override
        public Optional<Listing> getDeletedListing() {
            return Optional.of(this.data);
        }

        public static class ForcedDeleteResponseBuilder implements ForceDeleteMessage.Response.ResponseBuilder {

            private UUID request;
            private UUID listing;
            private UUID actor;
            private Listing data;
            private boolean give;
            private boolean successful;
            private ErrorCode error;

            @Override
            public ResponseBuilder request(UUID request) {
                this.request = request;
                return this;
            }

            @Override
            public ResponseBuilder listing(UUID listing) {
                this.listing = listing;
                return this;
            }

            @Override
            public ResponseBuilder actor(UUID actor) {
                this.actor = actor;
                return this;
            }

            @Override
            public ResponseBuilder data(Listing data) {
                this.data = data;
                return this;
            }

            @Override
            public ResponseBuilder give(boolean give) {
                this.give = give;
                return this;
            }

            @Override
            public ResponseBuilder successful(boolean successful) {
                this.successful = successful;
                return this;
            }

            @Override
            public ResponseBuilder error(ErrorCode error) {
                this.error = error;
                return this;
            }

            @Override
            public ForceDeleteMessage.Response build() {
                return new ForceDeleteResponse(
                        GTSPlugin.instance().messagingService().generatePingID(),
                        this.request,
                        this.listing,
                        this.actor,
                        this.data,
                        this.give,
                        this.successful,
                        this.error
                );
            }
        }
    }
}
