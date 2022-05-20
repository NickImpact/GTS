package net.impactdev.gts.common.messaging.messages.listings;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.impactdev.gts.api.communication.message.errors.ErrorCode;
import net.impactdev.gts.api.communication.message.errors.ErrorCodes;
import net.impactdev.gts.api.communication.message.type.listings.ClaimMessage;
import net.impactdev.gts.api.util.TriState;
import net.impactdev.gts.common.messaging.GTSMessagingService;
import net.impactdev.gts.common.messaging.messages.AbstractMessage;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.impactor.api.json.factory.JArray;
import net.impactdev.impactor.api.json.factory.JObject;
import net.impactdev.impactor.api.builders.Builder;
import net.impactdev.impactor.api.utilities.printing.PrettyPrinter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class ClaimMessageImpl extends AbstractMessage implements ClaimMessage {

    protected final UUID listing;
    protected final UUID actor;
    protected final UUID receiver;
    protected final boolean auction;

    protected ClaimMessageImpl(UUID id, UUID listing, UUID actor, @Nullable UUID receiver, boolean auction) {
        super(id);
        this.listing = listing;
        this.actor = actor;
        this.receiver = receiver;
        this.auction = auction;
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
    public Optional<UUID> getReceiver() {
        return Optional.ofNullable(this.receiver);
    }

    @Override
    public boolean isAuction() {
        return this.auction;
    }

    @Override
    public void print(PrettyPrinter printer) {
        printer.kv("Message ID", this.getID())
                .kv("Listing ID", this.listing)
                .kv("Actor", this.actor)
                .consume(p -> this.getReceiver().ifPresent(r -> p.kv("Receiver", r)))
                .kv("Is Auction", this.auction);
    }

    public static class ClaimRequestImpl extends ClaimMessageImpl implements ClaimMessage.Request {

        public static final String TYPE = "Listing/Claim/Request";

        public static ClaimRequestImpl decode(@Nullable JsonElement content, UUID id) {
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
            UUID receiver = Optional.ofNullable(raw.get("receiver"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElse(null);
            boolean auction = Optional.ofNullable(raw.get("auction"))
                    .map(JsonElement::getAsBoolean)
                    .orElseThrow(() -> new IllegalStateException("Failed to locate auction check field"));
            return new ClaimRequestImpl(id, listing, actor, receiver, auction);
        }

        public ClaimRequestImpl(UUID id, UUID listing, UUID actor, @Nullable UUID receiver, boolean auction) {
            super(id, listing, actor, receiver, auction);
        }

        @Override
        public CompletableFuture<ClaimMessage.Response> respond() {
            // Divert to storage system to respond, there it should response with either the base
            // response type, or one specifically meant for auctions

            return GTSPlugin.instance().storage().processClaimRequest(this);
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
                                this.getReceiver().ifPresent(r -> o.add("receiver", r.toString()));
                            })
                            .add("auction", this.auction)
                            .toJson()
            );
        }
    }

    public static class ClaimResponseImpl extends ClaimMessageImpl implements ClaimMessage.Response {

        public static final String TYPE = "Listing/Claim/Response";

        public static ClaimMessage.Response decode(@Nullable JsonElement content, UUID id) {
            if (content == null) {
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
            UUID receiver = Optional.ofNullable(raw.get("receiver"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElse(null);
            boolean auction = Optional.ofNullable(raw.get("auction"))
                    .map(JsonElement::getAsBoolean)
                    .orElseThrow(() -> new IllegalStateException("Failed to locate auction check field"));

            boolean successful = Optional.ofNullable(raw.get("successful"))
                    .map(JsonElement::getAsBoolean)
                    .orElseThrow(() -> new IllegalStateException("Failed to locate successful status"));
            ErrorCode error = Optional.ofNullable(raw.get("error"))
                    .map(x -> ErrorCodes.get(x.getAsInt()))
                    .orElse(null);

            ClaimResponseBuilder builder = ClaimResponseImpl.builder()
                    .id(id)
                    .request(request)
                    .listing(listing)
                    .actor(actor)
                    .receiver(receiver)
                    .error(error);
            if(successful) {
                builder.successful();
            }

            if(auction) {
                AuctionClaimResponseImpl.AuctionClaimResponseBuilder auc = AuctionClaimResponseImpl.builder().from(builder.build());

                boolean lister = Optional.ofNullable(raw.get("lister"))
                        .map(JsonElement::getAsBoolean)
                        .orElseThrow(() -> new IllegalStateException("Failed to locate lister status"));
                boolean winner = Optional.ofNullable(raw.get("winner"))
                        .map(JsonElement::getAsBoolean)
                        .orElseThrow(() -> new IllegalStateException("Failed to locate winner status"));
                Map<UUID, Boolean> others = Optional.ofNullable(raw.get("others"))
                        .map(element -> {
                            Map<UUID, Boolean> result = Maps.newHashMap();
                            JsonArray array = element.getAsJsonArray();
                            for(JsonElement entry : array) {
                                entry.getAsJsonObject().entrySet().forEach(e -> {
                                    result.put(UUID.fromString(e.getKey()), e.getValue().getAsBoolean());
                                });
                            }

                            return result;
                        })
                        .orElseThrow(() -> new IllegalStateException("Failed to locate others status"));

                return auc.lister(lister).winner(winner).others(others).build();
            } else {
                return builder.build();
            }
        }

        protected final UUID request;

        protected final boolean successful;
        protected final ErrorCode error;

        protected long responseTime;

        public ClaimResponseImpl(ClaimResponseBuilder builder) {
            super(builder.id, builder.listing, builder.actor, builder.actor, builder instanceof AuctionClaimResponseImpl.AuctionClaimResponseBuilder);
            this.request = builder.request;
            this.successful = builder.successful;
            this.error = builder.error;
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
        public void print(PrettyPrinter printer) {
            super.print(printer);

            printer.newline().kv("Request ID", this.request);
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
                            .consume(o -> {
                                this.getReceiver().ifPresent(r -> o.add("receiver", r.toString()));
                            })
                            .add("auction", this.auction)
                            .add("successful", this.successful)
                            .consume(o -> this.getErrorCode().ifPresent(e -> o.add("error", e.ordinal())))
                            .toJson()
            );
        }

        public static final class AuctionClaimResponseImpl extends ClaimResponseImpl implements ClaimMessage.Response.AuctionResponse {

            private final boolean lister;
            private final boolean winner;

            private final Map<UUID, Boolean> others;

            public AuctionClaimResponseImpl(AuctionClaimResponseBuilder builder) {
                super(builder);
                this.lister = builder.lister;
                this.winner = builder.winner;
                this.others = builder.others;
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
            public TriState hasOtherBidderClaimed(UUID uuid) {
                return Optional.ofNullable(this.others.get(uuid))
                        .map(TriState::fromBoolean)
                        .orElse(TriState.UNDEFINED);
            }

            @Override
            public List<UUID> getAllOtherClaimers() {
                return Lists.newArrayList(this.others.keySet());
            }

            @Override
            public void print(PrettyPrinter printer) {
                super.print(printer);
                printer.newline();
                printer.add("Lister Claimed: " + this.lister);
                printer.add("Winner Claimed: " + this.winner);
                printer.add("Others:");
                for(UUID id : this.others.keySet()) {
                    printer.add("  - " + id.toString());

                }
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
                                .consume(o -> {
                                    this.getReceiver().ifPresent(r -> o.add("receiver", r.toString()));
                                })
                                .add("auction", this.auction)
                                .add("lister", this.lister)
                                .add("winner", this.winner)
                                .consume(o -> {
                                    JArray others = new JArray();
                                    this.others.forEach((user, state) -> {
                                        others.add(new JObject().add(user.toString(), state));
                                    });
                                    o.add("others", others);
                                })
                                .add("successful", this.successful)
                                .consume(o -> this.getErrorCode().ifPresent(e -> o.add("error", e.ordinal())))
                                .toJson()
                );
            }

            public static AuctionClaimResponseBuilder builder() {
                return new AuctionClaimResponseBuilder();
            }

            public static final class AuctionClaimResponseBuilder extends ClaimResponseBuilder {

                private boolean lister;
                private boolean winner;

                private Map<UUID, Boolean> others = Maps.newHashMap();

                public AuctionClaimResponseBuilder lister(boolean state) {
                    this.lister = state;
                    return this;
                }

                public AuctionClaimResponseBuilder winner(boolean state) {
                    this.winner = state;
                    return this;
                }

                public AuctionClaimResponseBuilder others(Map<UUID, Boolean> others) {
                    this.others = others;
                    return this;
                }

                public AuctionClaimResponseBuilder from(ClaimMessage.Response response) {
                    this.id = response.getID();
                    this.listing = response.getListingID();
                    this.actor = response.getActor();
                    this.receiver = response.getReceiver().orElse(null);

                    this.request = response.getRequestID();
                    this.successful = response.wasSuccessful();
                    this.error = response.getErrorCode().orElse(null);

                    return this;
                }

                @Override
                public AuctionClaimResponseImpl build() {
                    return new AuctionClaimResponseImpl(this);
                }

            }
        }

        public static ClaimResponseBuilder builder() {
            return new ClaimResponseBuilder();
        }

        public static class ClaimResponseBuilder implements Builder<ClaimMessage.Response> {

            protected UUID id;
            protected UUID listing;
            protected UUID actor;
            protected UUID receiver;

            protected UUID request;
            protected boolean successful;
            protected ErrorCode error;

            public ClaimResponseBuilder id(UUID id) {
                this.id = id;
                return this;
            }

            public ClaimResponseBuilder listing(UUID listing) {
                this.listing = listing;
                return this;
            }

            public ClaimResponseBuilder actor(UUID actor) {
                this.actor = actor;
                return this;
            }

            public ClaimResponseBuilder receiver(UUID receiver) {
                this.receiver = receiver;
                return this;
            }

            public AuctionClaimResponseImpl.AuctionClaimResponseBuilder auction() {
                return AuctionClaimResponseImpl.builder().from(this.build());
            }

            public ClaimResponseBuilder request(UUID request) {
                this.request = request;
                return this;
            }

            public ClaimResponseBuilder successful() {
                this.successful = true;
                return this;
            }

            public ClaimResponseBuilder error(ErrorCode error) {
                this.error = error;
                return this;
            }

            @Override
            public ClaimResponseImpl build() {
                return new ClaimResponseImpl(this);
            }
        }
    }

}
