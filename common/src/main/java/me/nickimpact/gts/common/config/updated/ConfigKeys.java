package me.nickimpact.gts.common.config.updated;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.impactor.api.configuration.ConfigKey;
import com.nickimpact.impactor.api.storage.StorageCredentials;
import com.nickimpact.impactor.api.storage.StorageType;
import com.nickimpact.impactor.api.utilities.Time;
import me.nickimpact.gts.api.GTSService;
import me.nickimpact.gts.api.blacklist.Blacklist;
import me.nickimpact.gts.common.discord.DiscordOption;

import java.awt.Color;
import java.util.Map;
import java.util.List;
import java.util.function.BiFunction;

import static com.nickimpact.impactor.api.configuration.ConfigKeyTypes.*;

/**
 * Represents the configuration options available to GTS. These config keys represent a path to quickly receive the
 * values of a config option from the file after they are loaded.
 *
 * <p>The values here have since been redone, in favor of a new level of organization alongside new options.</p>
 * @since 6.0.0
 */
public class ConfigKeys {

	// Essential Settings
	public static final ConfigKey<Boolean> USE_MULTI_SERVER = booleanKey("multi-server", false);
	public static final ConfigKey<String> LANGUAGE = stringKey("language", "en_us");

	// Storage Settings
	public static final ConfigKey<StorageType> STORAGE_METHOD = enduringKey(customKey(adapter -> StorageType.parse(adapter.getString("storage-method", "JSON"))));
	public static final ConfigKey<StorageCredentials> STORAGE_CREDENTIALS = enduringKey(customKey(adapter -> {
		String address = adapter.getString("data.address", "localhost");
		String database = adapter.getString("data.database", "minecraft");
		String username = adapter.getString("data.username", "root");
		String password = adapter.getString("data.password", "");

		int maxPoolSize = adapter.getInteger("data.pool-settings.maximum-pool-size", 10);
		int minIdle = adapter.getInteger("data.pool-settings.minimum-idle", maxPoolSize);
		int maxLifetime = adapter.getInteger("data.pool-settings.maximum-lifetime", 1800000);
		int connectionTimeout = adapter.getInteger("data.pool-settings.connection-timeout", 5000);
		Map<String, String> props = ImmutableMap.copyOf(adapter.getStringMap("data.pool-settings.properties", ImmutableMap.of()));
		return new StorageCredentials(address, database, username, password, maxPoolSize, minIdle, maxLifetime, connectionTimeout, props);
	}));
	public static final ConfigKey<String> SQL_TABLE_PREFIX = enduringKey(stringKey("table-prefix", "gts_"));

	// Plugin Messaging
	public static final ConfigKey<String> MESSAGE_SERVICE = stringKey("messaging-service", "none");
	public static final ConfigKey<Boolean> REDIS_ENABLED = booleanKey("redis.enabled", false);
	public static final ConfigKey<String> REDIS_ADDRESS = stringKey("redis.address", "localhost");
	public static final ConfigKey<String> REDIS_PASSWORD = stringKey("redis.password", "");

	// Discord Logging
	public static final ConfigKey<Boolean> DISCORD_LOGGING_ENABLED = booleanKey("discord.enabled", false);
	public static final ConfigKey<String> DISCORD_AVATAR = stringKey("discord.avatar", "https://cdn.bulbagarden.net/upload/thumb/f/f5/399Bidoof.png/600px-399Bidoof.png");
	public static final ConfigKey<String> DISCORD_TITLE = stringKey("discord.title", "GTS Logging");
	public static final ConfigKey<Map<DiscordOption.Options, DiscordOption>> DISCORD_LINKS = customKey(adapter -> {
		BiFunction<String, String, String> options = (type, option) -> "discord.links.@type@.".replace("@type@", type) + option;

		Map<DiscordOption.Options, DiscordOption> links = Maps.newHashMap();
		links.put(DiscordOption.Options.List, new DiscordOption(
			adapter.getString(options.apply("color", "new-listing"), "#00FF00"),
			Color.decode(adapter.getString(options.apply("descriptor", "new-listing"), "New Listing Published")),
			adapter.getStringList(options.apply("hooks", "new-listing"), Lists.newArrayList())
		));
		links.put(DiscordOption.Options.Purchase, new DiscordOption(
				adapter.getString(options.apply("color", "purchase"), "#00FF00"),
				Color.decode(adapter.getString(options.apply("descriptor", "purchase"), "Listing Purchased")),
				adapter.getStringList(options.apply("hooks", "purchase"), Lists.newArrayList())
		));
		links.put(DiscordOption.Options.Bid, new DiscordOption(
				adapter.getString(options.apply("color", "bid"), "#00FF00"),
				Color.decode(adapter.getString(options.apply("descriptor", "bid"), "Bid Posted")),
				adapter.getStringList(options.apply("hooks", "bid"), Lists.newArrayList())
		));
		links.put(DiscordOption.Options.Remove, new DiscordOption(
				adapter.getString(options.apply("color", "remove"), "#00FF00"),
				Color.decode(adapter.getString(options.apply("descriptor", "remove"), "Listing Removed")),
				adapter.getStringList(options.apply("hooks", "remove"), Lists.newArrayList())
		));
		links.put(DiscordOption.Options.Expire, new DiscordOption(
				adapter.getString(options.apply("color", "expire"), "#00FF00"),
				Color.decode(adapter.getString(options.apply("descriptor", "expire"), "Listing Expired")),
				adapter.getStringList(options.apply("hooks", "expire"), Lists.newArrayList())
		));


		return links;
	});

	// Listing Management
	public static final ConfigKey<Blacklist> BLACKLIST = customKey(adapter -> {
		Blacklist blacklist = GTSService.getInstance().getRegistry().get(Blacklist.class);
		List<String> blocked = adapter.getKeys("blacklist", Lists.newArrayList());
		for(String classification : blocked) {
			GTSService.getInstance().getEntryRegistry().getForIdentifier(classification).ifPresent(base -> {
				adapter.getStringList("blacklist." + classification, Lists.newArrayList()).forEach(b -> {
					blacklist.append(base.getClassType(), b);
				});
			});
		}
		return blacklist;
	});
	public static final ConfigKey<Integer> MAX_LISTINGS_PER_USER = intKey("max-listings-per-user", 5);
	public static final ConfigKey<Time> LISTING_MIN_TIME = customKey(adapter -> {
		try {
			return new Time(Long.parseLong(adapter.getString("listing-min-time", "300")));
		} catch (NumberFormatException e) {
			return new Time(adapter.getString("listing-min-time", "5m"));
		}
	});
	public static final ConfigKey<Time> LISTING_MAX_TIME = customKey(adapter -> {
		try {
			return new Time(Long.parseLong(adapter.getString("listing-max-time", "86400")));
		} catch (NumberFormatException e) {
			return new Time(adapter.getString("listing-max-time", "1d"));
		}
	});
	public static final ConfigKey<Time> LISTING_BASE_TIME = customKey(adapter -> {
		try {
			return new Time(Long.parseLong(adapter.getString("listing-time", "3600")));
		} catch (NumberFormatException e) {
			return new Time(adapter.getString("listing-min-time", "1h"));
		}
	});
	public static final ConfigKey<Long> LISTINGS_MIN_PRICE = longKey("pricing.control.min-price", 1);
	public static final ConfigKey<Long> LISTINGS_MAX_PRICE = longKey("pricing.control.max-price", 10000000);
	public static final ConfigKey<Boolean> TAXES_ENABLED = booleanKey("pricing.taxes.enabled", false);
	public static final ConfigKey<Float> TAXES_RATE = customKey(adapter -> {
		double input = adapter.getDouble("pricing.taxes.rate", 0.08);
		return (float) input;
	});
	public static final ConfigKey<Float> AUCTIONS_INCREMENT_RATE = customKey(adapter -> {
		double in = adapter.getDouble("pricing.auctions.increment-rate", 0.1);
		return (float) in;
	});
}
