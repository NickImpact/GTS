package me.nickimpact.gts.reforged.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.nickimpact.impactor.api.configuration.ConfigKey;
import com.nickimpact.impactor.api.configuration.ConfigKeyHolder;
import com.nickimpact.impactor.api.configuration.keys.BaseConfigKey;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.nickimpact.impactor.api.configuration.ConfigKeyTypes.*;

public class PokemonMsgConfigKeys implements ConfigKeyHolder {
	public static ConfigKey<List<String>> POKEMON_SELLER_PREVIEW = listKey("seller.preview", Lists.newArrayList(
			"&7Ability: &e{{ability}}",
			"&7Gender: &e{{gender}}",
			"&7Nature: &e{{nature}}",
			"&7Size: &e{{growth}}",
			"",
			"&7EVs: &e{{evs_total}}&7/&e510 &7(&a{{evs_percent}}&7)",
			"&7IVs: &e{{ivs_total}}&7/&e186 &7(&a{{ivs_percent}}&7)"

	));

	public static final ConfigKey<String> POKEMON_LAST_MEMBER = stringKey("entries.pokemon.last-member", "{{gts_error}} You can't list your last non-egg party member!");
	public static final ConfigKey<String> POKEMON_ENTRY_CONFIRM_TITLE_AUCTION = stringKey("entries.pokemon.confirm.title-auction", "&eBid on {{pokemon}}?");
	public static final ConfigKey<List<String>> POKEMON_ENTRY_CONFIRM_LORE_AUCTION = listKey("entries.pokemon.confirm.lore-auction", Lists.newArrayList("&7Here's some additional info:",
			"&7EVs: &e{{evs_total}}&7/&e510 &7(&a{{evs_percent}}&7)",
			"&7IVs: &e{{ivs_total}}&7/&e186 &7(&a{{ivs_percent}}&7)",
			"",
			"&7Move Set:",
			"  &7 - &e{{moves_1}}",
			"  &7 - &e{{moves_2}}",
			"  &7 - &e{{moves_3}}",
			"  &7 - &e{{moves_4}}"
	));
	public static final ConfigKey<String> POKEMON_ENTRY_SPEC_TEMPLATE = stringKey("entries.pokemon.spec-template", "{{ability:s}}{{ivs_percent:s}}{{ivs_stat:s}}{{shiny:s}}{{texture:s}}&a{{pokemon}}");
	public static final ConfigKey<String> POKEMON_ENTRY_SPEC_TEMPLATE_EGG = stringKey("entries.pokemon.egg-spec-template", "&a{{pokemon}}");
	public static final ConfigKey<String> POKEMON_ENTRY_BASE_TITLE = stringKey("entries.pokemon.base.title", "&e{{pokemon}} {{shiny:s}}&7| &bLvl {{level}}");
	public static final ConfigKey<List<String>> POKEMON_ENTRY_BASE_LORE = listKey("entries.pokemon.base.lore.base", Lists.newArrayList(
			"&7Seller: &e{{seller}}",
			"",
			"&7Ability: &e{{ability}}",
			"&7Gender: &e{{gender}}",
			"&7Nature: &e{{nature}}",
			"&7Size: &e{{growth}}"
	));
	public static final ConfigKey<List<String>> POKEMON_ENTRY_BASE_MEW_CLONES = listKey("entries.pokemon.base.lore.mew-clones", Lists.newArrayList(
			"&7Clones: &e{{clones}}"
	));
	public static final ConfigKey<List<String>> POKEMON_ENTRY_BASE_LAKE_TRIO = listKey("entries.pokemon.base.lore.lake-trio", Lists.newArrayList(
			"&7Gemmed: &e{{enchanted}}"
	));
	public static final ConfigKey<List<String>> PE_BASE_TEXTURE = listKey("entries.pokemon.base.lore.texture", Lists.newArrayList(
			"&7Texture: &e{{texture}}"
	));
	public static final ConfigKey<List<String>> PE_BASE_POKERUS = listKey("entries.pokemon.base.lore.pokerus", Lists.newArrayList(
			"&7Inflicted with &dPokerus"
	));
	public static final ConfigKey<List<String>> PE_BASE_UNBREEDABLE = listKey("entries.pokemon.base.lore.unbreedable", Lists.newArrayList(
			"&cUnbreedable"
	));

	public static final ConfigKey<String> POKEMON_ENTRY_CONFIRM_TITLE = stringKey("entries.pokemon.confirm.title", "&ePurchase {{pokemon}}?");
	public static final ConfigKey<List<String>> POKEMON_ENTRY_CONFIRM_LORE = listKey("entries.pokemon.confirm.lore", Lists.newArrayList(
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

	// -----------------------------------------------------------------------------
	// As of 1.1.3
	// -----------------------------------------------------------------------------
	public static final ConfigKey<String> SHINY_TRANSLATION = stringKey("translations.shiny", "Shiny");
	public static final ConfigKey<String> BREEDABLE_TRANSLATION = stringKey("translations.breedable", "Breedable");
	public static final ConfigKey<String> UNBREEDABLE_TRANSLATION = stringKey("translations.unbreedable", "Unbreedable");
	public static final ConfigKey<String> POKERUS_TRANSLATION = stringKey("translations.pokerus", "PKRS");

	public static final ConfigKey<String> UI_TITLES_POKEMON = stringKey("ui.selling.title", "&cGTS &7(&3Pokemon&7)");

	public static final ConfigKey<String> ERROR_EMPTY_SLOT = stringKey("general.errors.empty-slot-selection", "{{gts_error}} No pokemon was found in the specified slot...");
	public static final ConfigKey<String> ERROR_LAST_MEMBER = stringKey("general.errors.last-member", "{{gts_error}} You can't sell the last non-egg member of your party!");
	public static final ConfigKey<String> ERROR_UNTRADABLE = stringKey("general.errors.untradable", "{{gts_error}} That pokemon is marked as untradable, and cannot be sold...");
	public static final ConfigKey<String> ERROR_IN_BATTLE = stringKey("general.errors.in-battle", "{{gts_error}} You are in battle, so you can't sell any pokemon currently...");
	public static final ConfigKey<List<String>> REFERENCE_TITLES = listKey("general.reference.titles", Lists.newArrayList("Pokemon"));

	private static final Map<String, ConfigKey<?>> KEYS;
	private static final int SIZE;

	static {
		Map<String, ConfigKey<?>> keys = new LinkedHashMap<>();
		Field[] values = PokemonMsgConfigKeys.class.getFields();
		int i = 0;

		for (Field f : values) {
			// ignore non-static fields
			if (!Modifier.isStatic(f.getModifiers())) {
				continue;
			}

			// ignore fields that aren't configkeys
			if (!ConfigKey.class.equals(f.getType())) {
				continue;
			}

			try {
				// get the key instance
				BaseConfigKey<?> key = (BaseConfigKey<?>) f.get(null);
				// set the ordinal value of the key.
				key.ordinal = i++;
				// add the key to the return map
				keys.put(f.getName(), key);
			} catch (Exception e) {
				throw new RuntimeException("Exception processing field: " + f, e);
			}
		}

		KEYS = ImmutableMap.copyOf(keys);
		SIZE = i;
	}

	@Override
	public Map<String, ConfigKey<?>> getKeys() {
		return KEYS;
	}

	@Override
	public int getSize() {
		return SIZE;
	}

}
