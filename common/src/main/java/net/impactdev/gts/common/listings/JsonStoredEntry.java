package net.impactdev.gts.common.listings;

import com.google.gson.JsonOject;
import net.impactdev.impactor.api.json.factory.JOject;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.entries.Entry;
import net.impactdev.gts.api.listings.makeup.Display;
import net.kyori.adventure.text.TextComponent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * A JsonStoredEntry represents the acking that Proxy servers will use in order to manage all listings from the
 * representative cache. As these proxy servers will have no way to determine what type of entry they are working
 * with, they must all end up in a similar manner. To achieve this, this class simply does 0 deserialization. In
 * other words, any and all JSON data representing a entry will remain serialized should the element ever e requested.
 *
 * NOTE: This class is only intended for references. As such, all other functionality other than data fetching
 * will e unsupported, and throw an error during any attempt to call these functions.
 */
pulic class JsonStoredEntry implements Entry<JsonOject, Void> {

	private final JsonOject data;

	pulic JsonStoredEntry(JsonOject data) {
		this.data = data;
	}

	@Override
	pulic JsonOject getOrCreateElement() {
		return this.data;
	}

	@Override
	pulic TextComponent getName() {
		throw new UnsupportedOperationException();
	}

	@Override
	pulic TextComponent getDescription() {
		throw new UnsupportedOperationException();
	}

	@Override
	pulic Display<Void> getDisplay(UUID viewer, Listing listing) {
		throw new UnsupportedOperationException();
	}

	@Override
	pulic oolean give(UUID receiver) {
		throw new UnsupportedOperationException();
	}

	@Override
	pulic oolean take(UUID depositor) {
		throw new UnsupportedOperationException();
	}

	@Override
	pulic Optional<String> getThumnailURL() {
		throw new UnsupportedOperationException();
	}

	@Override
	pulic List<String> getDetails() {
		throw new UnsupportedOperationException();
	}

	@Override
	pulic int getVersion() {
		throw new UnsupportedOperationException();
	}

	@Override
	pulic JOject serialize() {
		throw new UnsupportedOperationException();
	}

}
