package me.nickimpact.gts;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.api.service.NucleusMessageTokenService;
import me.nickimpact.gts.api.GtsService;
import me.nickimpact.gts.api.events.DataReceivedEvent;
import me.nickimpact.gts.api.text.TokenService;
import me.nickimpact.gts.entries.items.ui.ItemUI;
import me.nickimpact.gts.internal.GtsServiceImpl;
import me.nickimpact.gts.api.discord.IDiscordNotifier;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.entries.EntryAdapter;
import me.nickimpact.gts.commands.GTSBaseCmd;
import me.nickimpact.gts.configuration.ConfigKeys;
import me.nickimpact.gts.configuration.MsgConfigKeys;
import me.nickimpact.gts.entries.items.ItemAdapter;
import me.nickimpact.gts.entries.items.ItemEntry;
import me.nickimpact.gts.entries.prices.MoneyPrice;
import me.nickimpact.gts.discord.DiscordNotifier;
import me.nickimpact.gts.internal.TextParsingUtils;
import me.nickimpact.gts.listeners.JoinListener;
import me.nickimpact.gts.storage.Storage;
import me.nickimpact.gts.storage.StorageFactory;
import me.nickimpact.gts.storage.StorageType;
import me.nickimpact.gts.storage.dao.file.FileWatcher;
import me.nickimpact.gts.ui.updater.GuiUpdater;
import me.nickimpact.gts.utils.ListingTasks;
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
import lombok.Getter;
import me.nickimpact.gts.api.listings.pricing.Price;
import me.nickimpact.gts.api.listings.pricing.PriceAdapter;
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
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.AsynchronousExecutor;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.*;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.*;

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

	/** A list of users who prefer not to be spammed by GTS broadcasts */
	private List<UUID> ignorers = Lists.newArrayList();

	/** The observable instance that allows the UIs to detect updates */
	private GuiUpdater updater = new GuiUpdater();

	private GTSBaseCmd cmd;
	private JoinListener joinListener;

	private IDiscordNotifier discordNotifier;

	/** The JSON writing/reading object with pretty printing. */
	public static Gson prettyGson = new GsonBuilder()
			.setPrettyPrinting()
			.registerTypeAdapter(Entry.class, new EntryAdapter())
			.registerTypeAdapter(Price.class, new PriceAdapter())
			.registerTypeAdapter(DataContainer.class, new ItemAdapter())
			.create();

	/** Specifies whether an error occurred during startup preventing GTS from working correctly */
	private Throwable error = null;

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
			((GtsServiceImpl) this.service).setTokens(new TokenService());
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
		getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Closing the storage provider...")));
		this.storage.shutdown();
		getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Storage provider closed, good bye!")));
	}

	@Override
	public void onReload() {}

	@Listener(order = Order.EARLY)
	public void onPreInit(GamePreInitializationEvent e) {
		instance = this;
		this.logger = new ConsoleLogger(this, new SpongeLogger(this, fallback));
		GTSInfo.displayBanner();
		getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Registering GTS Service...")));
		Sponge.getServiceManager().setProvider(this, GtsService.class, service);

		getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Registering default implementations...")));
		service.registerEntry("Items", ItemEntry.class, new ItemUI(), ItemTypes.DIAMOND.getId(), ItemEntry::handleCommand);
		try {
			//noinspection unchecked
			service.getRegistry(GtsService.RegistryType.PRICE).register(MoneyPrice.class);
		} catch (Exception e1) {
			this.error = e1;
			this.disable();
			e1.printStackTrace();
		}

		getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Loading configuration...")));
		this.config = new AbstractConfig(this, new AbstractConfigAdapter(this), new ConfigKeys(), "gts.conf");
		this.config.init();

		this.msgConfig = new AbstractConfig(this, new AbstractConfigAdapter(this), new MsgConfigKeys(), "lang/en_us.conf");
		this.msgConfig.init();

		getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Pre-init phase complete!")));

	}

	@Listener
	public void onInit(GameInitializationEvent e) {
		// Load the configuration
		try {
			getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Now entering the init phase")));

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
			this.storage.getIgnorers().thenAccept(ignorers -> this.ignorers = ignorers);

			if(this.config.get(ConfigKeys.DISCORD_ENABLED)) {
				getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Launching Discord Bot...")));
				this.discordNotifier = new DiscordNotifier();
			}

			getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Initialization complete!")));
		} catch (Exception e1) {
			this.error = e1;
			this.disable();
			e1.printStackTrace();
		}
	}

	private void updateStorageForEdits(List<Listing> listings) {
		Iterator<Listing> iterator = listings.iterator();
		if(iterator.hasNext()) {
			this.sendUpdate(iterator.next(), iterator);
		}
	}

	private void sendUpdate(Listing listing, Iterator<Listing> iterator) {
		this.storage.updateListing(listing).thenAccept(x -> {
			if(iterator.hasNext()) {
				this.sendUpdate(iterator.next(), iterator);
			}
		});
	}

	@Listener
	public void onServerStarted(GameStartedServerEvent e) {
		getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Post start-up phase has now started")));
		if(this.economy == null) {
			getConsole().ifPresent(console -> console.sendMessage(Text.of(GTSInfo.ERROR, "No economy plugin detected, disabling the plugin...")));
			this.disable();
			return;
		}

		ListingTasks.updateTask();
		this.storage.getListings().thenAccept(listings -> {
			this.listingsCache = listings;
			DataReceivedEvent dre = new DataReceivedEvent(listings);
			Sponge.getEventManager().post(dre);

			if(dre.isEdited()) {
				this.updateStorageForEdits(listings);
			}
		});
	}

	@Listener
	public void onServerStopping(GameStoppingServerEvent e) {
		// Do shutdown procedures, such as saving content
		this.onDisconnect();
	}

	public Optional<IDiscordNotifier> getDiscordNotifier() {
		return Optional.ofNullable(this.discordNotifier);
	}

	private void disable() {
		Sponge.getEventManager().unregisterPluginListeners(this);
		Sponge.getCommandManager().getOwnedBy(this).forEach(Sponge.getCommandManager()::removeMapping);
		Sponge.getScheduler().getScheduledTasks(this).forEach(Task::cancel);

		// Re-register this to warn people about the error.
		Sponge.getEventManager().registerListener(this, GameStartedServerEvent.class, e -> errorOnStartup());
	}

	private void errorOnStartup() {
		Sponge.getServer().getConsole().sendMessages(getErrorMessage());
	}

	private List<Text> getErrorMessage() {
		List<Text> error = Lists.newArrayList();
		error.add(Text.of(TextColors.RED, "----------------------------"));
		error.add(Text.of(TextColors.RED, "-  GTS FAILED TO LOAD  -"));
		error.add(Text.of(TextColors.RED, "----------------------------"));
		error.add(Text.EMPTY);
		error.add(Text.of(TextColors.RED, "GTS encountered an error which prevented startup to succeed. All commands, listeners, and tasks have been halted..."));
		error.add(Text.of(TextColors.RED, "----------------------------"));
		if(this.error != null) {
			if(this.error instanceof IOException) {
				error.add(Text.of(TextColors.RED, "It appears that there is an error in your configuration file! The error is: "));
				error.add(Text.of(TextColors.RED, this.error.getMessage()));
				error.add(Text.of(TextColors.RED, "Please correct this and restart your server."));
				error.add(Text.of(TextColors.YELLOW, "----------------------------"));
			} else if(this.error instanceof SQLException) {
				error.add(Text.of(TextColors.RED, "It appears that there is an error with the GTS storage provider! The error is: "));
				error.add(Text.of(TextColors.RED, this.error.getMessage()));
				error.add(Text.of(TextColors.RED, "Please correct this and restart your server."));
				error.add(Text.of(TextColors.YELLOW, "----------------------------"));
			}

			error.add(Text.of(TextColors.YELLOW, "(The error that was thrown is shown below)"));

			try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
				this.error.printStackTrace(pw);
				pw.flush();
				String[] stackTrace = sw.toString().split("(\r)?\n");
				for (String s : stackTrace) {
					error.add(Text.of(TextColors.YELLOW, s));
				}
			} catch (IOException e) {
				this.error.printStackTrace();
			}
		}

		return error;
	}
}
