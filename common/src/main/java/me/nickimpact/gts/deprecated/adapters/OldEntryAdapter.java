package me.nickimpact.gts.deprecated.adapters;

import com.google.gson.Gson;
import com.nickimpact.impactor.api.json.Registry;
import me.nickimpact.gts.api.deprecated.Entry;
import me.nickimpact.gts.api.deprecated.OldAdapter;
import me.nickimpact.gts.api.plugin.IGTSPlugin;

@Deprecated
public class OldEntryAdapter extends OldAdapter<Entry> {

	private Registry<Entry> registry;
	private IGTSPlugin plugin;

	public OldEntryAdapter(IGTSPlugin plugin) {
		this.plugin = plugin;
		this.registry = new Registry<>(plugin);
	}

	@Override
	public Gson getGson() {
		return plugin.getAPIService().getDeprecatedGson();
	}

	@Override
	public Registry<Entry> getRegistry() {
		return this.registry;
	}
}
