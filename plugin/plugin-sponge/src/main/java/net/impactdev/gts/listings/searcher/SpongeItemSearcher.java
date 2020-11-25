package net.impactdev.gts.listings.searcher;

import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.searching.Searcher;
import net.impactdev.gts.listings.SpongeItemEntry;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.text.Text;

public class SpongeItemSearcher implements Searcher {

    @Override
    public boolean parse(Listing listing, String input) {
        if(listing.getEntry() instanceof SpongeItemEntry) {
            SpongeItemEntry entry = (SpongeItemEntry) listing.getEntry();
            String name = entry.getOrCreateElement().get(Keys.DISPLAY_NAME)
                    .map(Text::toPlain)
                    .orElse(entry.getOrCreateElement().getTranslation().get());

            return name.toLowerCase().startsWith(input.toLowerCase());
        }

        return false;
    }

}
