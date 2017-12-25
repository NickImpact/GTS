package com.nickimpact.gts;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.nickimpact.gts.api.GtsAPI;
import com.nickimpact.gts.api.configuration.ConfigKeys;
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
import com.nickimpact.gts.internal.TextParsingUtils;
import com.nickimpact.gts.api.text.Tokens;
import com.nickimpact.gts.listeners.JoinListener;
import com.nickimpact.gts.logs.Log;
import com.nickimpact.gts.storage.Storage;
import com.nickimpact.gts.storage.StorageFactory;
import com.nickimpact.gts.storage.StorageType;
import com.nickimpact.gts.storage.dao.file.FileWatcher;
import com.nickimpact.gts.ui.updater.GuiUpdater;
import com.nickimpact.gts.utils.ListingTasks;
import com.nickimpact.gts.utils.LotUtils;
import lombok.Getter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.*;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.AsynchronousExecutor;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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

	/** The pathing to the config directory for GTS */
	@Inject @ConfigDir(sharedRoot = false) private Path configDir;

	@Inject
	@AsynchronousExecutor
	private SpongeExecutorService asyncExecutorService;

	/** An instance of the plugin itself */
	private static GTS instance;

	/** The API specific fields for anything that extends outside the normal means of GTS */
	private GtsAPI api = new GtsAPI();

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

		getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Opening API and registering base setup...")));
		this.api.insertIntoRegistry(this.api.getRegistry(Entry.class), Lists.newArrayList(ItemEntry.class, PokemonEntry.class));
		this.api.insertIntoRegistry(this.api.getRegistry(Price.class), Lists.newArrayList(MoneyPrice.class, ItemPrice.class, PokePrice.class));

		getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Pre-init phase complete!")));
	}

	@Listener
	public void onInit(GameInitializationEvent e) {
		// If the preliminary checks complete, enable the plugin
		if(enabled) {
			// Load the configuration
			getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Now entering the init phase")));
			api.setTokens(new Tokens());
			getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Loading configuration...")));
			// TODO - Load the config and whatnot here
			this.config = new AbstractConfig(this, new GTSConfigAdapter(this), "gts.conf"); // Regular config
			this.config.init();

			this.msgConfig = new AbstractConfig(this, new GTSConfigAdapter(this), "messages.conf"); // Base message config
			this.msgConfig.init();

			if(getConfig().get(ConfigKeys.WATCH_FILES)) {
				fileWatcher = new FileWatcher(this);
				Sponge.getScheduler().createTaskBuilder().async().intervalTicks(30).delayTicks(30).execute(fileWatcher).submit(GTS.getInstance());
			}

			// Register the base command
			getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Initializing commands...")));
			new GTSBaseCmd().register();

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

				List<Listing> temp = Lists.newArrayList(this.listingsCache);
				temp.sort(Comparator.comparing(Listing::getID));
				LotUtils.setListingID(temp.size() != 0 ? temp.get(temp.size() - 1).getID() : 0);

				List<Log> tmp = Lists.newArrayList(this.logCache);
				tmp.sort(Comparator.comparing(Log::getID));
				LotUtils.setLogID(tmp.size() != 0 ? tmp.get(tmp.size() - 1).getID() : 0);
			} catch (InterruptedException | ExecutionException e1) {
				e1.printStackTrace();
			}

			getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Initialization complete!")));
		}
	}

	@Listener
	public void onServerStarted(GameStartedServerEvent e) {
		getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Post start-up phase has now started")));
		getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Loading base entry icons...")));
		this.api.addDisplayOption(
				this.api.getEntryDisplays(),
				ItemEntry.class,
				ItemStack.builder()
						.itemType(ItemTypes.DIAMOND)
						.add(Keys.DISPLAY_NAME, Text.of(TextColors.YELLOW, "Item Entry"))
						.add(Keys.ITEM_LORE, Lists.newArrayList(
								Text.of(TextColors.GRAY, "Add an item from your inventory"),
								Text.of(TextColors.GRAY, "to the set of listings")
						))
						.build()
		);
		this.api.addDisplayOption(
				this.api.getEntryDisplays(),
				PokemonEntry.class,
				ItemStack.builder()
						.itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:poke_ball").get())
						.add(Keys.DISPLAY_NAME, Text.of(TextColors.YELLOW, "Pokemon Entry"))
						.add(Keys.ITEM_LORE, Lists.newArrayList(
								Text.of(TextColors.GRAY, "Add a pokemon from your party"),
								Text.of(TextColors.GRAY, "or PC to the set of listings")
						))
						.build()
		);

		this.api.addDisplayOption(
				this.api.getPriceDisplays(),
				ItemPrice.class,
				ItemStack.builder()
						.itemType(ItemTypes.DIAMOND)
						.add(Keys.DISPLAY_NAME, Text.of(TextColors.YELLOW, "Item Price"))
						.add(Keys.ITEM_LORE, Lists.newArrayList(
								Text.of(TextColors.GRAY, "Declare an item as your"),
								Text.of(TextColors.GRAY, "intended pricing option")
						))
						.build()
		);
		this.api.addDisplayOption(
				this.api.getPriceDisplays(),
				PokePrice.class,
				ItemStack.builder()
						.itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:poke_ball").get())
						.add(Keys.DISPLAY_NAME, Text.of(TextColors.YELLOW, "Pokemon Price"))
						.add(Keys.ITEM_LORE, Lists.newArrayList(
								Text.of(TextColors.GRAY, "Declare a pokemon as your"),
								Text.of(TextColors.GRAY, "intended pricing option")
						))
						.build()
		);
		this.api.addDisplayOption(
				this.api.getPriceDisplays(),
				MoneyPrice.class,
				ItemStack.builder()
						.itemType(ItemTypes.GOLD_INGOT)
						.add(Keys.DISPLAY_NAME, Text.of(TextColors.YELLOW, "Monetary Price"))
						.add(Keys.ITEM_LORE, Lists.newArrayList(
								Text.of(TextColors.GRAY, "Declare a monetary price as your"),
								Text.of(TextColors.GRAY, "intended pricing option")
						))
						.build()
		);

		if(enabled) {
			ListingTasks.updateTask();
		}
	}

	@Listener
	public void onStop(GameStoppingServerEvent e) {
		if(enabled) {
			// Do shutdown procedures, such as saving content
			this.storage.shutdown();
		}
	}

	@Listener
	public void registerEconomyService(ChangeServiceProviderEvent e){
		if(e.getService().equals(EconomyService.class)){
			this.economy = (EconomyService) e.getNewProviderRegistration().getProvider();
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
