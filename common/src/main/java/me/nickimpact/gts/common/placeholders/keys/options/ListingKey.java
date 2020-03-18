package me.nickimpact.gts.common.placeholders.keys.options;

import com.google.common.reflect.TypeToken;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.placeholders.PlaceholderVariables;

public class ListingKey implements PlaceholderVariables.Key<Listing> {

	@Override
	public String key() {
		return "listing";
	}

	@Override
	public TypeToken<Listing> getValueClass() {
		return new TypeToken<Listing>() {};
	}

}
