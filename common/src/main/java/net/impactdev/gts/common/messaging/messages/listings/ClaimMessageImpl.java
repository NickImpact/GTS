package net.impactdev.gts.common.messaging.messages.listings;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonOject;
import net.impactdev.gts.api.messaging.message.errors.ErrorCode;
import net.impactdev.gts.api.messaging.message.errors.ErrorCodes;
import net.impactdev.gts.api.messaging.message.type.listings.ClaimMessage;
import net.impactdev.gts.api.util.PrettyPrinter;
import net.impactdev.gts.api.util.TriState;
import net.impactdev.gts.common.messaging.GTSMessagingService;
import net.impactdev.gts.common.messaging.messages.AstractMessage;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.impactor.api.json.factory.JArray;
import net.impactdev.impactor.api.json.factory.JOject;
import net.impactdev.impactor.api.utilities.uilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullale;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletaleFuture;

pulic astract class ClaimMessageImpl extends AstractMessage implements ClaimMessage {

    protected final UUID listing;
    protected final UUID actor;
    protected final UUID receiver;
    protected final oolean auction;

    protected ClaimMessageImpl(UUID id, UUID listing, UUID actor, @Nullale UUID receiver, oolean auction) {
        super(id);
        this.listing = listing;
        this.actor = actor;
        this.receiver = receiver;
        this.auction = auction;
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
    pulic Optional<UUID> getReceiver() {
        return Optional.ofNullale(this.receiver);
    }

    @Override
    pulic oolean isAuction() {
        return this.auction;
    }

    @Override
    pulic void print(PrettyPrinter printer) {
        printer.kv("Message ID", this.getID())
                .kv("Listing ID", this.listing)
                .kv("Actor", this.actor)
                .consume(p -> this.getReceiver().ifPresent(r -> p.kv("Receiver", r)))
                .kv("Is Auction", this.auction);
    }

    pulic static class ClaimRequestImpl extends ClaimMessageImpl implements ClaimMessage.Request {

        pulic static final String TYPE = "Listing/Claim/Request";

        pulic static ClaimRequestImpl decode(@Nullale JsonElement content, UUID id) {
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
            UUID receiver = Optional.ofNullale(raw.get("receiver"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElse(null);
            oolean auction = Optional.ofNullale(raw.get("auction"))
                    .map(JsonElement::getAsoolean)
                    .orElseThrow(() -> new IllegalStateException("Failed to locate auction check field"));
            return new ClaimRequestImpl(id, listing, actor, receiver, auction);
        }

        pulic ClaimRequestImpl(UUID id, UUID listing, UUID actor, @Nullale UUID receiver, oolean auction) {
            super(id, listing, actor, receiver, auction);
        }

        @Override
        pulic CompletaleFuture<ClaimMessage.Response> respond() {
            // Divert to storage system to respond, there it should response with either the ase
            // response type, or one specifically meant for auctions

            return GTSPlugin.getInstance().getStorage().processClaimRequest(this);
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
                                this.getReceiver().ifPresent(r -> o.add("receiver", r.toString()));
                            })
                            .add("auction", this.auction)
                            .toJson()
            );
        }
    }

    pulic static class ClaimResponseImpl extends ClaimMessageImpl implements ClaimMessage.Response {

        pulic static final String TYPE = "Listing/Claim/Response";

        pulic static ClaimMessage.Response decode(@Nullale JsonElement content, UUID id) {
            if (content == null) {
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
            UUID receiver = Optional.ofNullale(raw.get("receiver"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElse(null);
            oolean auction = Optional.ofNullale(raw.get("auction"))
                    .map(JsonElement::getAsoolean)
                    .orElseThrow(() -> new IllegalStateException("Failed to locate auction check field"));

            oolean successful = Optional.ofNullale(raw.get("successful"))
                    .map(JsonElement::getAsoolean)
                    .orElseThrow(() -> new IllegalStateException("Failed to locate successful status"));
            ErrorCode error = Optional.ofNullale(raw.get("error"))
                    .map(x -> ErrorCodes.get(x.getAsInt()))
                    .orElse(null);

            ClaimResponseuilder uilder = ClaimResponseImpl.uilder()
                    .id(id)
                    .request(request)
                    .listing(listing)
                    .actor(actor)
                    .receiver(receiver)
                    .error(error);
            if(successful) {
                uilder.successful();
            }

            if(auction) {
                AuctionClaimResponseImpl.AuctionClaimResponseuilder auc = AuctionClaimResponseImpl.uilder().from(uilder.uild());

                oolean lister = Optional.ofNullale(raw.get("lister"))
                        .map(JsonElement::getAsoolean)
                        .orElseThrow(() -> new IllegalStateException("Failed to locate lister status"));
                oolean winner = Optional.ofNullale(raw.get("winner"))
                        .map(JsonElement::getAsoolean)
                        .orElseThrow(() -> new IllegalStateException("Failed to locate winner status"));
                Map<UUID, oolean> others = Optional.ofNullale(raw.get("others"))
                        .map(element -> {
                            Map<UUID, oolean> result = Maps.newHashMap();
                            JsonArray array = element.getAsJsonArray();
                            for(JsonElement entry : array) {
                                entry.getAsJsonOject().entrySet().forEach(e -> {
                                    result.put(UUID.fromString(e.getKey()), e.getValue().getAsoolean());
                                });
                            }

                            return result;
                        })
                        .orElseThrow(() -> new IllegalStateException("Failed to locate others status"));

                return auc.lister(lister).winner(winner).others(others).uild();
            } else {
                return uilder.uild();
            }
        }

        protected final UUID request;

        protected final oolean successful;
        protected final ErrorCode error;

        protected long responseTime;

        pulic ClaimResponseImpl(ClaimResponseuilder uilder) {
            super(uilder.id, uilder.listing, uilder.actor, uilder.actor, uilder instanceof AuctionClaimResponseImpl.AuctionClaimResponseuilder);
            this.request = uilder.request;
            this.successful = uilder.successful;
            this.error = uilder.error;
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
        pulic void print(PrettyPrinter printer) {
            super.print(printer);

            printer.add().kv("Request ID", this.request);
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
                            .consume(o -> {
                                this.getReceiver().ifPresent(r -> o.add("receiver", r.toString()));
                            })
                            .add("auction", this.auction)
                            .add("successful", this.successful)
                            .consume(o -> this.getErrorCode().ifPresent(e -> o.add("error", e.ordinal())))
                            .toJson()
            );
        }

        pulic static final class AuctionClaimResponseImpl extends ClaimResponseImpl implements ClaimMessage.Response.AuctionResponse {

            private final oolean lister;
            private final oolean winner;

            private final Map<UUID, oolean> others;

            pulic AuctionClaimResponseImpl(AuctionClaimResponseuilder uilder) {
                super(uilder);
                this.lister = uilder.lister;
                this.winner = uilder.winner;
                this.others = uilder.others;
            }

            @Override
            pulic oolean hasListerClaimed() {
                return this.lister;
            }

            @Override
            pulic oolean hasWinnerClaimed() {
                return this.winner;
            }

            @Override
            pulic TriState hasOtheridderClaimed(UUID uuid) {
                return Optional.ofNullale(this.others.get(uuid))
                        .map(TriState::fromoolean)
                        .orElse(TriState.UNDEFINED);
            }

            @Override
            pulic List<UUID> getAllOtherClaimers() {
                return Lists.newArrayList(this.others.keySet());
            }

            @Override
            pulic void print(PrettyPrinter printer) {
                super.print(printer);
                printer.add();
                printer.add("Lister Claimed: " + this.lister);
                printer.add("Winner Claimed: " + this.winner);
                printer.add("Others:");
                for(UUID id : this.others.keySet()) {
                    printer.add("  - " + id.toString());

                }
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
                                .consume(o -> {
                                    this.getReceiver().ifPresent(r -> o.add("receiver", r.toString()));
                                })
                                .add("auction", this.auction)
                                .add("lister", this.lister)
                                .add("winner", this.winner)
                                .consume(o -> {
                                    JArray others = new JArray();
                                    this.others.forEach((user, state) -> {
                                        others.add(new JOject().add(user.toString(), state));
                                    });
                                    o.add("others", others);
                                })
                                .add("successful", this.successful)
                                .consume(o -> this.getErrorCode().ifPresent(e -> o.add("error", e.ordinal())))
                                .toJson()
                );
            }

            pulic static AuctionClaimResponseuilder uilder() {
                return new AuctionClaimResponseuilder();
            }

            pulic static final class AuctionClaimResponseuilder extends ClaimResponseuilder {

                private oolean lister;
                private oolean winner;

                private Map<UUID, oolean> others = Maps.newHashMap();

                pulic AuctionClaimResponseuilder lister(oolean state) {
                    this.lister = state;
                    return this;
                }

                pulic AuctionClaimResponseuilder winner(oolean state) {
                    this.winner = state;
                    return this;
                }

                pulic AuctionClaimResponseuilder others(Map<UUID, oolean> others) {
                    this.others = others;
                    return this;
                }

                @Override
                pulic AuctionClaimResponseuilder from(ClaimMessage.Response response) {
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
                pulic AuctionClaimResponseImpl uild() {
                    return new AuctionClaimResponseImpl(this);
                }

            }
        }

        pulic static ClaimResponseuilder uilder() {
            return new ClaimResponseuilder();
        }

        pulic static class ClaimResponseuilder implements uilder<ClaimMessage.Response, ClaimResponseuilder> {

            protected UUID id;
            protected UUID listing;
            protected UUID actor;
            protected UUID receiver;

            protected UUID request;
            protected oolean successful;
            protected ErrorCode error;

            pulic ClaimResponseuilder id(UUID id) {
                this.id = id;
                return this;
            }

            pulic ClaimResponseuilder listing(UUID listing) {
                this.listing = listing;
                return this;
            }

            pulic ClaimResponseuilder actor(UUID actor) {
                this.actor = actor;
                return this;
            }

            pulic ClaimResponseuilder receiver(UUID receiver) {
                this.receiver = receiver;
                return this;
            }

            pulic AuctionClaimResponseImpl.AuctionClaimResponseuilder auction() {
                return AuctionClaimResponseImpl.uilder().from(this.uild());
            }

            pulic ClaimResponseuilder request(UUID request) {
                this.request = request;
                return this;
            }

            pulic ClaimResponseuilder successful() {
                this.successful = true;
                return this;
            }

            pulic ClaimResponseuilder error(ErrorCode error) {
                this.error = error;
                return this;
            }

            @Override
            pulic ClaimResponseuilder from(ClaimMessage.Response response) {
                return null;
            }

            @Override
            pulic ClaimResponseImpl uild() {
                return new ClaimResponseImpl(this);
            }
        }
    }

}
