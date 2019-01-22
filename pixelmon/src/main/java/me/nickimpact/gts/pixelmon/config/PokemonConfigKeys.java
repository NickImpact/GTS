package me.nickimpact.gts.pixelmon.config;

import com.google.common.collect.Lists;
import com.nickimpact.impactor.api.configuration.ConfigKey;
import com.nickimpact.impactor.api.configuration.IConfigKeys;
import com.nickimpact.impactor.api.configuration.keys.ListKey;

import java.util.List;
import java.util.Map;

public class PokemonConfigKeys implements IConfigKeys {

	public static ConfigKey<List<String>> POKEMON_SELLER_PREVIEW = ListKey.of("pokemon.seller.preview", Lists.newArrayList(
			"&7Ability: &e{{ability}}",
			"&7Gender: &e{{gender}}",
			"&7Nature: &e{{nature}}",
			"&7Size: &e{{growth}}",
			"",
			"&7EVs: &e{{evs_total}}&7/&e510 &7(&a{{evs_percent}}&7)",
			"&7IVs: &e{{ivs_total}}&7/&e186 &7(&a{{ivs_percent}}&7)"

	));

	@Override
	public Map<String, ConfigKey<?>> getAllKeys() {
		return null;
	}
}
