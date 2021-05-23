package net.impactdev.gts.listings.searcher;

import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.searching.Searcher;
import org.spongepowered.api.Sponge;

public class SpongeUserSearcher implements Searcher {

    @Override
    public boolean parse(Listing listing, String input) {
        return Sponge.getServer().getPlayer(listing.getLister())
                .map(user -> user.getName().toLowerCase().startsWith(input.toLowerCase()))
                .orElse(false);
    }

}
