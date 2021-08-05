package net.impactdev.gts;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ichorpowered.protocolcontrol.service.ProtocolService;
import net.impactdev.gts.listeners.PlayerItemListingListener;
import net.impactdev.gts.listings.SpongeItemEntry;
import net.impactdev.gts.listings.legacy.SpongeLegacyItemStorable;
import net.impactdev.gts.listings.searcher.SpongeItemSearcher;
import net.impactdev.gts.listings.searcher.SpongeUserSearcher;
import net.impactdev.gts.manager.SpongeListingManager;
import net.impactdev.gts.messaging.SpongeMessagingFactory;
import net.impactdev.gts.api.extension.Extension;
import net.impactdev.gts.api.listings.manager.ListingManager;
import net.impactdev.gts.api.messaging.message.type.admin.ForceDeleteMessage;
import net.impactdev.gts.api.player.PlayerSettings;
import net.impactdev.gts.commands.GTSCommandManager;
import net.impactdev.gts.common.messaging.messages.admin.ForceDeleteMessageImpl;
import net.impactdev.gts.common.player.PlayerSettingsImpl;
import net.impactdev.gts.api.environment.Environment;
import net.impactdev.gts.common.utils.EconomicFormatter;
import net.impactdev.gts.common.utils.Version;
import net.impactdev.gts.listeners.AnvilRenameListener;
import net.impactdev.gts.listeners.JoinListener;
import net.impactdev.gts.listings.data.SpongeItemManager;
import net.impactdev.gts.messaging.interpreters.SpongeAdminInterpreters;
import net.impactdev.gts.messaging.interpreters.SpongeAuctionInterpreters;
import net.impactdev.gts.messaging.interpreters.SpongeBINInterpreters;
import net.impactdev.gts.messaging.interpreters.SpongeListingInterpreters;
import net.impactdev.gts.sponge.listings.ui.SpongeMainPageProvider;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.gts.ui.SpongeMainMenu;
import net.impactdev.gts.util.OreVersionChecker;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.dependencies.Dependency;
import net.impactdev.impactor.api.dependencies.DependencyManager;
import net.impactdev.impactor.api.plugin.PluginMetadata;
import net.impactdev.impactor.api.storage.StorageType;
import net.impactdev.impactor.sponge.configuration.SpongeConfig;
import net.impactdev.impactor.sponge.configuration.SpongeConfigAdapter;
import net.impactdev.impactor.sponge.plugin.AbstractSpongePlugin;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.blacklist.Blacklist;
import net.impactdev.gts.api.exceptions.LackingServiceException;
import net.impactdev.gts.api.extension.ExtensionManager;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.api.stashes.Stash;
import net.impactdev.gts.api.storage.GTSStorage;
import net.impactdev.gts.common.api.ApiRegistrationUtil;
import net.impactdev.gts.common.api.GTSAPIProvider;
import net.impactdev.gts.common.blacklist.BlacklistImpl;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.data.ResourceManagerImpl;
import net.impactdev.gts.common.extension.SimpleExtensionManager;
import net.impactdev.gts.common.messaging.InternalMessagingService;
import net.impactdev.gts.common.messaging.MessagingFactory;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.storage.StorageFactory;
import net.impactdev.gts.messaging.interpreters.SpongePingPongInterpreter;
import net.impactdev.gts.sponge.listings.SpongeAuction;
import net.impactdev.gts.sponge.listings.SpongeBuyItNow;
import net.impactdev.gts.sponge.pricing.provided.MonetaryPrice;
import net.impactdev.gts.sponge.stash.SpongeStash;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.user.UserStorageService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GTSSpongePlugin extends AbstractSpongePlugin implements GTSPlugin {

	private final GTSSpongeBootstrap bootstrap;

	private Config config;
	private Config msgConfig;

	private GTSStorage storage;

	private InternalMessagingService messagingService;

	private SimpleExtensionManager extensionManager;

	private Environment environment = null;

	public GTSSpongePlugin(GTSSpongeBootstrap bootstrap, org.slf4j.Logger fallback) {
		super(PluginMetadata.builder()
				.id("gts")
				.name("GTS")
				.version("@version@")
				.description("@gts_description@")
				.build(),
				fallback
		);
		this.bootstrap = bootstrap;
	}

	public void preInit() {
		Utilities.setContainer(this.bootstrap.getContainer());
		Impactor.getInstance().getRegistry().register(GTSPlugin.class, this);
		ApiRegistrationUtil.register(new GTSAPIProvider());

		String sponge = Sponge.getGame().getPlatform().getContainer(Platform.Component.API).getVersion().orElse("");
		if(!sponge.startsWith("7.3")) {
			throw new IllegalStateException("Invalid Sponge version");
		}

		this.displayBanner();

		this.getPluginLogger().info("Initializing API Components...");
		Sponge.getServiceManager().setProvider(this.bootstrap, GTSService.class, GTSService.getInstance());
		Impactor.getInstance().getRegistry().register(Blacklist.class, new BlacklistImpl());

		this.supplyBuilders();

		Impactor.getInstance().getRegistry().register(ListingManager.class, new SpongeListingManager());
		Impactor.getInstance().getRegistry().register(
				EconomicFormatter.class,
				amount -> this.getEconomy().getDefaultCurrency().format(new BigDecimal(amount)).toPlain()
		);

		GTSService.getInstance().getGTSComponentManager().registerListingResourceManager(BuyItNow.class, new ResourceManagerImpl<>("BIN", "minecraft:emerald", SpongeBuyItNow::deserialize));
		GTSService.getInstance().getGTSComponentManager().registerListingResourceManager(Auction.class, new ResourceManagerImpl<>("Auctions", "minecraft:gold_ingot", SpongeAuction::deserialize));
		GTSService.getInstance().getGTSComponentManager().registerEntryManager(SpongeItemEntry.class, new SpongeItemManager());
		GTSService.getInstance().getGTSComponentManager().registerPriceManager(MonetaryPrice.class, new MonetaryPrice.MonetaryPriceManager());
		GTSService.getInstance().getGTSComponentManager().registerLegacyEntryDeserializer("item", new SpongeLegacyItemStorable());

		GTSService.getInstance().addSearcher(new SpongeItemSearcher());
		GTSService.getInstance().addSearcher(new SpongeUserSearcher());

		this.extensionManager = new SimpleExtensionManager(this);

		this.getPluginLogger().info("Setting up configuration...");
		this.copyResource(Paths.get("gts.conf"), this.getConfigDir());
		this.config = new SpongeConfig(new SpongeConfigAdapter(this, new File(this.getConfigDir().toFile(), "gts.conf")), new ConfigKeys());

		String language = this.config.get(ConfigKeys.LANGUAGE);
		this.copyResource(Paths.get("lang").resolve(language.toLowerCase() + ".conf"), this.getConfigDir());
		this.msgConfig = new SpongeConfig(new SpongeConfigAdapter(this, new File(this.getConfigDir().toFile(), "lang/" + language.toLowerCase() + ".conf"), true), new MsgConfigKeys());

		if(this.getConfiguration().get(ConfigKeys.REDIS_ENABLED)) {
			this.getPluginLogger().info("Setting up Redis Client...");
			Impactor.getInstance().getRegistry().get(DependencyManager.class).loadDependencies(Lists.newArrayList(
					Dependency.JEDIS,
					Dependency.COMMONS_POOL_2
			));
		}

		this.getPluginLogger().info("Sending load event to available extensions...");
		this.extensionManager.loadExtensions(this.getBootstrap().getConfigDirectory().resolve("extensions"));

		this.config.get(ConfigKeys.BLACKLIST).read();
	}

	public void init() {
		this.applyMessagingServiceSettings();
		new GTSCommandManager(this.bootstrap.getContainer()).register();
		this.storage = new StorageFactory(this).getInstance(StorageType.H2);
		this.extensionManager.enableExtensions();

		Sponge.getEventManager().registerListeners(this.bootstrap, new JoinListener());

		new PlayerItemListingListener().registerListener();

		if(!this.config.get(ConfigKeys.ITEMS_ALLOW_ANVIL_NAMES)) {
			Sponge.getEventManager().registerListeners(this.bootstrap, new AnvilRenameListener());
		}
	}

	public void started() {
		if(!Sponge.getServiceManager().isRegistered(EconomyService.class)) {
			throw new LackingServiceException(EconomyService.class);
		}

		MonetaryPrice.setEconomy(this.getEconomy());

		final Version current = new Version(this.getMetadata().getVersion());
		OreVersionChecker.query().thenAccept(response -> {
			if(current.compareTo(response) < 0) {
				Impactor.getInstance().getScheduler().executeSync(() -> {
					GTSPlugin.getInstance().getPluginLogger().warn("A new version of GTS is available on Ore!");
					GTSPlugin.getInstance().getPluginLogger().warn("You can download it from here: &bhttps://ore.spongepowered.org/api/v1/projects/gts/versions/recommended/download");
				});
			}
		});
	}

	public MessagingFactory<?> getMessagingFactory() {
		return new SpongeMessagingFactory(this);
	}

	public EconomyService getEconomy() {
		return Sponge.getServiceManager().provideUnchecked(EconomyService.class);
	}

	public PluginContainer getPluginContainer() {
		return this.bootstrap.getContainer();
	}

	@Override
	public <T extends GTSPlugin> T as(Class<T> type) {
		if(!type.isAssignableFrom(this.getClass())) {
			throw new RuntimeException("Invalid plugin typing");
		}
		return (T) this;
	}

	@Override
	public GTSSpongeBootstrap getBootstrap() {
		return this.bootstrap;
	}

	@SuppressWarnings("OptionalGetWithoutIsPresent")
	@Override
	public Environment getEnvironment() {
		Environment environment = Optional.ofNullable(this.environment)
				.orElseGet(() -> (this.environment = new Environment()));
		environment.append("Sponge", Sponge.getGame().getPlatform().getContainer(Platform.Component.IMPLEMENTATION).getName() + " " + Sponge.getGame().getPlatform().getContainer(Platform.Component.IMPLEMENTATION).getVersion().orElse("?"));
		environment.append("Impactor", Sponge.getPluginManager().getPlugin("impactor").get().getVersion().get());
		environment.append("GTS", this.getMetadata().getVersion());

		Sponge.getServiceManager().getRegistration(EconomyService.class).ifPresent(provider -> {
			environment.append(provider.getPlugin().getName(), provider.getPlugin().getVersion().orElse("Unknown"));
		});
		Sponge.getServiceManager().getRegistration(ProtocolService.class).ifPresent(provider -> {
			environment.append(provider.getPlugin().getName(), provider.getPlugin().getVersion().orElse("Unknown"));
		});

		for(Extension extension : GTSService.getInstance().getAllExtensions()) {
			environment.append(extension.getMetadata().getName(), extension.getMetadata().getVersion());
			extension.getExtendedEnvironmentInformation(environment);
		}

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
		return this.extensionManager;
	}

	@Override
	public InternalMessagingService getMessagingService() {
		return this.messagingService;
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
	public List<Dependency> getAllDependencies() {
		return Lists.newArrayList(
				Dependency.KYORI_TEXT,
				Dependency.KYORI_TEXT_SERIALIZER_LEGACY,
				Dependency.KYORI_TEXT_SERIALIZER_GSON,
				Dependency.CAFFEINE,
				Dependency.MXPARSER
		);
	}

	@Override
	public List<StorageType> getStorageRequirements() {
		return Lists.newArrayList();
	}

	@Override
	public boolean inDebugMode() {
		return this.getConfiguration().get(ConfigKeys.DEBUG_ENABLED);
	}

	@Override
	public Config getMsgConfig() {
		return this.msgConfig;
	}

	private void displayBanner() {
		List<String> output = Lists.newArrayList(
				"",
				"&3     _________________",
				"&3    / ____/_  __/ ___/       &aGTS " + this.getMetadata().getVersion(),
				"&3   / / __  / /  \\__ \\        &aRunning on: &e" + Sponge.getGame().getPlatform().getContainer(Platform.Component.IMPLEMENTATION).getName() + " " + Sponge.getGame().getPlatform().getContainer(Platform.Component.IMPLEMENTATION).getVersion().orElse(""),
				"&3  / /_/ / / /  ___/ /        &aAuthor: &3NickImpact",
				"&3  \\____/ /_/  /____/",
				""
		);

		GTSPlugin.getInstance().getPluginLogger().noTag(output);
	}

	private void applyMessagingServiceSettings() {
		this.messagingService = this.getMessagingFactory().getInstance();
		new SpongePingPongInterpreter().register(this);
		new SpongeBINInterpreters().register(this);
		new SpongeAuctionInterpreters().register(this);
		new SpongeListingInterpreters().register(this);
		new SpongeAdminInterpreters().register(this);
	}

	private void supplyBuilders() {
		Impactor.getInstance().getRegistry().registerBuilderSupplier(Auction.AuctionBuilder.class, SpongeAuction.SpongeAuctionBuilder::new);
		Impactor.getInstance().getRegistry().registerBuilderSupplier(BuyItNow.BuyItNowBuilder.class, SpongeBuyItNow.SpongeBuyItNowBuilder::new);
		Impactor.getInstance().getRegistry().registerBuilderSupplier(Stash.StashBuilder.class, SpongeStash.SpongeStashBuilder::new);
		Impactor.getInstance().getRegistry().registerBuilderSupplier(SpongeMainPageProvider.Creator.class, SpongeMainMenu.MainMenuCreator::new);
		Impactor.getInstance().getRegistry().registerBuilderSupplier(PlayerSettings.PlayerSettingsBuilder.class, PlayerSettingsImpl.PlayerSettingsImplBuilder::new);
		Impactor.getInstance().getRegistry().registerBuilderSupplier(ForceDeleteMessage.Response.ResponseBuilder.class, ForceDeleteMessageImpl.ForceDeleteResponse.ForcedDeleteResponseBuilder::new);
	}

	@Override
	public ImmutableList<StorageType> getMultiServerCompatibleStorageOptions() {
		return ImmutableList.copyOf(Lists.newArrayList(
				StorageType.MARIADB,
				StorageType.MYSQL,
				StorageType.MONGODB,
				StorageType.POSTGRESQL
		));
	}

	@Override
	public String getPlayerDisplayName(UUID id) {
		UserStorageService uss = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);
		return uss.get(id).map(User::getName).orElse("Unknown");
	}

	private void copyResource(Path path, Path destination) {
		Path base = Paths.get("assets", "gts");
		if(!Files.exists(destination.resolve(path))) {
			try (InputStream resource = this.getResourceStream(base.resolve(path).toString().replace("\\", "/"))) {
				Files.createDirectories(destination.resolve(path).getParent());
				Files.copy(resource, destination.resolve(path));
			} catch (NullPointerException | IOException e) {
				GTSPlugin.getInstance().getPluginLogger().warn(path.getFileName().toString() + " does not exist! Creating it for you...");
				try {
					Files.createDirectories(destination.resolve(path).getParent());
					Files.createFile(destination.resolve(path));
				} catch (Exception e1) {
					throw new RuntimeException(e1);
				}
			}
		}
	}

}
