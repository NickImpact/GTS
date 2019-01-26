package me.nickimpact.gts.pixelmon.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.nickimpact.impactor.api.configuration.ConfigKey;
import com.nickimpact.impactor.api.configuration.IConfigKeys;
import com.nickimpact.impactor.api.configuration.keys.ListKey;
import com.nickimpact.impactor.api.configuration.keys.StringKey;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PokemonMsgConfigKeys implements IConfigKeys {

	public static ConfigKey<List<String>> POKEMON_SELLER_PREVIEW = ListKey.of("seller.preview", Lists.newArrayList(
			"&7Ability: &e{{ability}}",
			"&7Gender: &e{{gender}}",
			"&7Nature: &e{{nature}}",
			"&7Size: &e{{growth}}",
			"",
			"&7EVs: &e{{evs_total}}&7/&e510 &7(&a{{evs_percent}}&7)",
			"&7IVs: &e{{ivs_total}}&7/&e186 &7(&a{{ivs_percent}}&7)"

	));

	public static final ConfigKey<String> POKEMON_LAST_MEMBER = StringKey.of("entries.pokemon.last-member", "{{gts_error}} You can't list your last non-egg party member!");
	public static final ConfigKey<String> POKEMON_ENTRY_CONFIRM_TITLE_AUCTION = StringKey.of("entries.pokemon.confirm.title-auction", "&eBid on {{pokemon}}?");
	public static final ConfigKey<List<String>> POKEMON_ENTRY_CONFIRM_LORE_AUCTION = ListKey.of("entries.pokemon.confirm.lore-auction", Lists.newArrayList("&7Here's some additional info:",
			"&7EVs: &e{{evs_total}}&7/&e510 &7(&a{{evs_percent}}&7)",
			"&7IVs: &e{{ivs_total}}&7/&e186 &7(&a{{ivs_percent}}&7)",
			"",
			"&7Move Set:",
			"  &7 - &e{{moves_1}}",
			"  &7 - &e{{moves_2}}",
			"  &7 - &e{{moves_3}}",
			"  &7 - &e{{moves_4}}"
	));
	public static final ConfigKey<String> POKEMON_ENTRY_SPEC_TEMPLATE = StringKey.of("entries.pokemon.spec-template", "{{ability:s}}{{ivs_percent:s}}{{ivs_stat:s}}{{shiny:s}}{{texture:s}}&a{{pokemon}}");
	public static final ConfigKey<String> POKEMON_ENTRY_SPEC_TEMPLATE_EGG = StringKey.of("entries.pokemon.egg-spec-template", "&a{{pokemon}}");
	public static final ConfigKey<String> POKEMON_ENTRY_BASE_TITLE = StringKey.of("entries.pokemon.base.title", "&e{{pokemon}} {{shiny:s}}&7| &bLvl {{level}}");
	public static final ConfigKey<List<String>> POKEMON_ENTRY_BASE_LORE = ListKey.of("entries.pokemon.base.lore.base", Lists.newArrayList(
			"&7Seller: &e{{seller}}",
			"",
			"&7Ability: &e{{ability}}",
			"&7Gender: &e{{gender}}",
			"&7Nature: &e{{nature}}",
			"&7Size: &e{{growth}}"
	));
	public static final ConfigKey<List<String>> POKEMON_ENTRY_BASE_MEW_CLONES = ListKey.of("entries.pokemon.base.lore.mew-clones", Lists.newArrayList(
			"&7Clones: &e{{clones}}"
	));
	public static final ConfigKey<List<String>> POKEMON_ENTRY_BASE_LAKE_TRIO = ListKey.of("entries.pokemon.base.lore.lake-trio", Lists.newArrayList(
			"&7Gemmed: &e{{enchanted}}"
	));
	public static final ConfigKey<List<String>> PE_BASE_TEXTURE = ListKey.of("entries.pokemon.base.lore.texture", Lists.newArrayList(
			"&7Texture: &e{{texture}}"
	));
	public static final ConfigKey<List<String>> PE_BASE_POKERUS = ListKey.of("entries.pokemon.base.lore.pokerus", Lists.newArrayList(
			"&7Inflicted with &dPokerus"
	));
	public static final ConfigKey<List<String>> PE_BASE_UNBREEDABLE = ListKey.of("entries.pokemon.base.lore.unbreedable", Lists.newArrayList(
			"&cUnbreedable"
	));

	public static final ConfigKey<String> POKEMON_ENTRY_CONFIRM_TITLE = StringKey.of("entries.pokemon.confirm.title", "&ePurchase {{pokemon}}?");
	public static final ConfigKey<List<String>> POKEMON_ENTRY_CONFIRM_LORE = ListKey.of("entries.pokemon.confirm.lore", Lists.newArrayList(
			"&7Here's some additional info:",
			"&7EVs: &e{{evs_total}}&7/&e510 &7(&a{{evs_percent}}&7)",
			"&7IVs: &e{{ivs_total}}&7/&e186 &7(&a{{ivs_percent}}&7)",
			"",
			"&7Move Set:",
			"  &7 - &e{{moves_1}}",
			"  &7 - &e{{moves_2}}",
			"  &7 - &e{{moves_3}}",
			"  &7 - &e{{moves_4}}"
	));

	private static Map<String, ConfigKey<?>> KEYS = null;

	@Override
	public synchronized Map<String, ConfigKey<?>> getAllKeys() {
		if(KEYS == null) {
			Map<String, ConfigKey<?>> keys = new LinkedHashMap<>();

			try {
				Field[] values = PokemonMsgConfigKeys.class.getFields();
				for(Field f : values) {
					if(!Modifier.isStatic(f.getModifiers()))
						continue;

					Object val = f.get(null);
					if(val instanceof ConfigKey<?>)
						keys.put(f.getName(), (ConfigKey<?>) val);
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			KEYS = ImmutableMap.copyOf(keys);
		}

		return KEYS;
	}
}
