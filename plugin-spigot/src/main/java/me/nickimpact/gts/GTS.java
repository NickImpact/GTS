package me.nickimpact.gts;

import co.aikar.commands.BukkitCommandManager;
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
import com.nickimpact.impactor.spigot.configuration.SpigotConfig;
import com.nickimpact.impactor.spigot.configuration.SpigotConfigAdapter;
import com.nickimpact.impactor.spigot.logging.SpigotLogger;
import lombok.Getter;
import me.nickimpact.gts.api.GtsService;
import me.nickimpact.gts.api.dependencies.classloader.PluginClassLoader;
import me.nickimpact.gts.api.holders.EntryClassification;
import me.nickimpact.gts.api.holders.EntryRegistry;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.plugin.IGTSPlugin;
import me.nickimpact.gts.commands.EntryClassificationContextHandler;
import me.nickimpact.gts.commands.GtsCmd;
import me.nickimpact.gts.config.ConfigKeys;
import me.nickimpact.gts.config.MsgConfigKeys;
import me.nickimpact.gts.events.ServiceReadyEvent;
import me.nickimpact.gts.json.EntryAdapter;
import me.nickimpact.gts.listings.SpigotItemEntry;
import me.nickimpact.gts.manager.SpigotListingManager;
import me.nickimpact.gts.service.SpigotGtsService;
import me.nickimpact.gts.manager.TextParsingUtils;
import me.nickimpact.gts.storage.StorageFactory;
import me.nickimpact.gts.api.storage.StorageType;
import me.nickimpact.gts.api.dependencies.Dependency;
import me.nickimpact.gts.api.dependencies.DependencyManager;
import me.nickimpact.gts.api.dependencies.classloader.ReflectionClassLoader;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
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

	private BukkitCommandManager cmdManager;

	@Override
	public void onEnable() {
		instance = this;
		this.logger = new SpigotLogger(this);
		logger.info(ChatColor.GREEN + "Initializing GTS...");
		this.service = new SpigotGtsService();

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

		this.gson = new GsonBuilder()
				.setPrettyPrinting()
				.registerTypeAdapter(Entry.class, new EntryAdapter(this))
				.create();

		logger.info("Loading default entry types...");
		this.service.setRegistry(new EntryRegistry(this));
		this.service.registerEntry(Lists.newArrayList("items", "item"), SpigotItemEntry.class, null, Material.DIAMOND.name(), null);
		Bukkit.getPluginManager().callEvent(new ServiceReadyEvent(this.service));

		this.textParsingUtils = new TextParsingUtils();

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
				EntryClassificationContextHandler.getContextResolver()
		);

		this.cmdManager.registerCommand(new GtsCmd());

		logger.info("Initializing and reading storage...");
		this.service.setStorage(new StorageFactory(this).getInstance(StorageType.JSON));
		this.service.getListingManager().readStorage();
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
}
