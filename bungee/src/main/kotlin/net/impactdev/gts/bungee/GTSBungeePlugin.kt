package net.impactdev.gts.bungee

import com.google.common.collect.ImmutableList
import com.google.common.collect.Lists
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import net.impactdev.gts.api.GTSService.Companion.instance
import net.impactdev.gts.api.blacklist.Blacklist
import net.impactdev.gts.api.data.Storable
import net.impactdev.gts.api.environment.Environment
import net.impactdev.gts.api.extension.ExtensionManager
import net.impactdev.gts.api.listings.auctions.Auction
import net.impactdev.gts.api.listings.auctions.Auction.AuctionBuilder
import net.impactdev.gts.api.listings.buyitnow.BuyItNow
import net.impactdev.gts.api.listings.buyitnow.BuyItNow.BuyItNowBuilder
import net.impactdev.gts.api.messaging.message.type.admin.ForceDeleteMessage
import net.impactdev.gts.api.storage.GTSStorage
import net.impactdev.gts.bungee.listings.BungeeAuction
import net.impactdev.gts.bungee.listings.BungeeAuction.BungeeAuctionBuilder
import net.impactdev.gts.bungee.listings.BungeeBIN
import net.impactdev.gts.bungee.listings.BungeeBIN.BungeeBINBuilder
import net.impactdev.gts.bungee.messaging.BungeeMessagingFactory
import net.impactdev.gts.bungee.messaging.interpreters.*
import net.impactdev.gts.common.blacklist.BlacklistImpl
import net.impactdev.gts.common.config.ConfigKeys
import net.impactdev.gts.common.data.ResourceManagerImpl
import net.impactdev.gts.common.messaging.InternalMessagingService
import net.impactdev.gts.common.messaging.MessagingFactory
import net.impactdev.gts.common.messaging.messages.admin.ForceDeleteMessageImpl
import net.impactdev.gts.common.plugin.GTSPlugin
import net.impactdev.gts.common.storage.StorageFactory
import net.impactdev.impactor.api.Impactor
import net.impactdev.impactor.api.configuration.Config
import net.impactdev.impactor.api.dependencies.Dependency
import net.impactdev.impactor.api.plugin.PluginMetadata
import net.impactdev.impactor.api.storage.StorageType
import net.impactdev.impactor.bungee.configuration.BungeeConfig
import net.impactdev.impactor.bungee.configuration.BungeeConfigAdapter
import net.impactdev.impactor.bungee.plugin.AbstractBungeePlugin
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

class GTSBungeePlugin(private val bootstrap: GTSBungeeBootstrap) : AbstractBungeePlugin(
    PluginMetadata.builder()
        .id("gts")
        .name("GTS")
        .version("@version@")
        .description("@gts_description@")
        .build(), bootstrap.logger
), GTSPlugin {
    private var config: Config? = null
    private var storage: GTSStorage? = null
    private var messagingService: InternalMessagingService? = null
    private var environment: Environment? = null
    fun enable() {
        ApiRegistrationUtil.register(GTSAPIProvider())
        Impactor.getInstance().registry.register(GTSPlugin::class.java, this)
        Impactor.getInstance().registry.register(Blacklist::class.java, BlacklistImpl())
        instance!!.gTSComponentManager!!.registerListingResourceManager(
            BuyItNow::class.java,
            ResourceManagerImpl(
                "BIN",
                "N/A",
                Storable.Deserializer<BuyItNow> { json: JsonObject -> BungeeBIN.Companion.deserialize(json) })
        )
        instance!!.gTSComponentManager!!.registerListingResourceManager(
            Auction::class.java,
            ResourceManagerImpl(
                "Auctions",
                "N/A",
                Storable.Deserializer<Auction> { `object`: JsonObject -> BungeeAuction.Companion.deserialize(`object`) })
        )
        Impactor.getInstance().registry.registerBuilderSupplier(AuctionBuilder::class.java) { BungeeAuctionBuilder() }
        Impactor.getInstance().registry.registerBuilderSupplier(BuyItNowBuilder::class.java) { BungeeBINBuilder() }
        Impactor.getInstance().registry.registerBuilderSupplier(
            ForceDeleteMessage.Response.ResponseBuilder::class.java
        ) { ForceDeleteMessageImpl.ForceDeleteResponse.ForcedDeleteResponseBuilder() }
        copyResource(Paths.get("gts.conf"), this.configDir)
        config = BungeeConfig(BungeeConfigAdapter(this, File(this.configDir.toFile(), "gts.conf")), ConfigKeys())
        storage = StorageFactory(this).getInstance(StorageType.MARIADB)
        messagingService = messagingFactory.instance
        BungeePingPongInterpreter.registerDecoders(this)
        BungeePingPongInterpreter.registerInterpreters(this)
        BungeeBINInterpreters().register(this)
        BungeeAuctionInterpreter().register(this)
        BungeeListingInterpreter().register(this)
        BungeeAdminInterpreters().register(this)
    }

    override fun <T : GTSPlugin?> `as`(type: Class<T>): T {
        if (!type.isAssignableFrom(this.javaClass)) {
            throw RuntimeException("Invalid plugin typing")
        }
        return this as T
    }

    override fun getBootstrap(): GTSBungeeBootstrap {
        return bootstrap
    }

    override fun getEnvironment(): Environment {
        val environment = Optional.ofNullable(environment)
            .orElseGet { Environment().also { environment = it } }
        environment.append(bootstrap.proxy.name, bootstrap.proxy.version)
        environment.append("Impactor", bootstrap.proxy.pluginManager.getPlugin("impactor").description.version)
        environment.append("GTS", metadata.version)
        return environment
    }

    override fun getGson(): Gson {
        return GsonBuilder().create()
    }

    override fun getStorage(): GTSStorage {
        return storage!!
    }

    override fun getExtensionManager(): ExtensionManager {
        return null
    }

    override fun getMessagingService(): InternalMessagingService {
        return messagingService!!
    }

    override fun getMultiServerCompatibleStorageOptions(): ImmutableList<StorageType> {
        return ImmutableList.builder<StorageType>()
            .add(StorageType.MYSQL, StorageType.MARIADB)
            .add(StorageType.MONGODB)
            .add(StorageType.POSTGRESQL)
            .build()
    }

    override fun getPlayerDisplayName(id: UUID): String {
        return getBootstrap().proxy.getPlayer(id).name
    }

    override fun getConfigDir(): Path {
        return bootstrap.configDirectory
    }

    override fun getConfiguration(): Config {
        return config!!
    }

    override fun getStorageRequirements(): List<StorageType> {
        return Lists.newArrayList(StorageType.MARIADB)
    }

    override fun getMsgConfig(): Config {
        return null
    }

    val messagingFactory: MessagingFactory<*>
        get() = BungeeMessagingFactory(this)

    override fun getAllDependencies(): List<Dependency> {
        return Lists.newArrayList(
            Dependency.KYORI_TEXT,
            Dependency.KYORI_TEXT_SERIALIZER_LEGACY,
            Dependency.KYORI_TEXT_SERIALIZER_GSON,
            Dependency.CAFFEINE,
            Dependency.MXPARSER
        )
    }

    private fun copyResource(path: Path, destination: Path) {
        if (!Files.exists(destination.resolve(path))) {
            try {
                getResourceStream(path.toString().replace("\\", "/")).use { resource ->
                    Files.createDirectories(destination.resolve(path).parent)
                    Files.copy(resource, destination.resolve(path))
                }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
    }
}