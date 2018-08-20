package com.nickimpact.gts;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.nickimpact.gts.api.GtsService;
import com.nickimpact.gts.api.GtsServiceImpl;
import com.nickimpact.gts.api.discord.IDiscordNotifier;
import com.nickimpact.gts.api.listings.entries.EntryHolder;
import com.nickimpact.gts.api.listings.Listing;
import com.nickimpact.gts.api.listings.entries.Entry;
import com.nickimpact.gts.api.listings.entries.EntryAdapter;
import com.nickimpact.gts.api.listings.pricing.*;
import com.nickimpact.gts.commands.GTSBaseCmd;
import com.nickimpact.gts.configuration.ConfigKeys;
import com.nickimpact.gts.configuration.MsgConfigKeys;
import com.nickimpact.gts.entries.items.ItemAdapter;
import com.nickimpact.gts.entries.items.ItemEntry;
import com.nickimpact.gts.entries.pixelmon.PokemonEntry;
import com.nickimpact.gts.entries.prices.ItemPrice;
import com.nickimpact.gts.entries.prices.MoneyPrice;
import com.nickimpact.gts.entries.prices.PokePrice;
import com.nickimpact.gts.discord.DiscordNotifier;
import com.nickimpact.gts.internal.TextParsingUtils;
import com.nickimpact.gts.listeners.JoinListener;
import com.nickimpact.gts.logs.Log;
import com.nickimpact.gts.storage.Storage;
import com.nickimpact.gts.storage.StorageFactory;
import com.nickimpact.gts.storage.StorageType;
import com.nickimpact.gts.storage.dao.file.FileWatcher;
import com.nickimpact.gts.ui.updater.GuiUpdater;
import com.nickimpact.gts.utils.ListingTasks;
import com.nickimpact.impactor.api.commands.SpongeCommand;
import com.nickimpact.impactor.api.configuration.AbstractConfig;
import com.nickimpact.impactor.api.configuration.AbstractConfigAdapter;
import com.nickimpact.impactor.api.configuration.ConfigBase;
import com.nickimpact.impactor.api.logger.Logger;
import com.nickimpact.impactor.api.plugins.PluginInfo;
import com.nickimpact.impactor.api.plugins.SpongePlugin;
import com.nickimpact.impactor.api.services.plan.PlanData;
import com.nickimpact.impactor.logging.ConsoleLogger;
import com.nickimpact.impactor.logging.SpongeLogger;
import io.github.nucleuspowered.nucleus.api.service.NucleusMessageTokenService;
import lombok.Getter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.AsynchronousExecutor;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@Getter
@Plugin(id = GTSInfo.ID, name = GTSInfo.NAME, version = GTSInfo.VERSION, description = GTSInfo.DESCRIPTION, dependencies = {@Dependency(id="nucleus"), @Dependency(id="impactor")})
public class GTS extends SpongePlugin {

	/** The containing instance of the plugin set by Sponge */
	@Inject private PluginContainer pluginContainer;

	private PluginInfo pluginInfo = new GTSInfo();

	@Inject private org.slf4j.Logger fallback;
	private Logger logger;

	/** The pathing to the config directory for GTS */
	@Inject @ConfigDir(sharedRoot = false) private Path configDir;

	@Inject @AsynchronousExecutor private SpongeExecutorService asyncExecutorService;

	private UserStorageService userStorageService;

	/** An instance of the plugin itself */
	private static GTS instance;

	/** The API specific fields for anything that extends outside the normal means of GTS */
	private GtsService service = new GtsServiceImpl();

	/** The configuration for GTS */
	private ConfigBase config;

	/** The message configuration for GTS */
	private ConfigBase msgConfig;

	/** The storage provider for the plugin */
	private Storage storage;

	private FileWatcher fileWatcher = null;

	/** The economy service present on the server */
	private EconomyService economy;

	/** An internal provider set to help decode variables in strings */
	private TextParsingUtils textParsingUtils = new TextParsingUtils();

	/** The cache holding all listings in the current running instance */
	private List<Listing> listingsCache = Lists.newArrayList();

	/** The cache holding all logs in the current running instance */
	private List<Log> logCache = Lists.newArrayList();

	/** The cache holding all temporary entries */
	private List<EntryHolder> heldEntryCache = Lists.newArrayList();

	/** The cache holding all temporary prices */
	private List<PriceHolder> heldPriceCache = Lists.newArrayList();

	/** A list of users who prefer not to be spammed by GTS broadcasts */
	private List<UUID> ignorers = Lists.newArrayList();

	/** The observable instance that allows the UIs to detect updates */
	private GuiUpdater updater = new GuiUpdater();

	private GTSBaseCmd cmd;
	private JoinListener joinListener;

	private IDiscordNotifier discordNotifier;

	/** The JSON writing/reading object with pretty printing. */
	public static final Gson prettyGson = new GsonBuilder()
			.setPrettyPrinting()
			.registerTypeAdapter(Entry.class, new EntryAdapter())
			.registerTypeAdapter(Price.class, new PriceAdapter())
			.registerTypeAdapter(DataContainer.class, new ItemAdapter())
			.create();

	/** Whether or not the plugin has passed preliminary checks */
	private boolean enabled;

	@Listener
	public void onReload(GameReloadEvent e) {
		this.config.reload();
		this.msgConfig.reload();
	}

	@Listener
	public void registerServices(ChangeServiceProviderEvent e){
		if(e.getService().equals(EconomyService.class)) {
			this.economy = (EconomyService) e.getNewProviderRegistration().getProvider();
		} else if(e.getService().equals(UserStorageService.class)) {
			this.userStorageService = (UserStorageService) e.getNewProviderRegistration().getProvider();
		} else if(e.getService().equals(NucleusMessageTokenService.class)) {
			this.service.registerTokenService();
		}
	}

	public static GTS getInstance() {
		return instance;
	}

	public Optional<ConsoleSource> getConsole() {
		return Optional.ofNullable(Sponge.isServerAvailable() ? Sponge.getServer().getConsole() : null);
	}

	public File getDataDirectory() {
		File root = configDir.toFile().getParentFile().getParentFile();
		File gtsDir = new File(root, "gts");
		gtsDir.mkdirs();
		return gtsDir;
	}

	public Optional<FileWatcher> getFileWatcher() {
		return Optional.ofNullable(this.fileWatcher);
	}

	public InputStream getResourceStream(String path) {
		return getClass().getClassLoader().getResourceAsStream(path);
	}

	@Override
	public Optional<PlanData> getPlanData() {
		return Optional.empty();
	}

	@Override
	public List<ConfigBase> getConfigs() {
		return Lists.newArrayList(config, msgConfig);
	}

	@Override
	public List<SpongeCommand> getCommands() {
		return Collections.singletonList(cmd);
	}

	@Override
	public List<Object> getListeners() {
		return Lists.newArrayList(joinListener);
	}

	@Override
	public void onDisconnect() {
		if(enabled) {
			getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Closing the storage provider...")));
			this.storage.shutdown();
			getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Storage provider closed, good bye!")));
		}
	}

	@Override
	public void onReload() {}

	@Listener(order = Order.EARLY)
	public void onPreInit(GamePreInitializationEvent e) {
		instance = this;
		this.logger = new ConsoleLogger(this, new SpongeLogger(this, fallback));
		GTSInfo.startup();
		getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Checking dependencies...")));
		enabled = GTSInfo.dependencyCheck();

		if(enabled) {
			getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Opening API and registering base setup...")));
			Sponge.getServiceManager().setProvider(this, GtsService.class, service);
			service.registerEntries(Lists.newArrayList(ItemEntry.class, PokemonEntry.class));
			service.registerPrices(Lists.newArrayList(MoneyPrice.class, ItemPrice.class, PokePrice.class));

			getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Pre-init phase complete!")));
		}
	}

	@Listener
	public void onInit(GameInitializationEvent e) {
		// Load the configuration
		if(enabled) {
			getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Now entering the init phase")));
			getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Loading configuration...")));
			this.config = new AbstractConfig(this, new AbstractConfigAdapter(this), new ConfigKeys(), "gts.conf");
			this.config.init();

			this.msgConfig = new AbstractConfig(this, new AbstractConfigAdapter(this), new MsgConfigKeys(), "messages.conf");
			this.msgConfig.init();

			//if(getConfig().get(ConfigKeys.WATCH_FILES)) {
			//	fileWatcher = new FileWatcher(this);
			//	Sponge.getScheduler().createTaskBuilder().async().intervalTicks(30).delayTicks(30).execute(fileWatcher).submit(GTS.getInstance());
			//}

			// Register the base command
			getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Initializing commands...")));
			(cmd = new GTSBaseCmd(this)).register(this);

			// Declare and activate listeners
			getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Initializing listeners...")));
			Sponge.getEventManager().registerListeners(this, joinListener = new JoinListener());

			// Launch the storage option chosen via the config
			this.storage = StorageFactory.getInstance(this, StorageType.H2);

			// Read in and register all data entries into the cache
			getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Loading data into cache...")));
			try {
				this.listingsCache = this.storage.getListings().get();
				this.heldEntryCache = this.storage.getHeldElements().get();
				this.heldPriceCache = this.storage.getHeldPrices().get();
				this.ignorers = this.storage.getIgnorers().get();
			} catch (InterruptedException | ExecutionException e1) {
				e1.printStackTrace();
			}

			if(this.config.get(ConfigKeys.DISCORD_ENABLED)) {
				getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Launching Discord Bot...")));
				this.discordNotifier = new DiscordNotifier();
			}

			getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Initialization complete!")));
		}
	}

	@Listener
	public void onServerStarted(GameStartedServerEvent e) {
		getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Post start-up phase has now started")));

		if(enabled) {
			ListingTasks.updateTask();
		}
	}

	@Listener
	public void onServerStopping(GameStoppingServerEvent e) {
		// Do shutdown procedures, such as saving content
		this.onDisconnect();
	}

	public Optional<IDiscordNotifier> getDiscordNotifier() {
		return Optional.ofNullable(this.discordNotifier);
	}
}
