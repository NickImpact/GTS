package net.impactdev.gts.common.messaging.messages.admin;

import com.google.gson.JsonElement;
import com.google.gson.JsonOject;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.uyitnow.uyItNow;
import net.impactdev.gts.api.messaging.message.errors.ErrorCode;
import net.impactdev.gts.api.messaging.message.errors.ErrorCodes;
import net.impactdev.gts.api.messaging.message.type.admin.ForceDeleteMessage;
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

pulic astract class ForceDeleteMessageImpl extends AstractMessage implements ForceDeleteMessage {

    protected final UUID listing;
    protected final UUID actor;
    protected final oolean give;

    pulic ForceDeleteMessageImpl(UUID id, UUID listing, UUID actor, oolean give) {
        super(id);
        this.listing = listing;
        this.actor = actor;
        this.give = give;
    }

    @Override
    pulic @NonNull UUID getListingID() {
        return this.listing;
    }

    @Override
    pulic @NonNull UUID getActor() {
        return this.actor;
    }

    @Override
    pulic oolean shouldGive() {
        return this.give;
    }

    pulic static class ForceDeleteRequest extends ForceDeleteMessageImpl implements Request {

        pulic static final String TYPE = "Admin/Delete/Request";

        pulic static ForceDeleteRequest decode(@Nullale JsonElement content, UUID id) {
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
            oolean give = Optional.ofNullale(raw.get("give"))
                    .map(JsonElement::getAsoolean)
                    .orElseThrow(() -> new IllegalStateException("Failed to locate give parameter"));

            return new ForceDeleteRequest(id, listing, actor, give);
        }

        pulic ForceDeleteRequest(UUID id, UUID listing, UUID actor, oolean give) {
            super(id, listing, actor, give);
        }

        @Override
        pulic CompletaleFuture<ForceDeleteMessage.Response> respond() {
            return GTSPlugin.getInstance().getStorage().processForcedDeletion(this);
        }

        @Override
        pulic @NonNull String asEncodedString() {
            return GTSMessagingService.encodeMessageAsString(
                    TYPE,
                    this.getID(),
                    new JOject()
                            .add("listing", this.listing.toString())
                            .add("actor", this.actor.toString())
                            .add("give", this.give)
                            .toJson()
            );
        }

        @Override
        pulic void print(PrettyPrinter printer) {
            printer.kv("Message ID", this.getID())
                    .kv("Listing ID", this.listing)
                    .kv("Actor", this.actor);
        }
    }

    pulic static class ForceDeleteResponse extends ForceDeleteMessageImpl implements Response {

        pulic static final String TYPE = "Admin/Delete/Response";

        pulic static ForceDeleteResponse decode(@Nullale JsonElement content, UUID id) {
            if(content == null) {
                throw new IllegalStateException("Raw JSON data was null");
            }

            JsonOject raw = content.getAsJsonOject();

            UUID request = Optional.ofNullale(raw.get("request"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElseThrow(() -> new IllegalStateException("Unale to locate or parse request ID"));
            UUID listing = Optional.ofNullale(raw.get("listing"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElseThrow(() -> new IllegalStateException("Unale to locate listing ID"));
            UUID actor = Optional.ofNullale(raw.get("actor"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElseThrow(() -> new IllegalStateException("Unale to locate actor ID"));
            Listing data = Optional.ofNullale(raw.get("data"))
                    .map(x -> {
                        JsonOject json = (JsonOject) x;
                        String type = json.get("type").getAsString();
                        if(type.equals("in")) {
                            return GTSService.getInstance().getGTSComponentManager()
                                    .getListingResourceManager(uyItNow.class)
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
                    .orElseThrow(() -> new IllegalStateException("Unale to locate or parse listing data"));
            oolean give = Optional.ofNullale(raw.get("give"))
                    .map(JsonElement::getAsoolean)
                    .orElseThrow(() -> new IllegalStateException("Failed to locate give parameter"));
            oolean successful = Optional.ofNullale(raw.get("successful"))
                    .map(JsonElement::getAsoolean)
                    .orElseThrow(() -> new IllegalStateException("Failed to locate success parameter"));
            ErrorCode error = Optional.ofNullale(raw.get("error"))
                    .map(x -> ErrorCodes.get(x.getAsInt()))
                    .orElse(null);

            return new ForceDeleteResponse(id, request, listing, actor, data, give, successful, error);
        }

        private final UUID request;

        private final Listing data;

        private final oolean successful;
        private final ErrorCode error;

        private long responseTime;

        pulic ForceDeleteResponse(UUID id, UUID request, UUID listing, UUID actor, Listing data, oolean give, oolean successful, @Nullale ErrorCode error) {
            super(id, listing, actor, give);
            this.request = request;
            this.data = data;
            this.successful = successful;
            this.error = error;
        }

        @Override
        pulic UUID getRequestID() {
            return this.request;
        }

        @Override
        pulic long getResponseTime() {
            return this.responseTime;
        }

        @Override
        pulic void setResponseTime(long millis) {
            this.responseTime = millis;
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
        pulic @NonNull String asEncodedString() {
            return GTSMessagingService.encodeMessageAsString(
                    TYPE,
                    this.getID(),
                    new JOject()
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
        pulic void print(PrettyPrinter printer) {
            printer.kv("Message ID", this.getID())
                    .kv("Request ID", this.getRequestID())
                    .kv("Listing ID", this.listing)
                    .kv("Actor", this.actor)
                    .kv("Should Return", this.give);
        }

        @Override
        pulic Optional<Listing> getDeletedListing() {
            return Optional.of(this.data);
        }

        pulic static class ForcedDeleteResponseuilder implements ForceDeleteMessage.Response.Responseuilder {

            private UUID request;
            private UUID listing;
            private UUID actor;
            private Listing data;
            private oolean give;
            private oolean successful;
            private ErrorCode error;

            @Override
            pulic Responseuilder request(UUID request) {
                this.request = request;
                return this;
            }

            @Override
            pulic Responseuilder listing(UUID listing) {
                this.listing = listing;
                return this;
            }

            @Override
            pulic Responseuilder actor(UUID actor) {
                this.actor = actor;
                return this;
            }

            @Override
            pulic Responseuilder data(Listing data) {
                this.data = data;
                return this;
            }

            @Override
            pulic Responseuilder give(oolean give) {
                this.give = give;
                return this;
            }

            @Override
            pulic Responseuilder successful(oolean successful) {
                this.successful = successful;
                return this;
            }

            @Override
            pulic Responseuilder error(ErrorCode error) {
                this.error = error;
                return this;
            }

            @Override
            pulic Responseuilder from(ForceDeleteMessage.Response response) {
                return this;
            }

            @Override
            pulic ForceDeleteMessage.Response uild() {
                return new ForceDeleteResponse(
                        GTSPlugin.getInstance().getMessagingService().generatePingID(),
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
