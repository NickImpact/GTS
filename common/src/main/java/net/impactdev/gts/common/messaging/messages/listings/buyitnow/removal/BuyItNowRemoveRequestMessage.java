package net.impactdev.gts.common.messaging.messages.listings.buyitnow.removal;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.util.PrettyPrinter;
import net.impactdev.impactor.api.json.factory.JObject;
import net.impactdev.gts.api.messaging.message.type.listings.BuyItNowMessage;
import net.impactdev.gts.common.messaging.GTSMessagingService;
import net.impactdev.gts.common.messaging.messages.AbstractMessage;
import net.impactdev.gts.common.plugin.GTSPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BuyItNowRemoveRequestMessage extends AbstractMessage implements BuyItNowMessage.Remove.Request {

    public static final String TYPE = "BIN - Remove Request";

    public static BuyItNowRemoveRequestMessage decode(@Nullable JsonElement content, UUID id) {
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

        return new BuyItNowRemoveRequestMessage(id, listing, actor, receiver, shouldReceive);
    }

    private final UUID listing;
    private final UUID actor;
    private final UUID receiver;
    private final boolean shouldReceive;

    public BuyItNowRemoveRequestMessage(UUID id, UUID listing, UUID actor) {
        this(id, listing, actor, null, true);
    }

    public BuyItNowRemoveRequestMessage(UUID id, UUID listing, UUID actor, @Nullable UUID receiver, boolean shouldReceive) {
        super(id);
        this.listing = listing;
        this.actor = actor;
        this.receiver = receiver;
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
                        .add("listing", this.listing.toString())
                        .add("actor", this.actor.toString())
                        .consume(o -> {
                            if(this.receiver != null) {
                                o.add("receiver", this.receiver.toString());
                            }
                        })
                        .add("shouldReceive", this.shouldReceive)
                        .toJson()
        );
    }

    @Override
    public CompletableFuture<Remove.Response> respond() {
        return GTSPlugin.getInstance().getStorage().processListingRemoveRequest(this);
    }

    @Override
    public void print(PrettyPrinter printer) {
        printer.add("Request Information:")
                .hr('-')
                .kv("Request ID", this.getID())
                .kv("Listing ID", this.getListingID())
                .kv("Actor", this.getActor())
                .kv("Receiver", this.getRecipient().orElse(Listing.SERVER_ID))
                .kv("Should Receive", this.shouldReturnListing());
    }
}
