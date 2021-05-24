package net.impactdev.gts.common.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.common.config.types.time.TimeKey;
import net.impactdev.gts.common.config.wrappers.AtLeastOne;
import net.impactdev.gts.common.config.wrappers.LazyBlacklist;
import net.impactdev.gts.common.discord.DiscordOption;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.ConfigKey;
import net.impactdev.impactor.api.configuration.ConfigKeyHolder;
import net.impactdev.impactor.api.configuration.keys.BaseConfigKey;
import net.impactdev.impactor.api.storage.StorageCredentials;
import net.impactdev.impactor.api.storage.StorageType;
import net.impactdev.impactor.api.utilities.Time;
import net.impactdev.gts.api.blacklist.Blacklist;
import org.mariuszgromada.math.mxparser.Function;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.function.BiFunction;

import static net.impactdev.impactor.api.configuration.ConfigKeyTypes.*;

/**
 * Represents the configuration options available to GTS. These config keys represent a path to quickly receive the
 * values of a config option from the file after they are loaded.
 *
 * <p>The values here have since been redone, in favor of a new level of organization alongside new options.</p>
 * @since 6.0.0
 */
public class ConfigKeys implements ConfigKeyHolder {

	// Essential Settings
	public static final ConfigKey<Boolean> USE_MULTI_SERVER = booleanKey("multi-server", false);
	public static final ConfigKey<String> LANGUAGE = stringKey("language", "en_us");
	public static final ConfigKey<Boolean> DEBUG_ENABLED = booleanKey("debug", false);

	// Storage Settings
	public static final ConfigKey<StorageType> STORAGE_METHOD = enduringKey(customKey(adapter -> StorageType.parse(adapter.getString("storage-method", "H2"))));
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
	public static final ConfigKey<Boolean> DISCORD_LOGGING_ENABLED = booleanKey("discord.enabled", true);
	public static final ConfigKey<String> DISCORD_AVATAR = stringKey("discord.avatar", "https://cdn.bulbagarden.net/upload/thumb/f/f5/399Bidoof.png/600px-399Bidoof.png");
	public static final ConfigKey<String> DISCORD_TITLE = stringKey("discord.title", "GTS Logging");
	public static final ConfigKey<Map<DiscordOption.Options, DiscordOption>> DISCORD_LINKS = customKey(adapter -> {
		BiFunction<String, String, String> options = (type, option) -> "discord.links.@type@.".replace("@type@", type) + option;

		Map<DiscordOption.Options, DiscordOption> links = Maps.newHashMap();
		links.put(DiscordOption.Options.List_BIN, new DiscordOption(
			adapter.getString(options.apply("new-bin-listing", "descriptor"), "New \"Buy it Now\" Listing Published"),
			Color.decode(adapter.getString(options.apply("new-bin-listing", "color"), "#00FF00")),
			adapter.getStringList(options.apply("new-bin-listing", "hooks"), Lists.newArrayList())
		));
		links.put(DiscordOption.Options.List_Auction, new DiscordOption(
				adapter.getString(options.apply("new-auction-listing", "descriptor"), "New Auction Published"),
				Color.decode(adapter.getString(options.apply("new-auction-listing", "color"), "#66CCFF")),
				adapter.getStringList(options.apply("new-auction-listing", "hooks"), Lists.newArrayList())
		));
		links.put(DiscordOption.Options.Purchase, new DiscordOption(
				adapter.getString(options.apply("purchase", "descriptor"), "Listing Purchased"),
				Color.decode(adapter.getString(options.apply("purchase", "color"), "#FFFF00")),
				adapter.getStringList(options.apply("purchase", "hooks"), Lists.newArrayList())
		));
		links.put(DiscordOption.Options.Bid, new DiscordOption(
				adapter.getString(options.apply("bid", "descriptor"), "Bid Posted"),
				Color.decode(adapter.getString(options.apply("bid", "color"), "#FF9933")),
				adapter.getStringList(options.apply("bid", "hooks"), Lists.newArrayList())
		));
		links.put(DiscordOption.Options.Remove, new DiscordOption(
				adapter.getString(options.apply("remove", "descriptor"), "Listing Removed"),
				Color.decode(adapter.getString(options.apply("remove", "color"), "#FF0000")),
				adapter.getStringList(options.apply("remove", "hooks"), Lists.newArrayList())
		));
		links.put(DiscordOption.Options.Claim, new DiscordOption(
				adapter.getString(options.apply("claim", "descriptor"), "Listing Removed"),
				Color.decode(adapter.getString(options.apply("claim", "color"), "#CC00FF")),
				adapter.getStringList(options.apply("claim", "hooks"), Lists.newArrayList())
		));

		return links;
	});

	// Listing Management
	public static final ConfigKey<LazyBlacklist> BLACKLIST = customKey(adapter -> new LazyBlacklist(() -> {
		Blacklist blacklist = Impactor.getInstance().getRegistry().get(Blacklist.class);
		List<String> blocked = adapter.getKeys("blacklist", Lists.newArrayList());
		for(String classification : blocked) {
			GTSService.getInstance().getGTSComponentManager().getEntryManager(classification).ifPresent(type -> {
				Class<?> register = type.getBlacklistType();
				for(String entry : adapter.getStringList("blacklist." + classification, Lists.newArrayList())) {
					blacklist.append(register, entry);
				}
			});
		}
		return blacklist;
	}));
	public static final ConfigKey<Integer> MAX_LISTINGS_PER_USER = intKey("max-listings-per-user", 5);
	public static final ConfigKey<Time> LISTING_MIN_TIME = customKey(adapter -> {
		try {
			return new Time(Long.parseLong(adapter.getString("listing-min-time", "900")));
		} catch (NumberFormatException e) {
			return new Time(adapter.getString("listing-min-time", "15m"));
		}
	});
	public static final ConfigKey<Time> LISTING_MAX_TIME = customKey(adapter -> {
		try {
			return new Time(Long.parseLong(adapter.getString("listing-max-time", "604800")));
		} catch (NumberFormatException e) {
			return new Time(adapter.getString("listing-max-time", "7d"));
		}
	});
	public static final ConfigKey<Long> LISTINGS_MIN_PRICE = longKey("pricing.control.min-price", 1);
	public static final ConfigKey<Long> LISTINGS_MAX_PRICE = longKey("pricing.control.max-price", 10000000);
	public static final ConfigKey<Boolean> FEES_ENABLED = booleanKey("pricing.fees.enabled", true);
	public static final ConfigKey<Float> FEES_STARTING_PRICE_RATE_BIN = customKey(adapter -> {
		double input = adapter.getDouble("pricing.fees.starting-price.bin-rate", 0.02);
		return (float) input;
	});
	public static final ConfigKey<Float> FEES_STARTING_PRICE_RATE_AUCTION = customKey(adapter -> {
		double input = adapter.getDouble("pricing.fees.starting-price.auction-rate", 0.05);
		return (float) input;
	});
	public static final ConfigKey<Function> FEE_TIME_EQUATION = customKey(adapter -> {
		return new Function(adapter.getString("pricing.fees.time.equation", "f(hours,minutes) = 5 * (hours - 1 + (minutes > 0)) + 50"));
	});

	public static final ConfigKey<Float> AUCTIONS_INCREMENT_RATE = customKey(adapter -> {
		double in = adapter.getDouble("auctions.increment-rate", 0.03);
		return (float) in;
	});

	// Item Based Configuration Options
	public static final ConfigKey<Boolean> ITEMS_ALLOW_ANVIL_NAMES = booleanKey("allow-anvil-names", true);

	public static final TimeKey LISTING_TIME_LOWEST = new TimeKey("listing-time-lowest", "2h");
	public static final TimeKey LISTING_TIME_LOW = new TimeKey("listing-time-low", "6h");
	public static final TimeKey LISTING_TIME_MID = new TimeKey("listing-time-mid", "12h");
	public static final TimeKey LISTING_TIME_HIGH = new TimeKey("listing-time-high", "1d");
	public static final TimeKey LISTING_TIME_HIGHEST = new TimeKey("listing-time-highest", "2d");

	public static final ConfigKey<Boolean> PRICE_CONTROL_ENABLED = booleanKey("pricing.control.enabled", true);

	public static final ConfigKey<Boolean> AUCTIONS_ALLOW_CANCEL_WITH_BIDS = booleanKey("auctions.allow-cancel-with-bids", false);
	public static final ConfigKey<Boolean> AUCTIONS_ENABLED = booleanKey("auctions.enabled", true);
	public static final ConfigKey<AtLeastOne> BINS_ENABLED = customKey(c -> new AtLeastOne(AUCTIONS_ENABLED, c.getBoolean("buyitnow.enabled", true)));
	public static final ConfigKey<Boolean> AUCTIONS_SNIPING_BIDS_ENABLED = booleanKey("auctions.bid-sniping.enabled", false);
	public static final ConfigKey<Time> AUCTIONS_MINIMUM_SNIPING_TIME = customKey(adapter -> {
		try {
			return new Time(Long.parseLong(adapter.getString("auctions.bid-sniping.minimum-time", "30")));
		} catch (NumberFormatException e) {
			return new Time(adapter.getString("auctions.bid-sniping.minimum-time", "30s"));
		}
	});
	public static final ConfigKey<Time> AUCTIONS_SET_TIME = customKey(adapter -> {
		try {
			return new Time(Long.parseLong(adapter.getString("auctions.bid-sniping.set-time", "60")));
		} catch (NumberFormatException e) {
			return new Time(adapter.getString("auctions.bid-sniping.listing-set-time", "1m"));
		}
	});
	public static final ConfigKey<Boolean> SHOULD_SHOW_USER_PREFIX = booleanKey("should-show-user-prefix", true);

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
			if (!ConfigKey.class.isAssignableFrom(f.getType())) {
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
