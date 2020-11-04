package net.impactdev.gts.bungee;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.bungee.listings.BungeeAuction;
import net.impactdev.gts.bungee.messaging.interpreters.BungeeAuctionInterpreter;
import net.impactdev.gts.common.api.ApiRegistrationUtil;
import net.impactdev.gts.common.api.GTSAPIProvider;
import net.impactdev.gts.common.data.ResourceManagerImpl;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.plugin.PluginMetadata;
import net.impactdev.impactor.api.storage.StorageType;
import net.impactdev.impactor.bungee.configuration.BungeeConfig;
import net.impactdev.impactor.bungee.configuration.BungeeConfigAdapter;
import net.impactdev.impactor.bungee.plugin.AbstractBungeePlugin;
import net.impactdev.gts.api.blacklist.Blacklist;
import net.impactdev.gts.api.extension.ExtensionManager;
import net.impactdev.gts.api.storage.GTSStorage;
import net.impactdev.gts.bungee.messaging.BungeeMessagingFactory;
import net.impactdev.gts.bungee.messaging.interpreters.BungeeBINRemoveInterpreter;
import net.impactdev.gts.bungee.messaging.interpreters.BungeePingPongInterpreter;
import net.impactdev.gts.common.blacklist.BlacklistImpl;
import net.impactdev.gts.common.config.updated.ConfigKeys;
import net.impactdev.gts.common.messaging.InternalMessagingService;
import net.impactdev.gts.common.messaging.MessagingFactory;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.storage.StorageFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class GTSBungeePlugin extends AbstractBungeePlugin implements GTSPlugin {

	private final GTSBungeeBootstrap bootstrap;

	private Config config;

	private GTSStorage storage;
	private InternalMessagingService messagingService;

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

	public void load() {
		ApiRegistrationUtil.register(new GTSAPIProvider());

		Impactor.getInstance().getRegistry().register(GTSPlugin.class, this);
		Impactor.getInstance().getRegistry().register(Blacklist.class, new BlacklistImpl());

		GTSService.getInstance().getGTSComponentManager().registerListingResourceManager(Auction.class, new ResourceManagerImpl<>("Auctions", "N/A", BungeeAuction::deserialize));
		Impactor.getInstance().getRegistry().registerBuilderSupplier(Auction.AuctionBuilder.class, BungeeAuction.BungeeAuctionBuilder::new);
	}

	public void enable() {
		this.config = new BungeeConfig(new BungeeConfigAdapter(this, new File(this.getConfigDir().toFile(), "main.conf")), new ConfigKeys());
		this.storage = new StorageFactory(this).getInstance(StorageType.MARIADB);

		this.messagingService = this.getMessagingFactory().getInstance();

		BungeePingPongInterpreter.registerDecoders(this);
		BungeePingPongInterpreter.registerInterpreters(this);
		new BungeeBINRemoveInterpreter().register(this);
		new BungeeAuctionInterpreter().register(this);
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
}
