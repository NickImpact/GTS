package me.nickimpact.gts.sponge.listings.makeup;

import lombok.AllArgsConstructor;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.makeup.Display;
import me.nickimpact.gts.api.user.Source;
import me.nickimpact.gts.api.util.gson.JObject;
import me.nickimpact.gts.sponge.listings.SpongeListing;
import me.nickimpact.gts.sponge.sources.SpongeSource;

@AllArgsConstructor
public abstract class SpongeEntry<T> implements Entry<T> {

	private JObject data;

	public JObject getInternalData() {
		return this.data;
	}

	@Override
	public Display getDisplay(Source source, Listing listing) {
		return this.getDisplay((SpongeSource) source, (SpongeListing) listing);
	}

	public abstract SpongeDisplay getDisplay(SpongeSource source, SpongeListing listing);

}
