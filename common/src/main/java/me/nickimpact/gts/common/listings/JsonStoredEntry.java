package me.nickimpact.gts.common.listings;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.makeup.Display;
import net.kyori.text.TextComponent;

import java.util.UUID;

/**
 * A JsonStoredEntry represents the backing that Proxy servers will use in order to manage all listings from the
 * representative cache. As these proxy servers will have no way to determine what type of entry they are working
 * with, they must all end up in a similar manner. To achieve this, this class simply does 0 deserialization. In
 * other words, any and all JSON data representing a entry will remain serialized should the element ever be requested.
 *
 * NOTE: This class is only intended for references. As such, all other functionality other than data fetching
 * will be unsupported, and throw an error during any attempt to call these functions.
 */
@AllArgsConstructor
public class JsonStoredEntry implements Entry<JsonObject, JsonObject, Void> {

	private final JsonObject data;

	@Override
	public JsonObject getInternalData() {
		return this.data;
	}

	@Override
	public JsonObject getOrCreateElement() {
		return this.data;
	}

	@Override
	public TextComponent getName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Display<Void> getDisplay(UUID viewer, Listing listing) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean give(UUID receiver) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean take(UUID depositor) {
		throw new UnsupportedOperationException();
	}

}
