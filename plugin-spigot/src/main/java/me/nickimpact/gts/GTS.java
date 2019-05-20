package me.nickimpact.gts;

import co.aikar.commands.PaperCommandManager;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nickimpact.impactor.api.commands.Command;
import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.api.logging.Logger;
import com.nickimpact.impactor.api.platform.Platform;
import com.nickimpact.impactor.api.plugin.Configurable;
import com.nickimpact.impactor.api.plugin.ImpactorPlugin;
import com.nickimpact.impactor.api.plugin.PluginInfo;
import com.nickimpact.impactor.api.plugin.Translatable;
import com.nickimpact.impactor.api.registry.BuilderRegistry;
import com.nickimpact.impactor.spigot.configuration.SpigotConfig;
import com.nickimpact.impactor.spigot.configuration.SpigotConfigAdapter;
import com.nickimpact.impactor.spigot.logging.SpigotLogger;
import lombok.Getter;
import me.nickimpact.gts.api.GtsService;
import me.nickimpact.gts.api.dependencies.classloader.PluginClassLoader;
import me.nickimpact.gts.api.holders.EntryClassification;
import me.nickimpact.gts.api.holders.EntryRegistry;
import me.nickimpact.gts.api.holders.ServiceInstance;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.plugin.IGTSPlugin;
import me.nickimpact.gts.commands.SpigotEntryClassificationContextHandler;
import me.nickimpact.gts.commands.SpigotGtsCmd;
import me.nickimpact.gts.config.ConfigKeys;
import me.nickimpact.gts.config.MsgConfigKeys;
import me.nickimpact.gts.discord.DiscordNotifier;
import me.nickimpact.gts.api.plugin.PluginInstance;
import me.nickimpact.gts.json.EntryAdapter;
import me.nickimpact.gts.listings.SpigotItemEntry;
import me.nickimpact.gts.listings.SpigotItemUI;
import me.nickimpact.gts.manager.SpigotListingManager;
import me.nickimpact.gts.spigot.MoneyPrice;
import me.nickimpact.gts.spigot.SpigotGtsService;
import me.nickimpact.gts.manager.TextParsingUtils;
import me.nickimpact.gts.spigot.SpigotListing;
import me.nickimpact.gts.storage.StorageFactory;
import me.nickimpact.gts.api.storage.StorageType;
import me.nickimpact.gts.api.dependencies.Dependency;
import me.nickimpact.gts.api.dependencies.DependencyManager;
import me.nickimpact.gts.api.dependencies.classloader.ReflectionClassLoader;
import me.nickimpact.gts.tasks.SpigotListingTasks;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GTS extends JavaPlugin implements IGTSPlugin, Configurable, Translatable {

	@Getter private static GTS instance;

	private SpigotGtsService service;

	private Logger logger;

	private Path configDir;
	private Config config;
	private Config msgConfig;

	private TextParsingUtils textParsingUtils;

	private ScheduledExecutorService asyncExecutor;

	private Gson gson;

	private PluginClassLoader loader;
	private DependencyManager dependencyManager;

	private PaperCommandManager cmdManager;

	private DiscordNotifier discordNotifier;

	@Override
	public void onLoad() {
		instance = this;
		PluginInstance.setInstance(this);
		this.logger = new SpigotLogger(this);
		logger.info(ChatColor.GREEN + "Loading GTS...");
		logger.info(ChatColor.GREEN + "Initializing API service...");
		this.service = new SpigotGtsService(this);
		ServiceInstance.setService(this.service);

		this.service.setBuilders(new BuilderRegistry());
		this.service.getBuilderRegistry().register(Listing.ListingBuilder.class, SpigotListing.SpigotListingBuilder.class);

		logger.info("Loading default entry types...");
		this.service.setRegistry(new EntryRegistry(this));
		this.service.registerEntry(
				Lists.newArrayList("items", "item"),
				SpigotItemEntry.class,
				new SpigotItemUI(),
				Material.DIAMOND.name(),
				SpigotItemEntry::cmdExecutor
		);
	}

	@Override
	public void onEnable() {
		logger.info(ChatColor.GREEN + "Enabling GTS...");

		logger.info("Loading configuration...");
		this.configDir = this.getDataFolder().toPath();
		this.config = new SpigotConfig(new SpigotConfigAdapter(this, new File(this.configDir.toFile(), "gts.conf")), new ConfigKeys());
		this.msgConfig = new SpigotConfig(new SpigotConfigAdapter(this, new File(this.configDir.toFile(), "lang/en_us.conf")), new MsgConfigKeys());

		logger.info("Initializing additional dependencies...");
		this.loader = new ReflectionClassLoader(this);
		this.dependencyManager = new DependencyManager(this);
		this.dependencyManager.loadDependencies(EnumSet.of(Dependency.CONFIGURATE_CORE, Dependency.CONFIGURATE_HOCON, Dependency.HOCON_CONFIG, Dependency.CONFIGURATE_GSON, Dependency.CONFIGURATE_YAML));

		StorageType st = StorageType.parse(this.config.get(ConfigKeys.STORAGE_METHOD));
		this.dependencyManager.loadStorageDependencies(ImmutableSet.of(st != null ? st : StorageType.H2));

		logger.info("Initializing internal handlers...");
		this.gson = new GsonBuilder()
				.setPrettyPrinting()
				.registerTypeAdapter(Entry.class, new EntryAdapter(this))
				.create();

		this.textParsingUtils = new TextParsingUtils();

		logger.info("Integrating with Vault...");
		RegisteredServiceProvider<Economy> economy = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
		if(economy == null) {
			logger.error("No economy service available, plugin shutting down...");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		MoneyPrice.setEconomy(economy.getProvider());

		logger.info("Setting up discord notifier...");
		this.discordNotifier = new DiscordNotifier(this);

		logger.info("Initializing the Listing Manager...");
		this.service.setManager(new SpigotListingManager());

		logger.info("Registering commands with ACF...");
		this.cmdManager = new PaperCommandManager(this);
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
				SpigotEntryClassificationContextHandler.getContextResolver()
		);

		this.cmdManager.registerCommand(new SpigotGtsCmd());

		logger.info("Initializing and reading storage...");
		this.service.setStorage(new StorageFactory(this).getInstance(StorageType.JSON));
		this.service.getListingManager().readStorage();

		logger.info("Deploying running tasks...");
		new SpigotListingTasks().createExpirationTask();

		logger.info(ChatColor.GREEN + "Startup complete!");
	}

	@Override
	public void onDisable() {

	}

	@Override
	public GtsService getAPIService() {
		return this.service;
	}

	@Override
	public ScheduledExecutorService getAsyncExecutor() {
		return asyncExecutor != null ? asyncExecutor : (asyncExecutor = Executors.newSingleThreadScheduledExecutor());
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
		return Platform.Spigot;
	}

	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo() {
			@Override
			public String getID() {
				return "gts";
			}

			@Override
			public String getName() {
				return "GTS";
			}

			@Override
			public String getVersion() {
				return "4.2.0";
			}

			@Override
			public String getDescription() {
				return "XXX";
			}
		};
	}

	@Override
	public Logger getPluginLogger() {
		return this.logger;
	}

	@Override
	public List<Config> getConfigs() {
		return null;
	}

	@Override
	public List<Command> getCommands() {
		return null;
	}

	@Override
	public List<Object> getListeners() {
		return null;
	}

	@Override
	public Consumer<ImpactorPlugin> onReload() {
		return null;
	}

	@Override
	public boolean isConnected() {
		return false;
	}

	@Override
	public void setConnected() {

	}

	@Override
	public void handleDisconnect() {

	}

	public TextParsingUtils getTextParsingUtils() {
		return this.textParsingUtils;
	}

	@Override
	public Path getConfigDir() {
		return this.configDir;
	}

	@Override
	public Config getConfiguration() {
		return this.config;
	}

	@Override
	public Config getMsgConfig() {
		return this.msgConfig;
	}

	public DiscordNotifier getDiscordNotifier() {
		return discordNotifier;
	}
}
