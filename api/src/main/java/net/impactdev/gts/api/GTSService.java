package net.impactdev.gts.api;

import com.google.common.collect.ImmutableList;
import net.impactdev.gts.api.data.registry.GTSComponentManager;
import net.impactdev.gts.api.extension.Extension;
import net.impactdev.gts.api.player.PlayerSettingsManager;
import net.impactdev.gts.api.searching.Searcher;

import java.util.List;
import java.util.Optional;

public interface GTSService {

	static GTSService getInstance() {
		return GTSServiceProvider.get();
	}

	/**
	 *
	 *
	 * @return An immutable list of all loaded extensions hooked to GTS
	 */
	ImmutableList<Extension> getAllExtensions();

	/**
	 *
	 *
	 * @return The component manager responsible for managing the types of Listings,
	 * Entries, and Prices GTS can manipulate.
	 */
	GTSComponentManager getGTSComponentManager();

	/**
	 *
	 * @return A mapping manager of all individual player settings
	 */
	PlayerSettingsManager getPlayerSettingsManager();

	/**
	 * Registers a searching option for all listings in the listing manager.
	 *
	 * @param searcher The searcher
	 */
	void addSearcher(Searcher searcher);

	/**
	 * The set of registered searchers that a user can use to find listings matching their query.
	 *
	 * @return Every registered searcher
	 */
	List<Searcher> getSearchers();

}
