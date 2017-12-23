package com.nickimpact.gts.api.listings.pricing;

import com.google.gson.Gson;
import com.nickimpact.gts.GTS;
import com.nickimpact.gts.api.json.Adapter;
import com.nickimpact.gts.api.json.Registry;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class PriceAdapter extends Adapter<Price> {

	@Override
	public Gson getGson() {
		return GTS.prettyGson;
	}

	@Override
	public Registry getRegistry() {
		return GTS.getInstance().getApi().getRegistry(Price.class);
	}
}
