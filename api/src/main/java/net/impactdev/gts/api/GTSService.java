package net.impactdev.gts.api;

import com.google.common.collect.ImmutableList;
import net.impactdev.gts.api.data.registry.GTSComponentManager;
import net.impactdev.gts.api.extension.Extension;
import net.impactdev.gts.api.maintenance.MaintenanceManager;
import net.impactdev.gts.api.player.PlayerSettingsManager;
import net.impactdev.gts.api.searching.Searcher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GTSService {

	static GTSService getInstance() {
		return GTSServiceProvider.get();
	}

	/**
	 * Returns an unmodifiable list of extensions currently hooked and running with GTS
	 *
	 * @return An immutable list of all loaded extensions hooked to GTS
	 */
	ImmutableList<Extension> getAllExtensions();

	/**
	 * Represents the general component manager for the key parts of GTS (Listings, Entry Types, and Prices).
	 * This is where you can register your own custom types of data
	 *
	 * @return The component manager responsible for managing the types of Listings,
	 * Entries, and Prices GTS can manipulate.
	 */
	GTSComponentManager getGTSComponentManager();

	/**
	 * Represents the location of all user-specific player settings.
	 *
	 * @return A mapping manager of all individual player settings
	 */
	PlayerSettingsManager getPlayerSettingsManager();

	/**
	 * Specifies settings stating if the plugin itself is in maintenance mode, or a specific feature is disabled.
	 *
	 * <p>Users can use these settings to ultimately disable an entire feature of GTS, should they believe a bug
	 * has been found or even other reasons.</p>
	 *
	 * @return The manager controlling settings regarding GTS maintenance status states
	 */
	MaintenanceManager getMaintenanceManager();

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
