package me.nickimpact.gts.api.listings.entries;

import com.google.gson.Gson;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.GtsService;
import me.nickimpact.gts.api.json.Adapter;
import me.nickimpact.gts.api.json.Registry;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class EntryAdapter extends Adapter<Entry> {

	@Override
	public Gson getGson() {
		return GTS.prettyGson;
	}

	@Override
	public Registry getRegistry() {
		return GTS.getInstance().getService().getRegistry(GtsService.RegistryType.ENTRY);
	}
}
