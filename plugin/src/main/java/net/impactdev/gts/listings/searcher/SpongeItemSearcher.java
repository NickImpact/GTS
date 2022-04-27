package net.impactdev.gts.listings.searcher;

import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.searching.Searcher;
import net.impactdev.gts.listings.SpongeItemEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.data.Keys;

public class SpongeItemSearcher implements Searcher {

    @Override
    public boolean parse(Listing listing, String input) {
        if(listing.getEntry() instanceof SpongeItemEntry) {
            SpongeItemEntry entry = (SpongeItemEntry) listing.getEntry();
            Component name = entry.getOrCreateElement().get(Keys.CUSTOM_NAME)
                    .orElse(entry.getOrCreateElement().type().asComponent());

            return LegacyComponentSerializer.legacyAmpersand().serialize(name).toLowerCase().contains(input.toLowerCase());
        }

        return false;
    }

}
