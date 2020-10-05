package net.impactdev.gts.api;

import com.google.common.collect.ImmutableList;
import net.impactdev.gts.api.data.registry.GTSComponentManager;
import net.impactdev.gts.api.extension.Extension;
import net.impactdev.gts.api.player.PlayerSettingsManager;
import net.impactdev.gts.api.searching.Searcher;

import java.util.Optional;

public interface GTSService {

	static GTSService getInstance() {
		return GTSServiceProvider.get();
	}

	/**
	 *
	 *
	 * @since 6.0.0
	 * @return An immutable list of all loaded extensions hooked to GTS
	 */
	ImmutableList<Extension> getAllExtensions();

	GTSComponentManager getGTSComponentManager();

	/**
	 *
	 * @return A mapping manager of all individual player settings
	 *
	 * @since 6.0.0
	 */
	PlayerSettingsManager getPlayerSettingsManager();

	/**
	 * Registers a searching option for all listings in the listing manager.
	 *
	 * @param key The key for the search operation
	 * @param searcher The searcher
	 * @since 5.1.0
	 */
	void addSearcher(String key, Searcher searcher);

	/**
	 *
	 * @param key The key representing a particular searcher
	 * @return A matching Searcher if one is found belonging to the input key, or empty if none match
	 *
	 * @since 5.1.0
	 */
	Optional<Searcher> getSearcher(String key);

}
