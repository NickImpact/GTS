package me.nickimpact.gts.pixelmon.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.nickimpact.impactor.api.configuration.ConfigKey;
import com.nickimpact.impactor.api.configuration.IConfigKeys;
import com.nickimpact.impactor.api.configuration.keys.BooleanKey;
import com.nickimpact.impactor.api.configuration.keys.DoubleKey;
import com.nickimpact.impactor.api.configuration.keys.IntegerKey;
import com.nickimpact.impactor.api.configuration.keys.ListKey;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PokemonConfigKeys implements IConfigKeys {

	public static final ConfigKey<List<String>> BLACKLISTED = ListKey.of("blacklist", Lists.newArrayList());
	public static final ConfigKey<Boolean> MEMES = BooleanKey.of("memes", true);
	public static final ConfigKey<Double> MIN_PRICING_POKEMON_BASE = DoubleKey.of("min-pricing.base", 5000.0);
	public static final ConfigKey<Double> MIN_PRICING_POKEMON_IVS_PRICE = DoubleKey.of("min-pricing.ivs.price", 5000.0);
	public static final ConfigKey<Integer> MIN_PRICING_POKEMON_IVS_MINVAL = IntegerKey.of("min-pricing.ivs.min-iv", 28);
	public static final ConfigKey<Double> MIN_PRICING_POKEMON_LEGEND = DoubleKey.of("min-pricing.legends", 5000.0);
	public static final ConfigKey<Double> MIN_PRICING_POKEMON_SHINY = DoubleKey.of("min-pricing.shiny", 2500.0);
	public static final ConfigKey<Double> MIN_PRICING_POKEMON_HA = DoubleKey.of("min-pricing.hidden-ability", 5000.0);
	public static final ConfigKey<Boolean> TEXTUREFLAG_CAPITALIZE = BooleanKey.of("variables.texture.capitalize", true);
	public static final ConfigKey<Boolean> TEXTUREFLAG_TRIM_TRAILING_NUMS = BooleanKey.of("variables.texture.trim-trailing-numbers", true);

	public static final ConfigKey<Double> PRICING_LEFTCLICK_BASE = DoubleKey.of("pricing.left-click.base", 1.0);
	public static final ConfigKey<Double> PRICING_RIGHTCLICK_BASE = DoubleKey.of("pricing.right-click.base", 10.0);
	public static final ConfigKey<Double> PRICING_LEFTCLICK_SHIFT = DoubleKey.of("pricing.left-click.shift", 100.0);
	public static final ConfigKey<Double> PRICING_RIGHTCLICK_SHIFT = DoubleKey.of("pricing.right-click.shift", 1000.0);

	private static Map<String, ConfigKey<?>> KEYS = null;

	@Override
	public synchronized Map<String, ConfigKey<?>> getAllKeys() {
		if(KEYS == null) {
			Map<String, ConfigKey<?>> keys = new LinkedHashMap<>();

			try {
				Field[] values = PokemonConfigKeys.class.getFields();
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
