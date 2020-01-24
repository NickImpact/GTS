package me.nickimpact.gts.sponge.text.placeholders;

import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.api.placeholder.PlaceholderVariables;
import me.nickimpact.gts.api.listings.Listing;

public class ListingPlaceholderVariableKey implements PlaceholderVariables.Key<Listing> {

	@Override
	public String key() {
		return "GTS Listing";
	}

	@Override
	public TypeToken<Listing> getValueClass() {
		return new TypeToken<Listing>(){};
	}

}
