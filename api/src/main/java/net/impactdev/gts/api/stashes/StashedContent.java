package net.impactdev.gts.api.stashes;

import net.impactdev.gts.api.deliveries.Delivery;
import net.impactdev.gts.api.listings.Listing;
import net.kyori.adventure.util.TriState;

import java.util.UUID;

public abstract class StashedContent<T> {

    private final T content;
    private final TriState context;

    public StashedContent(T content, TriState context) {
        this.content = content;
        this.context = context;
    }

    public abstract UUID getID();

    public T getContent() {
        return this.content;
    }

    public TriState getContext() {
        return this.context;
    }

    public static class ListingContent extends StashedContent<Listing> {

        public ListingContent(Listing content, TriState context) {
            super(content, context);
        }

        @Override
        public UUID getID() {
            return this.getContent().getID();
        }

    }

    public static class DeliverableContent extends StashedContent<Delivery> {

        public DeliverableContent(Delivery content, TriState context) {
            super(content, context);
        }

        @Override
        public UUID getID() {
            return this.getContent().getID();
        }

    }

}
