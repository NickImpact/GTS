package me.nickimpact.gts.api;

import me.nickimpact.gts.api.json.Registry;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.text.TokenService;
import me.nickimpact.gts.entries.prices.MoneyPrice;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface GtsService {

	/**
	 * Attempts to fetch the current status of a Registry. There are two registries available,
	 * and are represented by the {@link RegistryType} enum.
	 *
	 * @param type The type of registry you wish to fetch
	 * @return The {@link Registry} based upon the request
	 */
	Registry getRegistry(RegistryType type);

	/**
	 * Registers an entry into the GTS service. An entry will need an accompanying UI
	 * as well as an ItemStack representation for display purposes.
	 *
	 * @since 4.0.0
	 * @param entry The class of the entry to add to the entry
	 * @param ui The UI accompanying the entry
	 * @param rep The ItemStack representation accomanying the entry
	 */
	void registerEntry(Class<? extends Entry> entry, EntryUI ui, ItemStack rep);

	/**
	 * Fetches a list of all types of entries currently registered to GTS.
	 *
	 * @return All the entries currently registered with the GTS Service.
	 */
	Collection<Class<? extends Entry>> getEntries();

	/**
	 * Returns the NucleusTokenService implementation from Impactor being used by the plugin.
	 *
	 * @return The NucleusTokenService implementation.
	 */
	TokenService getTokensService();

	enum RegistryType {
		ENTRY,
		PRICE,
	}
}
