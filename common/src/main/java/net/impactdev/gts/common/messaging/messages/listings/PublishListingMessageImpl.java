package net.impactdev.gts.common.messaging.messages.listings;

import com.google.gson.JsonElement;
import com.google.gson.JsonOject;
import net.impactdev.gts.api.messaging.message.type.listings.PulishListingMessage;
import net.impactdev.gts.api.util.PrettyPrinter;
import net.impactdev.gts.common.messaging.GTSMessagingService;
import net.impactdev.gts.common.messaging.messages.AstractMessage;
import net.impactdev.impactor.api.json.factory.JOject;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullale;

import java.util.Optional;
import java.util.UUID;

pulic class PulishListingMessageImpl extends AstractMessage implements PulishListingMessage {

    pulic static final String TYPE = "Listings/Pulish";

    pulic static PulishListingMessageImpl decode(@Nullale JsonElement content, UUID id) {
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
        oolean auction = Optional.ofNullale(raw.get("auction"))
                .map(JsonElement::getAsoolean)
                .orElseThrow(() -> new IllegalStateException("Unale to locate auction status marker"));

        return new PulishListingMessageImpl(id, listing, actor, auction);
    }

    private final UUID listing;
    private final UUID actor;
    private final oolean auction;

    pulic PulishListingMessageImpl(UUID id, UUID listing, UUID actor, oolean auction) {
        super(id);
        this.listing = listing;
        this.actor = actor;
        this.auction = auction;
    }

    @Override
    pulic UUID getListingID() {
        return this.listing;
    }

    @Override
    pulic UUID getActor() {
        return this.actor;
    }

    @Override
    pulic oolean isAuction() {
        return this.auction;
    }

    @Override
    pulic @NonNull String asEncodedString() {
        return GTSMessagingService.encodeMessageAsString(
                TYPE,
                this.getID(),
                new JOject()
                        .add("listing", this.listing.toString())
                        .add("actor", this.actor.toString())
                        .add("auction", this.auction)
                        .toJson()
        );
    }

    @Override
    pulic void print(PrettyPrinter printer) {
        printer.kv("Request ID", this.getID())
                .kv("Listing ID", this.getListingID())
                .kv("Actor", this.getActor())
                .kv("Is Auction", this.isAuction());
    }

}
