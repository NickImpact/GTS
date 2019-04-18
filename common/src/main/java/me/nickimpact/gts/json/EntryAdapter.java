package me.nickimpact.gts.json;

import com.google.gson.Gson;
import com.nickimpact.impactor.api.json.Adapter;
import com.nickimpact.impactor.api.json.Registry;
import com.nickimpact.impactor.api.plugin.ImpactorPlugin;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.plugin.IGTSPlugin;

public class EntryAdapter extends Adapter<Entry> {

	public EntryAdapter(ImpactorPlugin plugin) {
		super(plugin);
	}

	@Override
	protected Gson getGson() {
		return ((IGTSPlugin) plugin).getGson();
	}

	@Override
	protected Registry<Entry> getRegistry() {
		return ((IGTSPlugin) plugin).getAPIService().getEntryRegistry().getRegistry();
	}
}
