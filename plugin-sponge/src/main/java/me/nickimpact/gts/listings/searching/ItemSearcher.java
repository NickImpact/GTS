package me.nickimpact.gts.listings.searching;

import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.searching.Searcher;
import me.nickimpact.gts.listings.SpongeItemEntry;
import me.nickimpact.gts.sponge.SpongeListing;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.inventory.ItemStack;

public class ItemSearcher implements Searcher {

	@Override
	public boolean parse(Listing listing, String name) {
		SpongeListing sl = (SpongeListing) listing;
		if(sl.getEntry() instanceof SpongeItemEntry) {
			ItemStack item = ((SpongeItemEntry) sl.getEntry()).getEntry();
			return item.getType().getName().split(":")[1].replaceAll("_", " ").equalsIgnoreCase(name) || item.get(Keys.DISPLAY_NAME).map(text -> text.toPlain().equalsIgnoreCase(name)).orElse(false);
		}
		return false;
	}

}
