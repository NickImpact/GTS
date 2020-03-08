package me.nickimpact.gts.common.json;

import com.nickimpact.impactor.api.json.Adapter;
import com.nickimpact.impactor.api.json.Registry;
import com.nickimpact.impactor.api.plugin.ImpactorPlugin;
import me.nickimpact.gts.api.GtsServiceProvider;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.common.plugin.GTSPlugin;

public class EntryAdapter extends Adapter<Entry> {

	public EntryAdapter(ImpactorPlugin plugin) {
		super(plugin);
	}

	@Override
	protected Registry<Entry> getRegistry() {
		return GtsServiceProvider.get().getEntryRegistry().getRegistry();
	}
}
