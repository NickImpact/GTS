/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.nickimpact.gts.configuration;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.nickimpact.gts.api.configuration.ConfigKey;
import com.nickimpact.gts.api.configuration.keys.*;
import com.nickimpact.gts.storage.StorageCredentials;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfigKeys {

	//------------------------------------------------------------------------------------------------------------------
	// General config settings
	//------------------------------------------------------------------------------------------------------------------

	/** Default time = 1 minute = 60 seconds */
	public static final ConfigKey<Integer> AUC_TIME = IntegerKey.of("auctions.default-time", 60);

	/** Max time = 5 minutes = 300 seconds */
	public static final ConfigKey<Integer> AUC_MAX_TIME = IntegerKey.of("auctions.max-time", 300);

	public static final ConfigKey<Boolean> BID_KEEP_UI_OPEN = BooleanKey.of("auctions.keep-ui-open-on-bid", false);

	/** Default time = 1 hour = 60 minutes = 3600 seconds */
	public static final ConfigKey<Integer> LISTING_TIME = IntegerKey.of("listings.listing-time", 60 * 60);

	/** Max time = 12 hours = 720 minutes = 43,200 seconds */
	public static final ConfigKey<Integer> LISTING_MAX_TIME = IntegerKey.of("listings.listing-max-time", 720 * 60);

	/** The max number of listings a player can have in the GTS */
	public static final ConfigKey<Integer> MAX_LISTINGS = IntegerKey.of("listings.listings-max", 5);

	/** Whether or not taxes should be applied on listing entries */
	public static final ConfigKey<Boolean> TAX_ENABLED = BooleanKey.of("tax.enabled", false);
	public static final ConfigKey<Double> TAX_MONEY_TAX = DoubleKey.of("tax.money.tax", 0.08);

	public static final ConfigKey<Boolean> ITEMS_ENABLED = BooleanKey.of("entries.items.enabled", true);
	public static final ConfigKey<Boolean> POKEMON_ENABLED = BooleanKey.of("entries.pokemon.enabled", true);
	public static final ConfigKey<Boolean> MEMES = BooleanKey.of("entries.pokemon.memes", true);

	public static final ConfigKey<Boolean> CUSTOM_NAME_ALLOWED = BooleanKey.of("entries.items.custom-names-allowed", true);


	//------------------------------------------------------------------------------------------------------------------
	// Blacklist config settings
	//------------------------------------------------------------------------------------------------------------------
	public static final ConfigKey<List<String>> BLACKLISTED_POKEMON = ListKey.of("blacklist.pokemon", Lists.newArrayList());
	public static final ConfigKey<List<String>> BLACKLISTED_ITEMS = ListKey.of("blacklist.items", Lists.newArrayList());

	//------------------------------------------------------------------------------------------------------------------
	// Storage-based config settings
	//------------------------------------------------------------------------------------------------------------------

	/** Which storage type to use */
	public static final ConfigKey<String> STORAGE_METHOD = StringKey.of("storage.storage-method", "h2");

	/** Represents the credentials for logging into a database storage type */
	public static final ConfigKey<StorageCredentials> DATABASE_VALUES = EnduringKey.wrap(AbstractKey.of(c -> new StorageCredentials(
			c.getString("storage.data.address", null),
			c.getString("storage.data.database", null),
			c.getString("storage.data.username", null),
			c.getString("storage.data.password", null)
	)));

	/** The table prefix for the main SQL tables */
	public static final ConfigKey<String> SQL_TABLE_PREFIX = EnduringKey.wrap(StringKey.of("storage.data.table_prefix", "gts_"));
	public static final ConfigKey<Double> MIN_PRICING_POKEMON_BASE = DoubleKey.of("min-pricing.pokemon.base", 5000.0);
	public static final ConfigKey<Double> MIN_PRICING_POKEMON_IVS_PRICE = DoubleKey.of("min-pricing.pokemon.ivs.price", 5000.0);
	public static final ConfigKey<Integer> MIN_PRICING_POKEMON_IVS_MINVAL = IntegerKey.of("min-pricing.pokemon.ivs.min-iv", 28);
	public static final ConfigKey<Double> MIN_PRICING_POKEMON_LEGEND = DoubleKey.of("min-pricing.pokemon.legends", 5000.0);
	public static final ConfigKey<Double> MIN_PRICING_POKEMON_SHINY = DoubleKey.of("min-pricing.pokemon.shiny", 2500.0);
	public static final ConfigKey<Double> MIN_PRICING_POKEMON_HA = DoubleKey.of("min-pricing.pokemon.hidden-ability", 5000.0);
	public static final ConfigKey<Boolean> MIN_PRICING_ENABLED = BooleanKey.of("min-pricing.enabled", true);
	public static final ConfigKey<Double> MAX_MONEY_PRICE = DoubleKey.of("max-pricing.money.max", 100000000.0);

	private static Map<String, ConfigKey<?>> KEYS = null;

	public static synchronized Map<String, ConfigKey<?>> getAllKeys() {
		if(KEYS == null) {
			Map<String, ConfigKey<?>> keys = new LinkedHashMap<>();

			try {
				Field[] values = ConfigKeys.class.getFields();
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
