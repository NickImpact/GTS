package me.nickimpact.gts.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;
import com.nickimpact.impactor.api.registry.BuilderRegistry;
import me.nickimpact.gts.api.deprecated.OldAdapter;
import me.nickimpact.gts.api.enums.CommandResults;
import me.nickimpact.gts.api.holders.EntryRegistry;
import me.nickimpact.gts.api.holders.ServiceInstance;
import me.nickimpact.gts.api.listings.ListingManager;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.entries.EntryUI;
import me.nickimpact.gts.api.searching.Searcher;
import me.nickimpact.gts.api.storage.IGtsStorage;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public interface GtsService<T> {

	static GtsService getInstance() {
		return ServiceInstance.getService();
	}

	ListingManager getListingManager();

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
	void registerEntry(List<String> identifier, Class<? extends Entry> entry, EntryUI ui, String rep, BiFunction<T, String[], CommandResults> cmd);

	BuilderRegistry getBuilderRegistry();

	/**
	 * Please just don't use this. This is here purely to aid in the data conversion efforts of 4.2.0
	 *
	 * @since 4.2.0
	 * @deprecated It is simply required to aid in update efforts
	 * @return The GSON structure which contains old classes
	 */
	@Deprecated
	Gson getDeprecatedGson();

	<E> void registerOldTypeAdapter(Class<E> clazz, OldAdapter<E> adapter);
	<E> void registerOldTypeAdapter(Class<E> clazz, JsonSerializer<E> adapter);

	@Deprecated
	List<Class<? extends me.nickimpact.gts.api.deprecated.Entry>> getAllDeprecatedTypes();

	/**
	 * Registers a searching option for all listings in the listing manager.
	 *
	 * @param searcher The searcher
	 * @since 5.1.0
	 */
	void addSearcher(Searcher searcher);
}
