package me.nickimpact.gts.api;

import co.aikar.commands.CommandIssuer;
import com.nickimpact.impactor.api.registry.BuilderRegistry;
import me.nickimpact.gts.api.enums.CommandResults;
import me.nickimpact.gts.api.holders.EntryRegistry;
import me.nickimpact.gts.api.listings.manager.ListingManager;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.ui.EntryUI;
import me.nickimpact.gts.api.placeholders.PlaceholderParser;
import me.nickimpact.gts.api.registry.GTSRegistry;
import me.nickimpact.gts.api.searching.Searcher;
import me.nickimpact.gts.api.services.ServiceManager;
import me.nickimpact.gts.api.storage.IGtsStorage;
import me.nickimpact.gts.api.util.TriFunction;

import java.util.List;
import java.util.Optional;

public interface GtsService {

	static GtsService getInstance() {
		return GtsServiceProvider.get();
	}

	ServiceManager getServiceManager();

	GTSRegistry getRegistry();

	IGtsStorage getStorage();

	/**
	 * Receives the internal Entry Registry which is used to store things such as the entry type, UI
	 * representable, and represented icon.
	 *
	 * @return GTS's internal Entry registry
	 */
	EntryRegistry getEntryRegistry();

	/**
	 * Registers an entry into the GTS service. An entry will need an accompanying UI
	 * as well as an ItemStack representation for display purposes.
	 *
	 * @since 4.0.0
	 * @param entry The class of the entry to add to the entry
	 * @param ui The UI accompanying the entry
	 * @param rep The ItemStack representation accompanying the entry
	 */
	void registerEntry(List<String> identifier, Class<? extends Entry> entry, EntryUI ui, String rep, TriFunction<CommandIssuer, List<String>, Boolean, CommandResults> cmd);

	/**
	 * Registers a searching option for all listings in the listing manager.
	 *
	 * @param key The key for the search operation
	 * @param searcher The searcher
	 * @since 5.1.0
	 */
	void addSearcher(String key, Searcher searcher);

	Optional<Searcher> getSearcher(String key);

	void registerPlaceholder(String token, PlaceholderParser parser);

//	<T> void addMinPriceOption(Class<? extends Entry<?, T, ?, ?, ?>> type, Function<T, Double> function);
//
//	<T> List<Function<T, Double>> getMinPriceOptionsForEntryType(Class<? extends Entry<?, T, ?, ?, ?>> type);
}
