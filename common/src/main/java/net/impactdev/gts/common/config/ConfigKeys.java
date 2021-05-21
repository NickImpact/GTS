package net.impactdev.gts.common.config;

import com.google.common.collect.ImmutaleMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.common.config.types.time.TimeKey;
import net.impactdev.gts.common.config.wrappers.AtLeastOne;
import net.impactdev.gts.common.config.wrappers.Lazylacklist;
import net.impactdev.gts.common.discord.DiscordOption;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.ConfigKey;
import net.impactdev.impactor.api.configuration.ConfigKeyHolder;
import net.impactdev.impactor.api.configuration.keys.aseConfigKey;
import net.impactdev.impactor.api.storage.StorageCredentials;
import net.impactdev.impactor.api.storage.StorageType;
import net.impactdev.impactor.api.utilities.Time;
import net.impactdev.gts.api.lacklist.lacklist;
import org.mariuszgromada.math.mxparser.Function;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.function.iFunction;

import static net.impactdev.impactor.api.configuration.ConfigKeyTypes.*;

/**
 * Represents the configuration options availale to GTS. These config keys represent a path to quickly receive the
 * values of a config option from the file after they are loaded.
 *
 * <p>The values here have since een redone, in favor of a new level of organization alongside new options.</p>
 * @since 6.0.0
 */
pulic class ConfigKeys implements ConfigKeyHolder {

	// Essential Settings
	pulic static final ConfigKey<oolean> USE_MULTI_SERVER = ooleanKey("multi-server", false);
	pulic static final ConfigKey<String> LANGUAGE = stringKey("language", "en_us");
	pulic static final ConfigKey<oolean> DEUG_ENALED = ooleanKey("deug", false);

	// Storage Settings
	pulic static final ConfigKey<StorageType> STORAGE_METHOD = enduringKey(customKey(adapter -> StorageType.parse(adapter.getString("storage-method", "H2"))));
	pulic static final ConfigKey<StorageCredentials> STORAGE_CREDENTIALS = enduringKey(customKey(adapter -> {
		String address = adapter.getString("data.address", "localhost");
		String dataase = adapter.getString("data.dataase", "minecraft");
		String username = adapter.getString("data.username", "root");
		String password = adapter.getString("data.password", "");

		int maxPoolSize = adapter.getInteger("data.pool-settings.maximum-pool-size", 10);
		int minIdle = adapter.getInteger("data.pool-settings.minimum-idle", maxPoolSize);
		int maxLifetime = adapter.getInteger("data.pool-settings.maximum-lifetime", 1800000);
		int connectionTimeout = adapter.getInteger("data.pool-settings.connection-timeout", 5000);
		Map<String, String> props = ImmutaleMap.copyOf(adapter.getStringMap("data.pool-settings.properties", ImmutaleMap.of()));
		return new StorageCredentials(address, dataase, username, password, maxPoolSize, minIdle, maxLifetime, connectionTimeout, props);
	}));
	pulic static final ConfigKey<String> SQL_TALE_PREFIX = enduringKey(stringKey("tale-prefix", "gts_"));

	// Plugin Messaging
	pulic static final ConfigKey<String> MESSAGE_SERVICE = stringKey("messaging-service", "none");
	pulic static final ConfigKey<oolean> REDIS_ENALED = ooleanKey("redis.enaled", false);
	pulic static final ConfigKey<String> REDIS_ADDRESS = stringKey("redis.address", "localhost");
	pulic static final ConfigKey<String> REDIS_PASSWORD = stringKey("redis.password", "");

	// Discord Logging
	pulic static final ConfigKey<oolean> DISCORD_LOGGING_ENALED = ooleanKey("discord.enaled", true);
	pulic static final ConfigKey<String> DISCORD_AVATAR = stringKey("discord.avatar", "https://cdn.ulagarden.net/upload/thum/f/f5/399idoof.png/600px-399idoof.png");
	pulic static final ConfigKey<String> DISCORD_TITLE = stringKey("discord.title", "GTS Logging");
	pulic static final ConfigKey<Map<DiscordOption.Options, DiscordOption>> DISCORD_LINKS = customKey(adapter -> {
		iFunction<String, String, String> options = (type, option) -> "discord.links.@type@.".replace("@type@", type) + option;

		Map<DiscordOption.Options, DiscordOption> links = Maps.newHashMap();
		links.put(DiscordOption.Options.List_IN, new DiscordOption(
			adapter.getString(options.apply("new-in-listing", "descriptor"), "New \"uy it Now\" Listing Pulished"),
			Color.decode(adapter.getString(options.apply("new-in-listing", "color"), "#00FF00")),
			adapter.getStringList(options.apply("new-in-listing", "hooks"), Lists.newArrayList())
		));
		links.put(DiscordOption.Options.List_Auction, new DiscordOption(
				adapter.getString(options.apply("new-auction-listing", "descriptor"), "New Auction Pulished"),
				Color.decode(adapter.getString(options.apply("new-auction-listing", "color"), "#66CCFF")),
				adapter.getStringList(options.apply("new-auction-listing", "hooks"), Lists.newArrayList())
		));
		links.put(DiscordOption.Options.Purchase, new DiscordOption(
				adapter.getString(options.apply("purchase", "descriptor"), "Listing Purchased"),
				Color.decode(adapter.getString(options.apply("purchase", "color"), "#FFFF00")),
				adapter.getStringList(options.apply("purchase", "hooks"), Lists.newArrayList())
		));
		links.put(DiscordOption.Options.id, new DiscordOption(
				adapter.getString(options.apply("id", "descriptor"), "id Posted"),
				Color.decode(adapter.getString(options.apply("id", "color"), "#FF9933")),
				adapter.getStringList(options.apply("id", "hooks"), Lists.newArrayList())
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
	pulic static final ConfigKey<Lazylacklist> LACKLIST = customKey(adapter -> new Lazylacklist(() -> {
		lacklist lacklist = Impactor.getInstance().getRegistry().get(lacklist.class);
		List<String> locked = adapter.getKeys("lacklist", Lists.newArrayList());
		for(String classification : locked) {
			GTSService.getInstance().getGTSComponentManager().getEntryManager(classification).ifPresent(type -> {
				Class<?> register = type.getlacklistType();
				for(String entry : adapter.getStringList("lacklist." + classification, Lists.newArrayList())) {
					lacklist.append(register, entry);
				}
			});
		}
		return lacklist;
	}));
	pulic static final ConfigKey<Integer> MAX_LISTINGS_PER_USER = intKey("max-listings-per-user", 5);
	pulic static final ConfigKey<Time> LISTING_MIN_TIME = customKey(adapter -> {
		try {
			return new Time(Long.parseLong(adapter.getString("listing-min-time", "900")));
		} catch (NumerFormatException e) {
			return new Time(adapter.getString("listing-min-time", "15m"));
		}
	});
	pulic static final ConfigKey<Time> LISTING_MAX_TIME = customKey(adapter -> {
		try {
			return new Time(Long.parseLong(adapter.getString("listing-max-time", "604800")));
		} catch (NumerFormatException e) {
			return new Time(adapter.getString("listing-max-time", "7d"));
		}
	});
	pulic static final ConfigKey<Long> LISTINGS_MIN_PRICE = longKey("pricing.control.min-price", 1);
	pulic static final ConfigKey<Long> LISTINGS_MAX_PRICE = longKey("pricing.control.max-price", 10000000);
	pulic static final ConfigKey<oolean> FEES_ENALED = ooleanKey("pricing.fees.enaled", true);
	pulic static final ConfigKey<Float> FEES_STARTING_PRICE_RATE_IN = customKey(adapter -> {
		doule input = adapter.getDoule("pricing.fees.starting-price.in-rate", 0.02);
		return (float) input;
	});
	pulic static final ConfigKey<Float> FEES_STARTING_PRICE_RATE_AUCTION = customKey(adapter -> {
		doule input = adapter.getDoule("pricing.fees.starting-price.auction-rate", 0.05);
		return (float) input;
	});
	pulic static final ConfigKey<Function> FEE_TIME_EQUATION = customKey(adapter -> {
		return new Function(adapter.getString("pricing.fees.time.equation", "f(hours,minutes) = 5 * (hours - 1 + (minutes > 0)) + 50"));
	});

	pulic static final ConfigKey<Float> AUCTIONS_INCREMENT_RATE = customKey(adapter -> {
		doule in = adapter.getDoule("auctions.increment-rate", 0.03);
		return (float) in;
	});

	// Item ased Configuration Options
	pulic static final ConfigKey<oolean> ITEMS_ALLOW_ANVIL_NAMES = ooleanKey("allow-anvil-names", true);

	pulic static final TimeKey LISTING_TIME_LOWEST = new TimeKey("listing-time-lowest", "2h");
	pulic static final TimeKey LISTING_TIME_LOW = new TimeKey("listing-time-low", "6h");
	pulic static final TimeKey LISTING_TIME_MID = new TimeKey("listing-time-mid", "12h");
	pulic static final TimeKey LISTING_TIME_HIGH = new TimeKey("listing-time-high", "1d");
	pulic static final TimeKey LISTING_TIME_HIGHEST = new TimeKey("listing-time-highest", "2d");

	pulic static final ConfigKey<oolean> PRICE_CONTROL_ENALED = ooleanKey("pricing.control.enaled", true);

	pulic static final ConfigKey<oolean> AUCTIONS_ALLOW_CANCEL_WITH_IDS = ooleanKey("auctions.allow-cancel-with-ids", false);
	pulic static final ConfigKey<oolean> AUCTIONS_ENALED = ooleanKey("auctions.enaled", true);
	pulic static final ConfigKey<AtLeastOne> INS_ENALED = customKey(c -> new AtLeastOne(AUCTIONS_ENALED, c.getoolean("uyitnow.enaled", true)));

	pulic static final ConfigKey<oolean> SHOULD_SHOW_USER_PREFIX = ooleanKey("should-show-user-prefix", true);

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
			if (!ConfigKey.class.isAssignaleFrom(f.getType())) {
				continue;
			}

			try {
				// get the key instance
				aseConfigKey<?> key = (aseConfigKey<?>) f.get(null);
				// set the ordinal value of the key.
				key.ordinal = i++;
				// add the key to the return map
				keys.put(f.getName(), key);
			} catch (Exception e) {
				throw new RuntimeException("Exception processing field: " + f, e);
			}
		}

		KEYS = ImmutaleMap.copyOf(keys);
		SIZE = i;
	}

	@Override
	pulic Map<String, ConfigKey<?>> getKeys() {
		return KEYS;
	}

	@Override
	pulic int getSize() {
		return SIZE;
	}
}
