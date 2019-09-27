package me.nickimpact.gts;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.SpongeCommandManager;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.api.logging.Logger;
import com.nickimpact.impactor.api.platform.Platform;
import com.nickimpact.impactor.api.plugin.ImpactorPlugin;
import com.nickimpact.impactor.api.plugin.PluginInfo;
import com.nickimpact.impactor.api.registry.BuilderRegistry;
import com.nickimpact.impactor.api.storage.StorageType;
import com.nickimpact.impactor.api.storage.dependencies.DependencyManager;
import com.nickimpact.impactor.api.storage.dependencies.classloader.PluginClassLoader;
import com.nickimpact.impactor.api.storage.dependencies.classloader.ReflectionClassLoader;
import com.nickimpact.impactor.sponge.AbstractSpongePlugin;
import com.nickimpact.impactor.sponge.SpongeImpactorPlugin;
import com.nickimpact.impactor.sponge.configuration.SpongeConfig;
import com.nickimpact.impactor.sponge.configuration.SpongeConfigAdapter;
import com.nickimpact.impactor.sponge.logging.SpongeLogger;
import io.github.nucleuspowered.nucleus.api.service.NucleusMessageTokenService;
import lombok.Getter;
import me.nickimpact.gts.api.GtsService;
import me.nickimpact.gts.api.holders.EntryClassification;
import me.nickimpact.gts.api.holders.EntryRegistry;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.plugin.PluginInstance;
import me.nickimpact.gts.commands.SpongeEntryClassificationContextHandler;
import me.nickimpact.gts.commands.SpongeGtsCmd;
import me.nickimpact.gts.config.ConfigKeys;
import me.nickimpact.gts.config.MsgConfigKeys;
import me.nickimpact.gts.deprecated.ItemEntry;
import me.nickimpact.gts.deprecated.adapters.OldEntryAdapter;
import me.nickimpact.gts.deprecated.adapters.OldPriceAdapter;
import me.nickimpact.gts.discord.DiscordNotifier;
import me.nickimpact.gts.json.DataContainerAdapter;
import me.nickimpact.gts.json.EntryAdapter;
import me.nickimpact.gts.listeners.JoinListener;
import me.nickimpact.gts.listings.SpongeItemEntry;
import me.nickimpact.gts.listings.SpongeItemUI;
import me.nickimpact.gts.listings.searching.ItemSearcher;
import me.nickimpact.gts.manager.SpongeListingManager;
import me.nickimpact.gts.sponge.*;
import me.nickimpact.gts.sponge.service.SpongeGtsService;
import me.nickimpact.gts.storage.StorageFactory;
import me.nickimpact.gts.tasks.SpongeListingTasks;
import me.nickimpact.gts.sponge.text.TokenService;
import me.nickimpact.gts.text.ItemTokens;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.AsynchronousExecutor;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.service.economy.EconomyService;

import java.io.File;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static me.nickimpact.gts.GTSInfo.*;

@Getter
@Plugin(id = ID, name = NAME, version = VERSION, description = DESCRIPTION, dependencies = {@Dependency(id = "impactor"), @Dependency(id = "nucleus")})
public class GTS extends AbstractSpongePlugin implements SpongePlugin {

	@Getter
	private static GTS instance;

	@Inject
	private PluginContainer pluginContainer;

	@Inject
	private org.slf4j.Logger fallback;
	private SpongeLogger logger;

	@Inject
	@ConfigDir(sharedRoot = false)
	private Path configDir;

	private Config config;
	private Config msgConfig;

	private SpongeGtsService service;

	private EconomyService economy;
	private TextParsingUtils textParsingUtils;

	private Gson gson;

	private DiscordNotifier discordNotifier;

	private SpongeCommandManager cmdManager;

	private PluginClassLoader loader;
	private DependencyManager dependencyManager;

	@Inject
	@AsynchronousExecutor
	private SpongeExecutorService async;

	public GTS() {
		instance = this;
		PluginInstance.setInstance(this);
		this.connect();
	}

	@Listener
	public void onReload(GameReloadEvent e) {
		this.config.reload();
		this.msgConfig.reload();
	}

	@Listener
	public void registerServices(ChangeServiceProviderEvent e) {
		if(e.getService().equals(EconomyService.class)) {
			this.economy = (EconomyService) e.getNewProviderRegistration().getProvider();
			MoneyPrice.setEconomy(this.economy);
		} else if(e.getService().equals(NucleusMessageTokenService.class)) {
			this.textParsingUtils = new TextParsingUtils(this);
		}
	}

	@Listener
	public void onPreInit(GamePreInitializationEvent e) {
		this.logger = new SpongeLogger(this, this.fallback);
		((GTSInfo)this.getPluginInfo()).displayBanner();
		this.logger.info("Initializing GTS...");
		this.logger.info("Registering Service with Sponge...");
		Sponge.getServiceManager().setProvider(this, GtsService.class, service = new SpongeGtsService(this));

		logger.info("Loading configuration...");
		this.config = new SpongeConfig(new SpongeConfigAdapter(this, new File(this.configDir.toFile(), "gts.conf")), new ConfigKeys());
		this.msgConfig = new SpongeConfig(new SpongeConfigAdapter(this, new File(this.configDir.toFile(), "lang/en_us.conf")), new MsgConfigKeys());

		this.logger.info("Loading default entry types...");
		this.service.setRegistry(new EntryRegistry(this));

		if(this.config.get(ConfigKeys.ITEMS_ENABLED)) {
			this.service.registerEntry(
					Lists.newArrayList("Items", "Item"),
					SpongeItemEntry.class,
					new SpongeItemUI(),
					"diamond",
					SpongeItemEntry::cmdExecutor
			);
		} else {
			this.logger.info("Ignoring item entry type (disabled in configuration)");
		}

		this.service.setBuilders(new BuilderRegistry());
		this.service.getBuilderRegistry().register(Listing.ListingBuilder.class, SpongeListing.SpongeListingBuilder.class);

		this.service.getAllDeprecatedTypes().add(ItemEntry.class);

		this.service.addSearcher("item", new ItemSearcher());
	}

	@Listener
	public void onInit(GamePostInitializationEvent e) {
		logger.info("&aEnabling GTS...");

		logger.info("Initializing additional dependencies...");
		this.loader = new ReflectionClassLoader(this);
		this.dependencyManager = new DependencyManager(this);
		this.dependencyManager.loadDependencies(EnumSet.of(com.nickimpact.impactor.api.storage.dependencies.Dependency.CONFIGURATE_CORE,com.nickimpact.impactor.api.storage.dependencies.Dependency.CONFIGURATE_HOCON, com.nickimpact.impactor.api.storage.dependencies.Dependency.HOCON_CONFIG, com.nickimpact.impactor.api.storage.dependencies.Dependency.CONFIGURATE_GSON, com.nickimpact.impactor.api.storage.dependencies.Dependency.CONFIGURATE_YAML));

		StorageType st = StorageType.parse(this.config.get(ConfigKeys.STORAGE_METHOD));
		this.dependencyManager.loadStorageDependencies(ImmutableSet.of(st != null ? st : StorageType.H2));

		logger.info("Initializing internal handlers...");
		gson = new GsonBuilder().setPrettyPrinting()
				.registerTypeAdapter(Entry.class, new EntryAdapter(this))
				.registerTypeAdapter(DataContainer.class, new DataContainerAdapter())
				.create();

		Sponge.getEventManager().registerListeners(this, new JoinListener());

		logger.info("Setting up discord notifier...");
		this.discordNotifier = new DiscordNotifier(this);

		logger.info("Initializing the Listing Manager...");
		this.service.setManager(new SpongeListingManager());

		logger.info("Registering tokens with Nucleus...");
		this.service.setTokenService(new TokenService(this));
		this.service.registerTokens(new ItemTokens());

		logger.info("Registering commands with ACF...");
		this.cmdManager = new SpongeCommandManager(this.getPluginContainer());
		this.cmdManager.enableUnstableAPI("help");

		StringJoiner pipedEntryTypes = new StringJoiner("|");
		for(List<String> identifiers : this.service.getEntryRegistry().getClassifications().stream().map(EntryClassification::getIdentifers).collect(Collectors.toList())) {
			for(String id : identifiers) {
				pipedEntryTypes.add(id);
			}
		}

		this.cmdManager.getCommandReplacements().addReplacement("entryType", pipedEntryTypes.toString());
		this.cmdManager.getCommandContexts().registerContext(
				EntryClassification.class,
				SpongeEntryClassificationContextHandler.getContextResolver()
		);

		this.cmdManager.registerCommand(new SpongeGtsCmd());

		OldEntryAdapter oea = new OldEntryAdapter(this);
		for(Class<? extends me.nickimpact.gts.api.deprecated.Entry> clazz : this.service.getAllDeprecatedTypes()) {
			try {
				oea.getRegistry().register(clazz);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		OldPriceAdapter opa = new OldPriceAdapter(this);
		try {
			opa.getRegistry().register(me.nickimpact.gts.api.deprecated.MoneyPrice.class);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		this.getAPIService().registerOldTypeAdapter(me.nickimpact.gts.api.deprecated.Entry.class, oea);
		this.getAPIService().registerOldTypeAdapter(me.nickimpact.gts.api.deprecated.Price.class, opa);
		this.getAPIService().registerOldTypeAdapter(DataContainer.class, new DataContainerAdapter());

		logger.info("Initializing storage...");
		this.service.setStorage(new StorageFactory(this).getInstance(StorageType.JSON));

		logger.info("Deploying running tasks...");
		new SpongeListingTasks().createExpirationTask();

		logger.info("&aStartup complete!");
	}

	@Listener
	public void onStart(GameStartedServerEvent event) {
		logger.info("Reading storage...");
		this.service.getListingManager().readStorage();
	}

	@Override
	public GtsService getAPIService() {
		return this.service;
	}

	@Override
	public ScheduledExecutorService getAsyncExecutor() {
		return this.async;
	}

	@Override
	public Gson getGson() {
		return this.gson;
	}

	@Override
	public PluginClassLoader getPluginClassLoader() {
		return SpongeImpactorPlugin.getInstance().getPluginClassLoader();
	}

	@Override
	public DependencyManager getDependencyManager() {
		return SpongeImpactorPlugin.getInstance().getDependencyManager();
	}

	@Override
	public List<StorageType> getStorageTypes() {
		return Lists.newArrayList(
				StorageType.parse(this.config.get(ConfigKeys.STORAGE_METHOD))
		);
	}

	@Override
	public Platform getPlatform() {
		return Platform.Sponge;
	}

	@Override
	public PluginInfo getPluginInfo() {
		return new GTSInfo();
	}

	@Override
	public Logger getPluginLogger() {
		return this.logger;
	}

	@Override
	public List<Config> getConfigs() {
		return Lists.newArrayList();
	}

	@Override
	public List<BaseCommand> getCommands() {
		return Lists.newArrayList();
	}

	@Override
	public List<Object> getListeners() {
		return Lists.newArrayList();
	}

	@Override
	public Consumer<ImpactorPlugin> onReload() {
		return plugin -> {};
	}

	@Override
	public Config getConfiguration() {
		return this.config;
	}
}
