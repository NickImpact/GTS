package me.nickimpact.gts.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.nickimpact.impactor.api.configuration.ConfigKey;
import com.nickimpact.impactor.api.configuration.ConfigKeyHolder;
import com.nickimpact.impactor.api.configuration.keys.BaseConfigKey;
import com.nickimpact.impactor.api.storage.StorageCredentials;
import me.nickimpact.gts.discord.DiscordOption;

import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.nickimpact.impactor.api.configuration.ConfigKeyTypes.*;

public class ConfigKeys implements ConfigKeyHolder {

//------------------------------------------------------------------------------------------------------------------
	// General config settings
	//------------------------------------------------------------------------------------------------------------------

	/** Default time = 1 hour = 60 minutes = 3600 seconds */
	public static final ConfigKey<Integer> LISTING_TIME = intKey("listings.listing-time", 60 * 60);

	/** Max time = 12 hours = 720 minutes = 43,200 seconds */
	public static final ConfigKey<Integer> LISTING_MAX_TIME = intKey("listings.listing-max-time", 720 * 60);

	/** The max number of listings a player can have in the GTS */
	public static final ConfigKey<Integer> MAX_LISTINGS = intKey("listings.listings-max", 5);

	/** Whether or not taxes should be applied on listing entries */
	public static final ConfigKey<Boolean> TAX_ENABLED = booleanKey("tax.enabled", false);
	public static final ConfigKey<Double> TAX_MONEY_TAX = doubleKey("tax.money.tax", 0.08);

	public static final ConfigKey<Boolean> CUSTOM_NAME_ALLOWED = booleanKey("entries.items.custom-names-allowed", true);
	public static final ConfigKey<Boolean> ITEMS_ENABLED = booleanKey("entries.items.enabled", true);

	public static final ConfigKey<Double> PRICING_LEFTCLICK_BASE = doubleKey("pricing.left-click.base", 1.0);
	public static final ConfigKey<Double> PRICING_RIGHTCLICK_BASE = doubleKey("pricing.right-click.base", 10.0);
	public static final ConfigKey<Double> PRICING_LEFTCLICK_SHIFT = doubleKey("pricing.left-click.shift", 100.0);
	public static final ConfigKey<Double> PRICING_RIGHTCLICK_SHIFT = doubleKey("pricing.right-click.shift", 1000.0);

	//------------------------------------------------------------------------------------------------------------------
	// Blacklist config settings
	//------------------------------------------------------------------------------------------------------------------
	public static final ConfigKey<List<String>> BLACKLISTED_ITEMS = listKey("blacklist.items", Lists.newArrayList());

	//------------------------------------------------------------------------------------------------------------------
	// Storage-based config settings
	//------------------------------------------------------------------------------------------------------------------

	/** Which storage type to use */
	public static final ConfigKey<String> STORAGE_METHOD = stringKey("storage.storage-method", "h2");

	/** Represents the credentials for logging into a database storage type */
	public static final ConfigKey<StorageCredentials> DATABASE_VALUES = enduringKey(customKey(c -> {
		String address = c.getString("storage.data.address", "localhost");
		String database = c.getString("storage.data.database", "gts");
		String user = c.getString("storage.data.username", "user");
		String password = c.getString("storage.data.password", "pass");
		int maxPoolSize = c.getInteger("storage.data.pool-settings.maximum-pool-size", c.getInteger("storage.data.pool-size", 10));
		int minIdle = c.getInteger("storage.data.pool-settings.minimum-idle", maxPoolSize);
		int maxLifetime = c.getInteger("storage.data.pool-settings.maximum-lifetime", 1800000);
		int connectionTimeout = c.getInteger("storage.data.pool-settings.connection-timeout", 5000);
		Map<String, String> props = ImmutableMap.copyOf(c.getStringMap("storage.data.pool-settings.properties", ImmutableMap.of()));

		return new StorageCredentials(address, database, user, password, maxPoolSize, minIdle, maxLifetime, connectionTimeout, props);
	}));

	/** The table prefix for the main SQL tables */
	public static final ConfigKey<String> SQL_TABLE_PREFIX = enduringKey(stringKey("storage.data.table_prefix", "gts_"));

	public static final ConfigKey<Boolean> MIN_PRICING_ENABLED = booleanKey("min-pricing.enabled", true);
	public static final ConfigKey<Double> MAX_MONEY_PRICE = doubleKey("max-pricing.money.max", 100000000.0);

	public static final ConfigKey<Boolean> DISCORD_ENABLED = booleanKey("discord.enabled", false);
	public static final ConfigKey<Boolean> DISCORD_DEBUG = booleanKey("discord.debug-enabled", false);
	public static final ConfigKey<String> DISCORD_TITLE = stringKey("discord.title", "GTS Notifier");
	public static final ConfigKey<String> DISCORD_AVATAR = stringKey("discord.avatar", "https://cdn.bulbagarden.net/upload/thumb/f/f5/399Bidoof.png/600px-399Bidoof.png");
	public static final ConfigKey<DiscordOption> DISCORD_NEW_LISTING = customKey(d -> new DiscordOption(
			d.getString("discord.notifications.new-listing.descriptor", "New Listing Published"),
			Color.decode(d.getString("discord.notifications.new-listing.color", "#00FF00")),
			d.getStringList("discord.notifications.new-listing.webhooks", Lists.newArrayList())
	));
	public static final ConfigKey<DiscordOption> DISCORD_SELL_LISTING = customKey(d -> new DiscordOption(
			d.getString("discord.notifications.sell-listing.descriptor", "Listing Purchase"),
			Color.decode(d.getString("discord.notifications.sell-listing.color", "#00FFFF")),
			d.getStringList("discord.notifications.sell-listing.webhooks", Lists.newArrayList())
	));
	public static final ConfigKey<DiscordOption> DISCORD_EXPIRE = customKey(d -> new DiscordOption(
			d.getString("discord.notifications.listing-expire.descriptor", "Listing Expiration"),
			Color.decode(d.getString("discord.notifications.listing-expire.color", "#FF0000")),
			d.getStringList("discord.notifications.listing-expire.webhooks", Lists.newArrayList())
	));
	public static final ConfigKey<DiscordOption> DISCORD_REMOVE = customKey(d -> new DiscordOption(
			d.getString("discord.notifications.listing-remove.descriptor", "Listing Removal"),
			Color.decode(d.getString("discord.notifications.listing-remove.color", "#800080")),
			d.getStringList("discord.notifications.listing-remove.webhooks", Lists.newArrayList())
	));

	private static final Map<String, ConfigKey<?>> KEYS;
	private static final int SIZE;

	static {
		Map<String, ConfigKey<?>> keys = new LinkedHashMap<>();
		Field[] values = ConfigKeys.class.getFields();
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
