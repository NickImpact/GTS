package me.nickimpact.gts;

import co.aikar.commands.SpongeCommandManager;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.nickimpact.impactor.api.commands.Command;
import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.api.logging.Logger;
import com.nickimpact.impactor.api.platform.Platform;
import com.nickimpact.impactor.api.plugin.ImpactorPlugin;
import com.nickimpact.impactor.api.plugin.PluginInfo;
import com.nickimpact.impactor.sponge.AbstractSpongePlugin;
import com.nickimpact.impactor.sponge.configuration.SpongeConfig;
import com.nickimpact.impactor.sponge.configuration.SpongeConfigAdapter;
import com.nickimpact.impactor.sponge.logging.SpongeLogger;
import io.github.nucleuspowered.nucleus.api.service.NucleusMessageTokenService;
import lombok.Getter;
import me.nickimpact.gts.api.GtsService;
import me.nickimpact.gts.api.dependencies.DependencyManager;
import me.nickimpact.gts.api.dependencies.classloader.PluginClassLoader;
import me.nickimpact.gts.api.dependencies.classloader.ReflectionClassLoader;
import me.nickimpact.gts.api.holders.EntryClassification;
import me.nickimpact.gts.api.holders.EntryRegistry;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.storage.StorageType;
import me.nickimpact.gts.commands.SpongeEntryClassificationContextHandler;
import me.nickimpact.gts.commands.SpongeGtsCmd;
import me.nickimpact.gts.config.ConfigKeys;
import me.nickimpact.gts.config.MsgConfigKeys;
import me.nickimpact.gts.discord.DiscordNotifier;
import me.nickimpact.gts.json.EntryAdapter;
import me.nickimpact.gts.listings.SpongeItemEntry;
import me.nickimpact.gts.listings.SpongeItemUI;
import me.nickimpact.gts.manager.SpongeListingManager;
import me.nickimpact.gts.sponge.MoneyPrice;
import me.nickimpact.gts.sponge.SpongeEntry;
import me.nickimpact.gts.sponge.SpongePlugin;
import me.nickimpact.gts.sponge.TextParsingUtils;
import me.nickimpact.gts.sponge.service.SpongeGtsService;
import me.nickimpact.gts.storage.StorageFactory;
import me.nickimpact.gts.tasks.SpongeListingTasks;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
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

	private GTSInfo info = new GTSInfo();

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

		}
	}

	@Listener
	public void onPreInit(GamePreInitializationEvent e) {
		this.logger = new SpongeLogger(this.fallback);
		this.info.displayBanner();
		this.logger.info("Initializing GTS...");
		this.logger.info("Registering Service with Sponge...");
		Sponge.getServiceManager().setProvider(this, GtsService.class, service = new SpongeGtsService(this));

		this.logger.info("Loading default entry types...");
		this.service.setRegistry(new EntryRegistry(this));
		this.service.registerEntry(
				Lists.newArrayList("items", "item"),
				SpongeEntry.class,
				new SpongeItemUI(),
				"diamond",
				SpongeItemEntry::cmdExecutor
		);
	}

	@Listener
	public void onInit(GameInitializationEvent e) {
		logger.info("&aEnabling GTS...");

		logger.info("Loading configuration...");
		this.config = new SpongeConfig(new SpongeConfigAdapter(this, new File(this.configDir.toFile(), "gts.conf")), new ConfigKeys());
		this.msgConfig = new SpongeConfig(new SpongeConfigAdapter(this, new File(this.configDir.toFile(), "lang/en_us.conf")), new MsgConfigKeys());

		logger.info("Initializing additional dependencies...");
		this.loader = new ReflectionClassLoader(this);
		this.dependencyManager = new DependencyManager(this);
		this.dependencyManager.loadDependencies(EnumSet.of(me.nickimpact.gts.api.dependencies.Dependency.CONFIGURATE_CORE, me.nickimpact.gts.api.dependencies.Dependency.CONFIGURATE_HOCON, me.nickimpact.gts.api.dependencies.Dependency.HOCON_CONFIG, me.nickimpact.gts.api.dependencies.Dependency.CONFIGURATE_GSON, me.nickimpact.gts.api.dependencies.Dependency.CONFIGURATE_YAML));

		StorageType st = StorageType.parse(this.config.get(ConfigKeys.STORAGE_METHOD));
		this.dependencyManager.loadStorageDependencies(ImmutableSet.of(st != null ? st : StorageType.H2));

		logger.info("Initializing internal handlers...");
		gson = new GsonBuilder().setPrettyPrinting()
				.registerTypeAdapter(Entry.class, new EntryAdapter(this))
				.create();

		this.textParsingUtils = new TextParsingUtils(this);

		logger.info("Setting up discord notifier...");
		this.discordNotifier = new DiscordNotifier(this);

		logger.info("Initializing the Listing Manager...");
		this.service.setManager(new SpongeListingManager());

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

		logger.info("Initializing and reading storage...");
		this.service.setStorage(new StorageFactory(this).getInstance(StorageType.JSON));
		this.service.getListingManager().readStorage();

		logger.info("Deploying running tasks...");
		new SpongeListingTasks().createExpirationTask();

		logger.info("&aStartup complete!");
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
		return this.loader;
	}

	@Override
	public DependencyManager getDependencyManager() {
		return this.dependencyManager;
	}

	@Override
	public Platform getPlatform() {
		return Platform.Sponge;
	}

	@Override
	public PluginInfo getPluginInfo() {
		return this.info;
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
	public List<Command> getCommands() {
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
