package me.nickimpact.gts.deprecated.adapters;

import com.google.gson.Gson;
import com.nickimpact.impactor.api.json.Registry;
import me.nickimpact.gts.api.deprecated.OldAdapter;
import me.nickimpact.gts.api.deprecated.Price;
import me.nickimpact.gts.api.plugin.IGTSPlugin;

@Deprecated
public class OldPriceAdapter extends OldAdapter<Price> {

	private Registry<Price> registry;
	private IGTSPlugin plugin;

	public OldPriceAdapter(IGTSPlugin plugin) {
		this.plugin = plugin;
		this.registry = new Registry<>(plugin);
	}

	@Override
	public Gson getGson() {
		return plugin.getAPIService().getDeprecatedGson();
	}

	@Override
	public Registry<Price> getRegistry() {
		return this.registry;
	}
}
