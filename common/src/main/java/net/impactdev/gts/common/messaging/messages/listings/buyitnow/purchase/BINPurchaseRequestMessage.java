package net.impactdev.gts.common.messaging.messages.listings.buyitnow.purchase;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.impactdev.gts.api.messaging.message.type.listings.BuyItNowMessage;
import net.impactdev.gts.common.messaging.GTSMessagingService;
import net.impactdev.gts.common.messaging.messages.AbstractMessage;
import net.impactdev.impactor.api.json.factory.JObject;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BINPurchaseRequestMessage extends AbstractMessage implements BuyItNowMessage.Purchase.Request {

    private static final String TYPE = "BIN/Purchase/Request";

    private static BINPurchaseRequestMessage decode(@Nullable JsonElement content, UUID id) {
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

        return new BINPurchaseRequestMessage(id, listing, actor);
    }

    private UUID listing;
    private UUID actor;

    public BINPurchaseRequestMessage(UUID id, UUID listing, UUID actor) {
        super(id);
        this.listing = listing;
        this.actor = actor;
    }

    @Override
    public CompletableFuture<Purchase.Response> respond() {
        return null;
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
    public @NonNull String asEncodedString() {
        return GTSMessagingService.encodeMessageAsString(
                TYPE,
                this.getID(),
                new JObject()
                        .add("listing", this.listing.toString())
                        .add("actor", this.actor.toString())
                        .toJson()
        );
    }
}
