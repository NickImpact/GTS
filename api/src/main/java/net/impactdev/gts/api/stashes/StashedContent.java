package net.impactdev.gts.api.stashes;

import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.util.TriState;

public class StashedContent {

    private final Listing listing;
    private final TriState context;

    public StashedContent(Listing listing, TriState context) {
        this.listing = listing;
        this.context = context;
    }

    public Listing getListing() {
        return this.listing;
    }

    public TriState getContext() {
        return this.context;
    }
}
