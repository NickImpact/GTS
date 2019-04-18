package me.nickimpact.gts.api;

import me.nickimpact.gts.api.holders.EntryRegistry;
import me.nickimpact.gts.api.listings.ListingManager;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.entries.EntryUI;
import me.nickimpact.gts.api.storage.IGtsStorage;
import me.nickimpact.gts.api.wrappers.CmdResultWrapper;
import me.nickimpact.gts.api.wrappers.CmdSourceWrapper;

import java.util.List;
import java.util.function.BiFunction;

public interface GtsService<CS extends CmdSourceWrapper, CR extends CmdResultWrapper> {

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
	void registerEntry(List<String> identifier, Class<? extends Entry> entry, EntryUI ui, String rep, BiFunction<CS, String[], CR> cmd);
}
