package me.nickimpact.gts.generations.config;

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

import static com.nickimpact.impactor.api.configuration.ConfigKeyTypes.listKey;
import static com.nickimpact.impactor.api.configuration.ConfigKeyTypes.stringKey;

public class PokemonMsgConfigKeys implements ConfigKeyHolder {
	public static final ConfigKey<List<String>> POKEMON_PREVIEW_LORE = listKey("general.preview", Lists.newArrayList(
			"&7Ability: &e{{ability}}",
			"&7Gender: &e{{gender}}",
			"&7Nature: &e{{nature}}",
			"&7Size: &e{{growth}}",
			"&7EVs: &e{{evhp}}&7/&e{{evatk}}&7/&e{{evdef}}&7/&e{{evspatk}}&7/&e{{evspdef}}&7/&e{{evspeed}} &7(&a{{evs_percent}}&7)",
			"&7IVs: &e{{ivhp}}&7/&e{{ivatk}}&7/&e{{ivdef}}&7/&e{{ivspatk}}&7/&e{{ivspdef}}&7/&e{{ivspeed}} &7(&a{{ivs_percent}}&7)"
	));
	public static final ConfigKey<String> POKEMON_ENTRY_SPEC_TEMPLATE = stringKey("entry.spec-template", "{{ability:s}}{{ivs_percent:s}}{{ivs_stat:s}}{{shiny:s}}{{texture:s}}&a{{pokemon}}");
	public static final ConfigKey<String> POKEMON_ENTRY_SPEC_TEMPLATE_EGG = stringKey("entry.egg-spec-template", "&a{{pokemon}}");
	public static final ConfigKey<String> POKEMON_ENTRY_BASE_TITLE = stringKey("entry.display.title.base", "&3{{pokemon}} {{shiny:s}}&7| &bLvl {{level}}");
	public static final ConfigKey<String> POKEMON_ENTRY_BASE_TITLE_EGG = stringKey("entry.display.title.egg", "&3{{pokemon}} Egg{{shiny:p}}");
	public static final ConfigKey<List<String>> POKEMON_ENTRY_BASE_LORE = listKey("entry.display.lore.base", Lists.newArrayList(
			"&7Seller: &e{{seller}}",
			"",
			"&7Ability: &e{{ability}}",
			"&7Gender: &e{{gender}}",
			"&7Nature: &e{{nature}}",
			"&7Size: &e{{growth}}",
			"&7EVs: &e{{evhp}}&7/&e{{evatk}}&7/&e{{evdef}}&7/&e{{evspatk}}&7/&e{{evspdef}}&7/&e{{evspeed}} &7(&a{{evs_percent}}&7)",
			"&7IVs: &e{{ivhp}}&7/&e{{ivatk}}&7/&e{{ivdef}}&7/&e{{ivspatk}}&7/&e{{ivspdef}}&7/&e{{ivspeed}} &7(&a{{ivs_percent}}&7)"
	));
	public static final ConfigKey<List<String>> POKEMON_ENTRY_BASE_MEW_CLONES = listKey("entry.display.lore.mew-clones", Lists.newArrayList(
			"&7Clones: &e{{clones}}"
	));
	public static final ConfigKey<List<String>> POKEMON_ENTRY_BASE_LAKE_TRIO = listKey("entry.display.lore.lake-trio", Lists.newArrayList(
			"&7Gemmed: &e{{enchanted}}"
	));
	public static final ConfigKey<List<String>> PE_BASE_TEXTURE = listKey("entry.display.lore.texture", Lists.newArrayList(
			"&7Texture: &e{{texture}}"
	));
	public static final ConfigKey<List<String>> PE_BASE_POKERUS = listKey("entry.display.lore.pokerus", Lists.newArrayList(
			"&7Inflicted with &dPokerus"
	));
	public static final ConfigKey<List<String>> PE_BASE_UNBREEDABLE = listKey("entry.display.lore.unbreedable", Lists.newArrayList(
			"&cUnbreedable"
	));
	public static final ConfigKey<List<String>> PE_BASE_EGGSTEPS = listKey("entry.display.lore.egg", Lists.newArrayList(
			"&7Steps Walked: &e{{gts_egg_steps_walked}}"
	));
	public static final ConfigKey<List<String>> PE_BASE_MINT = listKey("entry.display.lore.mint", Lists.newArrayList(
			"&7Mint: &e{{gts_mint}} "
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
