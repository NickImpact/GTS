package net.impactdev.gts.listings.searcher;

import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.searching.Searcher;
import org.spongepowered.api.Sponge;

public class SpongeUserSearcher implements Searcher {

    @Override
    public boolean parse(Listing listing, String input) {
        return Sponge.server().player(listing.getLister())
                .map(user -> user.name().toLowerCase().startsWith(input.toLowerCase()))
                .orElse(false);
    }

}
