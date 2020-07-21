package me.nickimpact.gts.api;

import me.nickimpact.gts.api.data.registry.StorableRegistry;
import me.nickimpact.gts.api.searching.Searcher;

import java.util.Optional;

public interface GTSService {

	static GTSService getInstance() {
		return GTSServiceProvider.get();
	}

	/**
	 * Represents a registry in which objects that can be serialized and deserialized to and from JSON
	 * data can be referenced. GTS will employ this registry when it comes to attempting to work with
	 * storable data.
	 */
	StorableRegistry getStorableRegistry();

	/**
	 * Registers a searching option for all listings in the listing manager.
	 *
	 * @param key The key for the search operation
	 * @param searcher The searcher
	 * @since 5.1.0
	 */
	void addSearcher(String key, Searcher searcher);

	Optional<Searcher> getSearcher(String key);

}
