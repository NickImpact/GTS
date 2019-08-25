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

public class PokemonConfigKeys implements ConfigKeyHolder {

	public static ConfigKey<List<String>> BLACKLISTED_POKEMON = listKey("blacklist", Lists.newArrayList());
	public static final ConfigKey<Boolean> TEXTUREFLAG_CAPITALIZE = booleanKey("variables.texture.capitalize", true);
	public static final ConfigKey<Boolean> TEXTUREFLAG_TRIM_TRAILING_NUMS = booleanKey("variables.texture.trim-trailing-numbers", true);

	public static final ConfigKey<Double> MIN_PRICING_POKEMON_BASE = doubleKey("min-pricing.base", 5000.0);
	public static final ConfigKey<Double> MIN_PRICING_POKEMON_IVS_PRICE = doubleKey("min-pricing.ivs.price", 5000.0);
	public static final ConfigKey<Integer> MIN_PRICING_POKEMON_IVS_MINVAL = intKey("min-pricing.ivs.min-iv", 28);
	public static final ConfigKey<Double> MIN_PRICING_POKEMON_LEGEND = doubleKey("min-pricing.legends", 5000.0);
	public static final ConfigKey<Double> MIN_PRICING_POKEMON_SHINY = doubleKey("min-pricing.shiny", 2500.0);
	public static final ConfigKey<Double> MIN_PRICING_POKEMON_HA = doubleKey("min-pricing.hidden-ability", 5000.0);

	public static final ConfigKey<Double> PRICING_LEFTCLICK_BASE = doubleKey("pricing.left-click.base", 1.0);
	public static final ConfigKey<Double> PRICING_RIGHTCLICK_BASE = doubleKey("pricing.right-click.base", 10.0);
	public static final ConfigKey<Double> PRICING_LEFTCLICK_SHIFT = doubleKey("pricing.left-click.shift", 100.0);
	public static final ConfigKey<Double> PRICING_RIGHTCLICK_SHIFT = doubleKey("pricing.right-click.shift", 1000.0);

	private static final Map<String, ConfigKey<?>> KEYS;
	private static final int SIZE;

	static {
		Map<String, ConfigKey<?>> keys = new LinkedHashMap<>();
		Field[] values = PokemonConfigKeys.class.getFields();
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
