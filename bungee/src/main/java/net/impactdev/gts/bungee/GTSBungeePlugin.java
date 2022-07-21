package net.impactdev.gts.bungee;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.api.messaging.message.type.admin.ForceDeleteMessage;
import net.impactdev.gts.bungee.listings.BungeeAuction;
import net.impactdev.gts.bungee.listings.BungeeBIN;
import net.impactdev.gts.bungee.messaging.interpreters.BungeeAdminInterpreters;
import net.impactdev.gts.bungee.messaging.interpreters.BungeeAuctionInterpreter;
import net.impactdev.gts.bungee.messaging.interpreters.BungeeListingInterpreter;
import net.impactdev.gts.common.api.ApiRegistrationUtil;
import net.impactdev.gts.common.api.GTSAPIProvider;
import net.impactdev.gts.common.config.ConfigProvider;
import net.impactdev.gts.common.data.ResourceManagerImpl;
import net.impactdev.gts.common.dependencies.GTSDependencies;
import net.impactdev.gts.common.messaging.messages.admin.ForceDeleteMessageImpl;
import net.impactdev.gts.api.environment.Environment;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.dependencies.Dependency;
import net.impactdev.impactor.api.dependencies.ProvidedDependencies;
import net.impactdev.impactor.api.logging.PluginLogger;
import net.impactdev.impactor.api.plugin.PluginMetadata;
import net.impactdev.impactor.api.storage.StorageType;
import net.impactdev.gts.api.blacklist.Blacklist;
import net.impactdev.gts.api.extension.ExtensionManager;
import net.impactdev.gts.api.storage.GTSStorage;
import net.impactdev.gts.bungee.messaging.BungeeMessagingFactory;
import net.impactdev.gts.bungee.messaging.interpreters.BungeeBINInterpreters;
import net.impactdev.gts.bungee.messaging.interpreters.BungeePingPongInterpreter;
import net.impactdev.gts.common.blacklist.BlacklistImpl;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.messaging.InternalMessagingService;
import net.impactdev.gts.common.messaging.MessagingFactory;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.storage.StorageFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class GTSBungeePlugin implements GTSPlugin {

	private final GTSBungeeBootstrap bootstrap;

	private ConfigProvider provider;

	private GTSStorage storage;
	private InternalMessagingService messagingService;

	private Environment environment;

	public GTSBungeePlugin(GTSBungeeBootstrap bootstrap) {
		this.bootstrap = bootstrap;
		this.register();
	}

	@Override
	public <T extends GTSPlugin> T as(Class<T> type) {
		if(!type.isAssignableFrom(this.getClass())) {
			throw new RuntimeException("Invalid plugin typing");
		}
		return (T) this;
	}

	@Override
	public GTSBungeeBootstrap bootstrap() {
		return this.bootstrap;
	}

	@Override
	public ConfigProvider configuration() {
		return this.provider;
	}

	@Override
	public Environment environment() {
		Environment environment = Optional.ofNullable(this.environment)
				.orElseGet(() -> (this.environment = new Environment()));
		environment.append(this.bootstrap.proxy().getProxy().getName(), this.bootstrap.proxy().getProxy().getVersion());
		environment.append("Impactor", this.bootstrap.proxy().getProxy().getPluginManager().getPlugin("Impactor").getDescription().getVersion());
		environment.append("GTS", this.metadata().version());

		return environment;
	}

	@Override
	public Gson gson() {
		return new GsonBuilder().create();
	}

	@Override
	public GTSStorage storage() {
		return this.storage;
	}

	@Override
	public ExtensionManager extensionManager() {
		return null;
	}

	@Override
	public InternalMessagingService messagingService() {
		return this.messagingService;
	}

	@Override
	public CompletableFuture<String> playerDisplayName(UUID id) {
		return CompletableFuture.completedFuture(this.bootstrap().proxy().getProxy().getPlayer(id).getName());
	}

	public MessagingFactory<?> getMessagingFactory() {
		return new BungeeMessagingFactory(this);
	}

	@Override
	public PluginMetadata metadata() {
		return PluginMetadata.builder()
				.id("gts")
				.name("GTS")
				.version("@version@")
				.build();
	}

	@Override
	public PluginLogger logger() {
		return this.bootstrap.logger();
	}

	@Override
	public void construct() throws Exception {
		ApiRegistrationUtil.register(new GTSAPIProvider());

		Impactor.getInstance().getRegistry().register(GTSPlugin.class, this);
		Impactor.getInstance().getRegistry().register(Blacklist.class, new BlacklistImpl());

		GTSService.getInstance().getGTSComponentManager().registerListingResourceManager(BuyItNow.class, new ResourceManagerImpl<>("BIN", "N/A", BungeeBIN::deserialize));
		GTSService.getInstance().getGTSComponentManager().registerListingResourceManager(Auction.class, new ResourceManagerImpl<>("Auctions", "N/A", BungeeAuction::deserialize));
		Impactor.getInstance().getRegistry().registerBuilderSupplier(Auction.AuctionBuilder.class, BungeeAuction.BungeeAuctionBuilder::new);
		Impactor.getInstance().getRegistry().registerBuilderSupplier(BuyItNow.BuyItNowBuilder.class, BungeeBIN.BungeeBINBuilder::new);
		Impactor.getInstance().getRegistry().registerBuilderSupplier(ForceDeleteMessage.Response.ResponseBuilder.class, ForceDeleteMessageImpl.ForceDeleteResponse.ForcedDeleteResponseBuilder::new);

		Path configDirectory = this.bootstrap.configDirectory();
		this.copyResource(Paths.get("gts.conf"), configDirectory);
		Config config = Config.builder()
				.path(configDirectory.resolve("gts.conf"))
				.provider(ConfigKeys.class)
				.build();

		this.provider = new ConfigProvider(config, null);
		this.storage = new StorageFactory(this).getInstance(StorageType.MARIADB);

		this.messagingService = this.getMessagingFactory().getInstance();

		BungeePingPongInterpreter.registerDecoders(this);
		BungeePingPongInterpreter.registerInterpreters(this);
		new BungeeBINInterpreters().register(this);
		new BungeeAuctionInterpreter().register(this);
		new BungeeListingInterpreter().register(this);
		new BungeeAdminInterpreters().register(this);
	}

	@Override
	public void shutdown() throws Exception {}

	@Override
	public Set<Dependency> dependencies() {
		return Sets.newHashSet(
				ProvidedDependencies.ADVENTURE,
				ProvidedDependencies.ADVENTURE_LEGACY_SERIALIZER,
				ProvidedDependencies.ADVENTURE_GSON_SERIALIZER,
				ProvidedDependencies.CAFFEINE,
				ProvidedDependencies.CONFIGURATE_CORE,
				ProvidedDependencies.CONFIGURATE_HOCON,
				ProvidedDependencies.CONFIGURATE_GSON,
				ProvidedDependencies.CONFIGURATE_YAML,
				ProvidedDependencies.TYPESAFE_CONFIG,
				GTSDependencies.MXPARSER
		);
	}

	private void copyResource(Path path, Path destination) {
		if(!Files.exists(destination.resolve(path))) {
			try (InputStream resource = this.resource(path.toString().replace("\\", "/"))) {
				Files.createDirectories(destination.resolve(path).getParent());
				Files.copy(resource, destination.resolve(path));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
