package net.impactdev.gts.bungee;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
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
import net.impactdev.gts.common.data.ResourceManagerImpl;
import net.impactdev.gts.common.messaging.messages.admin.ForceDeleteMessageImpl;
import net.impactdev.gts.common.plugin.Environment;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.dependencies.Dependency;
import net.impactdev.impactor.api.plugin.PluginMetadata;
import net.impactdev.impactor.api.plugin.registry.PluginRegistry;
import net.impactdev.impactor.api.storage.StorageType;
import net.impactdev.impactor.bungee.configuration.BungeeConfig;
import net.impactdev.impactor.bungee.configuration.BungeeConfigAdapter;
import net.impactdev.impactor.bungee.plugin.AbstractBungeePlugin;
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

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GTSBungeePlugin extends AbstractBungeePlugin implements GTSPlugin {

	private final GTSBungeeBootstrap bootstrap;

	private Config config;

	private GTSStorage storage;
	private InternalMessagingService messagingService;

	private Environment environment;

	public GTSBungeePlugin(GTSBungeeBootstrap bootstrap) {
		super(PluginMetadata.builder()
				.id("gts")
				.name("GTS")
				.version("@version@")
				.description("@gts_description@")
				.build(), bootstrap.getLogger()
		);
		this.bootstrap = bootstrap;
	}

	public void enable() {
		ApiRegistrationUtil.register(new GTSAPIProvider());

		Impactor.getInstance().getRegistry().register(GTSPlugin.class, this);
		Impactor.getInstance().getRegistry().register(Blacklist.class, new BlacklistImpl());

		GTSService.getInstance().getGTSComponentManager().registerListingResourceManager(BuyItNow.class, new ResourceManagerImpl<>("BIN", "N/A", BungeeBIN::deserialize));
		GTSService.getInstance().getGTSComponentManager().registerListingResourceManager(Auction.class, new ResourceManagerImpl<>("Auctions", "N/A", BungeeAuction::deserialize));
		Impactor.getInstance().getRegistry().registerBuilderSupplier(Auction.AuctionBuilder.class, BungeeAuction.BungeeAuctionBuilder::new);
		Impactor.getInstance().getRegistry().registerBuilderSupplier(BuyItNow.BuyItNowBuilder.class, BungeeBIN.BungeeBINBuilder::new);
		Impactor.getInstance().getRegistry().registerBuilderSupplier(ForceDeleteMessage.Response.ResponseBuilder.class, ForceDeleteMessageImpl.ForceDeleteResponse.ForcedDeleteResponseBuilder::new);

		this.config = new BungeeConfig(new BungeeConfigAdapter(this, new File(this.getConfigDir().toFile(), "main.conf")), new ConfigKeys());
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
	public <T extends GTSPlugin> T as(Class<T> type) {
		if(!type.isAssignableFrom(this.getClass())) {
			throw new RuntimeException("Invalid plugin typing");
		}
		return (T) this;
	}

	@Override
	public GTSBungeeBootstrap getBootstrap() {
		return this.bootstrap;
	}

	@Override
	public Environment getEnvironment() {
		Environment environment = Optional.ofNullable(this.environment)
				.orElseGet(() -> (this.environment = new Environment()));
		environment.append(this.bootstrap.getProxy().getName(), this.bootstrap.getProxy().getVersion());
		environment.append("Impactor", this.bootstrap.getProxy().getPluginManager().getPlugin("impactor").getDescription().getVersion());
		environment.append("GTS", this.getMetadata().getVersion());

		return environment;
	}

	@Override
	public Gson getGson() {
		return new GsonBuilder().create();
	}

	@Override
	public GTSStorage getStorage() {
		return this.storage;
	}

	@Override
	public ExtensionManager getExtensionManager() {
		return null;
	}

	@Override
	public InternalMessagingService getMessagingService() {
		return this.messagingService;
	}

	@Override
	public ImmutableList<StorageType> getMultiServerCompatibleStorageOptions() {
		return ImmutableList.<StorageType>builder().build();
	}

	@Override
	public String getPlayerDisplayName(UUID id) {
		return this.getBootstrap().getProxy().getPlayer(id).getName();
	}

	@Override
	public Path getConfigDir() {
		return this.bootstrap.getConfigDirectory();
	}

	@Override
	public Config getConfiguration() {
		return this.config;
	}

	@Override
	public List<StorageType> getStorageRequirements() {
		return Lists.newArrayList(StorageType.MARIADB);
	}

	@Override
	public Config getMsgConfig() {
		return null;
	}

	public MessagingFactory<?> getMessagingFactory() {
		return new BungeeMessagingFactory(this);
	}

	@Override
	public List<Dependency> getAllDependencies() {
		return Lists.newArrayList(
				Dependency.KYORI_TEXT,
				Dependency.KYORI_TEXT_SERIALIZER_LEGACY,
				Dependency.KYORI_TEXT_SERIALIZER_GSON,
				Dependency.CAFFEINE,
				Dependency.MXPARSER
		);
	}
}
