package net.impactdev.gts.common.listings;

import com.google.gson.JsonObject;
import net.impactdev.impactor.api.json.factory.JObject;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.entries.Entry;
import net.impactdev.gts.api.listings.makeup.Display;
import net.kyori.adventure.text.TextComponent;

import java.util.List;
import java.util.Optional;
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
public class JsonStoredEntry implements Entry<JsonObject, Void> {

	private final JsonObject data;

	public JsonStoredEntry(JsonObject data) {
		this.data = data;
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
	public TextComponent getDescription() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Display<Void> getDisplay(UUID viewer) {
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

	@Override
	public Optional<String> getThumbnailURL() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<String> getDetails() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getVersion() {
		throw new UnsupportedOperationException();
	}

	@Override
	public JObject serialize() {
		throw new UnsupportedOperationException();
	}

}
