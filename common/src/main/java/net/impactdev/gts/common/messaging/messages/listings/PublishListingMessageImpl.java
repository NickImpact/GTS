package net.impactdev.gts.common.messaging.messages.listings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.impactdev.gts.api.messaging.message.type.listings.PublishListingMessage;
import net.impactdev.gts.api.util.PrettyPrinter;
import net.impactdev.gts.common.messaging.GTSMessagingService;
import net.impactdev.gts.common.messaging.messages.AbstractMessage;
import net.impactdev.impactor.api.json.factory.JObject;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;
import java.util.UUID;

public class PublishListingMessageImpl extends AbstractMessage implements PublishListingMessage {

    public static final String TYPE = "Listings/Publish";

    public static PublishListingMessageImpl decode(@Nullable JsonElement content, UUID id) {
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
        boolean auction = Optional.ofNullable(raw.get("auction"))
                .map(JsonElement::getAsBoolean)
                .orElseThrow(() -> new IllegalStateException("Unable to locate auction status marker"));

        return new PublishListingMessageImpl(id, listing, actor, auction);
    }

    private final UUID listing;
    private final UUID actor;
    private final boolean auction;

    public PublishListingMessageImpl(UUID id, UUID listing, UUID actor, boolean auction) {
        super(id);
        this.listing = listing;
        this.actor = actor;
        this.auction = auction;
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
    public boolean isAuction() {
        return this.auction;
    }

    @Override
    public @NonNull String asEncodedString() {
        return GTSMessagingService.encodeMessageAsString(
                TYPE,
                this.getID(),
                new JObject()
                        .add("listing", this.listing.toString())
                        .add("actor", this.actor.toString())
                        .add("auction", this.auction)
                        .toJson()
        );
    }

    @Override
    public void print(PrettyPrinter printer) {
        printer.kv("Request ID", this.getID())
                .kv("Listing ID", this.getListingID())
                .kv("Actor", this.getActor())
                .kv("Is Auction", this.isAuction());
    }

}
