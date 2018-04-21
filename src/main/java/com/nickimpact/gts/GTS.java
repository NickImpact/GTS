package com.nickimpact.gts;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.nickimpact.gts.api.GtsService;
import com.nickimpact.gts.api.GtsServiceImpl;
import com.nickimpact.gts.api.listings.entries.EntryHolder;
import com.nickimpact.gts.commands.basic.HelpCmd;
import com.nickimpact.gts.api.configuration.GTSConfiguration;
import com.nickimpact.gts.api.listings.Listing;
import com.nickimpact.gts.api.listings.entries.Entry;
import com.nickimpact.gts.api.listings.entries.EntryAdapter;
import com.nickimpact.gts.api.listings.pricing.*;
import com.nickimpact.gts.commands.GTSBaseCmd;
import com.nickimpact.gts.configuration.AbstractConfig;
import com.nickimpact.gts.configuration.GTSConfigAdapter;
import com.nickimpact.gts.entries.items.ItemAdapter;
import com.nickimpact.gts.entries.items.ItemEntry;
import com.nickimpact.gts.entries.pixelmon.PokemonEntry;
import com.nickimpact.gts.entries.prices.ItemPrice;
import com.nickimpact.gts.entries.prices.MoneyPrice;
import com.nickimpact.gts.entries.prices.PokePrice;
import com.nickimpact.gts.internal.TextParsingUtils;
import com.nickimpact.gts.listeners.JoinListener;
import com.nickimpact.gts.logs.Log;
import com.nickimpact.gts.storage.Storage;
import com.nickimpact.gts.storage.StorageFactory;
import com.nickimpact.gts.storage.StorageType;
import com.nickimpact.gts.storage.dao.file.FileWatcher;
import com.nickimpact.gts.ui.updater.GuiUpdater;
import com.nickimpact.gts.utils.ListingTasks;
import io.github.nucleuspowered.nucleus.api.service.NucleusMessageTokenService;
import lombok.Getter;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.*;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
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
@Plugin(id = GTSInfo.ID, name = GTSInfo.NAME, version = GTSInfo.VERSION, description = GTSInfo.DESCRIPTION)
public class GTS {

	/** The containing instance of the plugin set by Sponge */
	@Inject private PluginContainer pluginContainer;

	@Inject private Logger logger;

	/** The pathing to the config directory for GTS */
	@Inject @ConfigDir(sharedRoot = false) private Path configDir;

	@Inject @AsynchronousExecutor private SpongeExecutorService asyncExecutorService;

	private UserStorageService userStorageService;

	/** An instance of the plugin itself */
	private static GTS instance;

	/** The API specific fields for anything that extends outside the normal means of GTS */
	private GtsService service;

	/** The configuration for GTS */
	private GTSConfiguration config;

	/** The message configuration for GTS */
	private GTSConfiguration msgConfig;

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

	/** The JSON writing/reading object with pretty printing. */
	public static final Gson prettyGson = new GsonBuilder()
			.setPrettyPrinting()
			.registerTypeAdapter(Entry.class, new EntryAdapter())
			.registerTypeAdapter(Price.class, new PriceAdapter())
			.registerTypeAdapter(DataContainer.class, new ItemAdapter())
			.create();

	/** Whether or not the plugin has passed preliminary checks */
	private boolean enabled;

	@Listener(order = Order.EARLY)
	@SuppressWarnings("unchecked")
	public void onPreInit(GamePreInitializationEvent e) {
		instance = this;
		GTSInfo.startup();
		getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Checking dependencies...")));
		enabled = GTSInfo.dependencyCheck();

		if(enabled) {
			getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Opening API and registering base setup...")));
			Sponge.getServiceManager().setProvider(this, GtsService.class, (service = new GtsServiceImpl()));
			service.registerEntries(Lists.newArrayList(ItemEntry.class, PokemonEntry.class));
			service.registerPrices(Lists.newArrayList(MoneyPrice.class, ItemPrice.class, PokePrice.class));

			getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Pre-init phase complete!")));
		}
	}

	@Listener
	public void onInit(GameInitializationEvent e) {
		// If the preliminary checks complete, enable the plugin
		if(enabled) {
			// Load the configuration
			getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Now entering the init phase")));
			getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Loading configuration...")));
			this.config = new AbstractConfig(this, new GTSConfigAdapter(this), "gts.conf");
			this.config.init();

			this.msgConfig = new AbstractConfig(this, new GTSConfigAdapter(this), "messages.conf");
			this.msgConfig.init();

			//if(getConfig().get(ConfigKeys.WATCH_FILES)) {
			//	fileWatcher = new FileWatcher(this);
			//	Sponge.getScheduler().createTaskBuilder().async().intervalTicks(30).delayTicks(30).execute(fileWatcher).submit(GTS.getInstance());
			//}

			// Register the base command
			getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Initializing commands...")));
			GTSBaseCmd base = new GTSBaseCmd();
			base.register();
			HelpCmd.updateCommand(base);

			// Declare and activate listeners
			getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Initializing listeners...")));
			Sponge.getEventManager().registerListeners(this, new JoinListener());

			// Launch the storage option chosen via the config
			this.storage = StorageFactory.getInstance(this, StorageType.H2);

			// Read in and register all data entries into the cache
			getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Loading data into cache...")));
			try {
				this.listingsCache = this.storage.getListings().get();
				this.logCache = this.storage.getLogs().get();
				this.heldEntryCache = this.storage.getHeldElements().get();
				this.heldPriceCache = this.storage.getHeldPrices().get();
				this.ignorers = this.storage.getIgnorers().get();
			} catch (InterruptedException | ExecutionException e1) {
				e1.printStackTrace();
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
	public void onReload(GameReloadEvent e) {
		this.config.reload();
		this.msgConfig.reload();
	}

	@Listener
	public void onStop(GameStoppingServerEvent e) {
		if(enabled) {
			// Do shutdown procedures, such as saving content
			this.storage.shutdown();
		}
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

	public GTSConfiguration getDefaultMsgConfig() {
		return msgConfig;
	}
}
