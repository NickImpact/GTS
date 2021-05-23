package net.impactdev.gts.common.config

import com.google.common.collect.ImmutableMap
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import net.impactdev.gts.api.GTSService
import net.impactdev.gts.api.blacklist.Blacklist
import net.impactdev.gts.api.listings.entries.Entry
import net.impactdev.gts.api.listings.entries.EntryManager
import net.impactdev.gts.common.config.types.time.TimeKey
import net.impactdev.gts.common.config.wrappers.AtLeastOne
import net.impactdev.gts.common.config.wrappers.LazyBlacklist
import net.impactdev.gts.common.discord.DiscordOption
import net.impactdev.impactor.api.Impactor
import net.impactdev.impactor.api.configuration.ConfigKey
import net.impactdev.impactor.api.configuration.ConfigKeyHolder
import net.impactdev.impactor.api.configuration.ConfigKeyTypes
import net.impactdev.impactor.api.configuration.ConfigurationAdapter
import net.impactdev.impactor.api.configuration.keys.BaseConfigKey
import net.impactdev.impactor.api.storage.StorageCredentials
import net.impactdev.impactor.api.storage.StorageType
import net.impactdev.impactor.api.utilities.Time
import org.mariuszgromada.math.mxparser.Function
import java.awt.Color
import java.lang.reflect.Modifier
import java.util.*
import java.util.function.BiFunction

/**
 * Represents the configuration options available to GTS. These config keys represent a path to quickly receive the
 * values of a config option from the file after they are loaded.
 *
 *
 * The values here have since been redone, in favor of a new level of organization alongside new options.
 * @since 6.0.0
 */
class ConfigKeys : ConfigKeyHolder {
    companion object {
        // Essential Settings
		@JvmField
		val USE_MULTI_SERVER: ConfigKey<Boolean> = ConfigKeyTypes.booleanKey("multi-server", false)
        @JvmField
		val LANGUAGE: ConfigKey<String> = ConfigKeyTypes.stringKey("language", "en_us")
        @JvmField
		val DEBUG_ENABLED: ConfigKey<Boolean> = ConfigKeyTypes.booleanKey("debug", false)

        // Storage Settings
		@JvmField
		val STORAGE_METHOD: ConfigKey<StorageType> =
            ConfigKeyTypes.enduringKey(ConfigKeyTypes.customKey { adapter: ConfigurationAdapter ->
                StorageType.parse(adapter.getString("storage-method", "H2"))
            })
        @JvmField
		val STORAGE_CREDENTIALS: ConfigKey<StorageCredentials> =
            ConfigKeyTypes.enduringKey(ConfigKeyTypes.customKey { adapter: ConfigurationAdapter ->
                val address = adapter.getString("data.address", "localhost")
                val database = adapter.getString("data.database", "minecraft")
                val username = adapter.getString("data.username", "root")
                val password = adapter.getString("data.password", "")
                val maxPoolSize = adapter.getInteger("data.pool-settings.maximum-pool-size", 10)
                val minIdle = adapter.getInteger("data.pool-settings.minimum-idle", maxPoolSize)
                val maxLifetime = adapter.getInteger("data.pool-settings.maximum-lifetime", 1800000)
                val connectionTimeout = adapter.getInteger("data.pool-settings.connection-timeout", 5000)
                val props: Map<String, String> =
                    ImmutableMap.copyOf(adapter.getStringMap("data.pool-settings.properties", ImmutableMap.of()))
                StorageCredentials(
                    address,
                    database,
                    username,
                    password,
                    maxPoolSize,
                    minIdle,
                    maxLifetime,
                    connectionTimeout,
                    props
                )
            })
        @JvmField
		val SQL_TABLE_PREFIX: ConfigKey<String> =
            ConfigKeyTypes.enduringKey(ConfigKeyTypes.stringKey("table-prefix", "gts_"))

        // Plugin Messaging
		@JvmField
		val MESSAGE_SERVICE: ConfigKey<String> = ConfigKeyTypes.stringKey("messaging-service", "none")
        @JvmField
		val REDIS_ENABLED: ConfigKey<Boolean> = ConfigKeyTypes.booleanKey("redis.enabled", false)
        @JvmField
		val REDIS_ADDRESS: ConfigKey<String> = ConfigKeyTypes.stringKey("redis.address", "localhost")
        @JvmField
		val REDIS_PASSWORD: ConfigKey<String> = ConfigKeyTypes.stringKey("redis.password", "")

        // Discord Logging
		@JvmField
		val DISCORD_LOGGING_ENABLED: ConfigKey<Boolean> = ConfigKeyTypes.booleanKey("discord.enabled", true)
        @JvmField
		val DISCORD_AVATAR: ConfigKey<String> = ConfigKeyTypes.stringKey(
            "discord.avatar",
            "https://cdn.bulbagarden.net/upload/thumb/f/f5/399Bidoof.png/600px-399Bidoof.png"
        )
        @JvmField
		val DISCORD_TITLE: ConfigKey<String> = ConfigKeyTypes.stringKey("discord.title", "GTS Logging")
        @JvmField
		val DISCORD_LINKS: ConfigKey<Map<DiscordOption.Options, DiscordOption>> =
            ConfigKeyTypes.customKey { adapter: ConfigurationAdapter ->
                val options = BiFunction { type: String?, option: String ->
                    "discord.links.@type@.".replace(
                        "@type@",
                        type!!
                    ) + option
                }
                val links: MutableMap<DiscordOption.Options, DiscordOption> = Maps.newHashMap()
                links[DiscordOption.Options.List_BIN] = DiscordOption(
                    adapter.getString(
                        options.apply("new-bin-listing", "descriptor"),
                        "New \"Buy it Now\" Listing Published"
                    ),
                    Color.decode(adapter.getString(options.apply("new-bin-listing", "color"), "#00FF00")),
                    adapter.getStringList(options.apply("new-bin-listing", "hooks"), Lists.newArrayList())
                )
                links[DiscordOption.Options.List_Auction] = DiscordOption(
                    adapter.getString(options.apply("new-auction-listing", "descriptor"), "New Auction Published"),
                    Color.decode(adapter.getString(options.apply("new-auction-listing", "color"), "#66CCFF")),
                    adapter.getStringList(options.apply("new-auction-listing", "hooks"), Lists.newArrayList())
                )
                links[DiscordOption.Options.Purchase] = DiscordOption(
                    adapter.getString(options.apply("purchase", "descriptor"), "Listing Purchased"),
                    Color.decode(adapter.getString(options.apply("purchase", "color"), "#FFFF00")),
                    adapter.getStringList(options.apply("purchase", "hooks"), Lists.newArrayList())
                )
                links[DiscordOption.Options.Bid] = DiscordOption(
                    adapter.getString(options.apply("bid", "descriptor"), "Bid Posted"),
                    Color.decode(adapter.getString(options.apply("bid", "color"), "#FF9933")),
                    adapter.getStringList(options.apply("bid", "hooks"), Lists.newArrayList())
                )
                links[DiscordOption.Options.Remove] = DiscordOption(
                    adapter.getString(options.apply("remove", "descriptor"), "Listing Removed"),
                    Color.decode(adapter.getString(options.apply("remove", "color"), "#FF0000")),
                    adapter.getStringList(options.apply("remove", "hooks"), Lists.newArrayList())
                )
                links[DiscordOption.Options.Claim] = DiscordOption(
                    adapter.getString(options.apply("claim", "descriptor"), "Listing Removed"),
                    Color.decode(adapter.getString(options.apply("claim", "color"), "#CC00FF")),
                    adapter.getStringList(options.apply("claim", "hooks"), Lists.newArrayList())
                )
                links
            }

        // Listing Management
		@JvmField
		val BLACKLIST: ConfigKey<LazyBlacklist> = ConfigKeyTypes.customKey { adapter: ConfigurationAdapter ->
            LazyBlacklist {
                val blacklist = Impactor.getInstance().registry.get(Blacklist::class.java)
                val blocked = adapter.getKeys("blacklist", Lists.newArrayList())
                for (classification in blocked) {
                    GTSService.instance.gtsComponentManager.getEntryManager<Entry<*, *>?>(classification)
                        .ifPresent { type: EntryManager<Entry<*, *>?, *> ->
                            val register = type.blacklistType
                            for (entry in adapter.getStringList("blacklist.$classification", Lists.newArrayList())) {
                                blacklist.append(register, entry)
                            }
                        }
                }
                blacklist
            }
        }
        @JvmField
		val MAX_LISTINGS_PER_USER: ConfigKey<Int> = ConfigKeyTypes.intKey("max-listings-per-user", 5)
        @JvmField
		val LISTING_MIN_TIME: ConfigKey<Time> = ConfigKeyTypes.customKey { adapter: ConfigurationAdapter ->
            try {
                return@customKey Time(adapter.getString("listing-min-time", "900").toLong())
            } catch (e: NumberFormatException) {
                return@customKey Time(adapter.getString("listing-min-time", "15m"))
            }
        }
        @JvmField
		val LISTING_MAX_TIME: ConfigKey<Time> = ConfigKeyTypes.customKey { adapter: ConfigurationAdapter ->
            try {
                return@customKey Time(adapter.getString("listing-max-time", "604800").toLong())
            } catch (e: NumberFormatException) {
                return@customKey Time(adapter.getString("listing-max-time", "7d"))
            }
        }
        @JvmField
		val LISTINGS_MIN_PRICE: ConfigKey<Long> = ConfigKeyTypes.longKey("pricing.control.min-price", 1)
        @JvmField
		val LISTINGS_MAX_PRICE: ConfigKey<Long> = ConfigKeyTypes.longKey("pricing.control.max-price", 10000000)
        @JvmField
		val FEES_ENABLED: ConfigKey<Boolean> = ConfigKeyTypes.booleanKey("pricing.fees.enabled", true)
        @JvmField
		val FEES_STARTING_PRICE_RATE_BIN: ConfigKey<Float> = ConfigKeyTypes.customKey { adapter: ConfigurationAdapter ->
            val input = adapter.getDouble("pricing.fees.starting-price.bin-rate", 0.02)
            input.toFloat()
        }
        @JvmField
		val FEES_STARTING_PRICE_RATE_AUCTION: ConfigKey<Float> =
            ConfigKeyTypes.customKey { adapter: ConfigurationAdapter ->
                val input = adapter.getDouble("pricing.fees.starting-price.auction-rate", 0.05)
                input.toFloat()
            }
        @JvmField
		val FEE_TIME_EQUATION: ConfigKey<Function> = ConfigKeyTypes.customKey { adapter: ConfigurationAdapter ->
            Function(
                adapter.getString(
                    "pricing.fees.time.equation",
                    "f(hours,minutes) = 5 * (hours - 1 + (minutes > 0)) + 50"
                )
            )
        }
        @JvmField
		val AUCTIONS_INCREMENT_RATE: ConfigKey<Float> = ConfigKeyTypes.customKey { adapter: ConfigurationAdapter ->
            val `in` = adapter.getDouble("auctions.increment-rate", 0.03)
            `in`.toFloat()
        }

        // Item Based Configuration Options
		@JvmField
		val ITEMS_ALLOW_ANVIL_NAMES: ConfigKey<Boolean> = ConfigKeyTypes.booleanKey("allow-anvil-names", true)
        @JvmField
		val LISTING_TIME_LOWEST = TimeKey("listing-time-lowest", "2h")
        @JvmField
		val LISTING_TIME_LOW = TimeKey("listing-time-low", "6h")
        @JvmField
		val LISTING_TIME_MID = TimeKey("listing-time-mid", "12h")
        @JvmField
		val LISTING_TIME_HIGH = TimeKey("listing-time-high", "1d")
        @JvmField
		val LISTING_TIME_HIGHEST = TimeKey("listing-time-highest", "2d")
        @JvmField
		val PRICE_CONTROL_ENABLED: ConfigKey<Boolean> = ConfigKeyTypes.booleanKey("pricing.control.enabled", true)
        @JvmField
		val AUCTIONS_ALLOW_CANCEL_WITH_BIDS: ConfigKey<Boolean> =
            ConfigKeyTypes.booleanKey("auctions.allow-cancel-with-bids", false)
        @JvmField
		val AUCTIONS_ENABLED: ConfigKey<Boolean> = ConfigKeyTypes.booleanKey("auctions.enabled", true)
        @JvmField
		val BINS_ENABLED: ConfigKey<AtLeastOne> = ConfigKeyTypes.customKey { c: ConfigurationAdapter ->
            AtLeastOne(
                AUCTIONS_ENABLED, c.getBoolean("buyitnow.enabled", true)
            )
        }
        @JvmField
		val SHOULD_SHOW_USER_PREFIX: ConfigKey<Boolean> = ConfigKeyTypes.booleanKey("should-show-user-prefix", true)
        private val KEYS: Map<String, ConfigKey<*>>? = null
        private const val SIZE = 0

        init {
            val keys: Map<String, ConfigKey<*>> = LinkedHashMap()
            val values = ConfigKeys::class.java.fields
            val i = 0
            for (f in net.impactdev.gts.common.config.values) {
                // ignore non-static fields
                if (!Modifier.isStatic(net.impactdev.gts.common.config.f.getModifiers())) {
                    continue
                }

                // ignore fields that aren't configkeys
                if (!ConfigKey::class.java.isAssignableFrom(net.impactdev.gts.common.config.f.getType())) {
                    continue
                }
                try {
                    // get the key instance
                    val key = net.impactdev.gts.common.config.f.get(null) as BaseConfigKey<*>
                    // set the ordinal value of the key.
                    net.impactdev.gts.common.config.key.ordinal = net.impactdev.gts.common.config.i++
                    // add the key to the return map
                    net.impactdev.gts.common.config.keys.put(
                        net.impactdev.gts.common.config.f.getName(),
                        net.impactdev.gts.common.config.key
                    )
                } catch (e: Exception) {
                    throw RuntimeException("Exception processing field: " + net.impactdev.gts.common.config.f, e)
                }
            }
            KEYS = ImmutableMap.copyOf<String, ConfigKey<*>>(net.impactdev.gts.common.config.keys)
            SIZE = net.impactdev.gts.common.config.i
        }
    }

    override fun getKeys(): Map<String, ConfigKey<*>>? {
        return KEYS
    }

    override fun getSize(): Int {
        return SIZE
    }
}