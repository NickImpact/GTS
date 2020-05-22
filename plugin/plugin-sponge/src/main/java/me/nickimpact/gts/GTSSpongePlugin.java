package me.nickimpact.gts;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.SpongeCommandManager;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.api.logging.Logger;
import com.nickimpact.impactor.api.platform.Platform;
import com.nickimpact.impactor.api.plugin.ImpactorPlugin;
import com.nickimpact.impactor.api.plugin.PluginInfo;
import com.nickimpact.impactor.api.storage.StorageType;
import com.nickimpact.impactor.api.storage.dependencies.DependencyManager;
import com.nickimpact.impactor.api.storage.dependencies.classloader.PluginClassLoader;
import com.nickimpact.impactor.sponge.configuration.SpongeConfig;
import com.nickimpact.impactor.sponge.configuration.SpongeConfigAdapter;
import me.nickimpact.gts.api.GTSService;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.placeholders.PlaceholderService;
import me.nickimpact.gts.api.scheduling.SchedulerAdapter;
import me.nickimpact.gts.api.storage.GTSStorage;
import me.nickimpact.gts.api.text.MessageService;
import me.nickimpact.gts.commands.TestCommand;
import me.nickimpact.gts.common.config.MsgConfigKeys;
import me.nickimpact.gts.common.messaging.MessagingFactory;
import me.nickimpact.gts.common.plugin.AbstractGTSPlugin;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import me.nickimpact.gts.common.tasks.SyncTask;
import me.nickimpact.gts.listings.SpongeItemEntry;
import me.nickimpact.gts.manager.SpongeListingManager;
import me.nickimpact.gts.messaging.SpongeMessagingFactory;
import me.nickimpact.gts.messaging.interpreters.SpongePingPongInterpreter;
import me.nickimpact.gts.sponge.SpongePlugin;
import me.nickimpact.gts.sponge.listings.SpongeQuickPurchase;
import me.nickimpact.gts.sponge.service.SpongeGtsService;
import me.nickimpact.gts.sponge.text.SpongeMessageService;
import me.nickimpact.gts.sponge.text.SpongePlaceholderService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public class GTSSpongePlugin extends AbstractGTSPlugin implements SpongePlugin {

	private GTSSpongeBootstrap bootstrap;

	private PluginInfo info = new GTSInfo();

	private Config msgConfig;

	public GTSSpongePlugin(GTSSpongeBootstrap bootstrap) {
		this.bootstrap = bootstrap;
	}

	@Override
	public void preInit() {
		super.preInit();

		GTSService.getInstance().getRegistry().register(GTSPlugin.class, this);
		((GTSInfo) this.getPluginInfo()).displayBanner();
		Sponge.getServiceManager().setProvider(this.bootstrap, GTSService.class, new SpongeGtsService(this));
		GTSService.getInstance().getRegistry().registerBuilderSupplier(Listing.ListingBuilder.class, SpongeQuickPurchase.SpongeListingBuilder::new);
		GTSService.getInstance().getRegistry().register(SpongeListingManager.class, new SpongeListingManager());
		GTSService.getInstance().getServiceManager().register(MessageService.class, new SpongeMessageService());
		GTSService.getInstance().getServiceManager().register(PlaceholderService.class, new SpongePlaceholderService(this));

		this.msgConfig = new SpongeConfig(new SpongeConfigAdapter(this, new File(this.getConfigDir().toFile(), "lang/en_us.conf")), new MsgConfigKeys());
	}

	@Override
	public void init() {
		super.init();

		SpongePingPongInterpreter.registerDecoders(this);
		SpongePingPongInterpreter.registerInterpreters(this);

		SpongeCommandManager commands = new SpongeCommandManager(this.bootstrap.getContainer());
		commands.registerCommand(new TestCommand());
	}

	@Override
	public void started() {
		super.started();

		ItemStack test = ItemStack.builder()
				.itemType(ItemTypes.BARRIER)
				.add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Testing"))
				.build();
		SpongeItemEntry testing = null;
		testing = new SpongeItemEntry(test.createSnapshot());
		this.getPluginLogger().debug(testing.getInternalData().toJson().toString());

	}

	@Override
	public MessagingFactory<?> getMessagingFactory() {
		return new SpongeMessagingFactory(this);
	}

	@Override
	public Platform getPlatform() {
		return Platform.Sponge;
	}

	public PluginInfo getPluginInfo() {
		return this.info;
	}

	@Override
	public Logger getPluginLogger() {
		return this.bootstrap.getPluginLogger();
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
		return gts -> {};
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

	@Override
	public EconomyService getEconomy() {
		return null;
	}

	@Override
	public PluginContainer getPluginContainer() {
		return this.bootstrap.getContainer();
	}

	@Override
	public GTSSpongeBootstrap getBootstrap() {
		return this.bootstrap;
	}

	@Override
	public Gson getGson() {
		return new GsonBuilder().create();
	}

	@Override
	public GTSStorage getStorage() {
		return null;
	}

	@Override
	public SchedulerAdapter getScheduler() {
		return this.bootstrap.getScheduler();
	}

	@Override
	public SyncTask.Buffer getSyncTaskBuffer() {
		return null;
	}

	@Override
	public Path getConfigDir() {
		return this.bootstrap.getConfigDirectory();
	}

	@Override
	public Config getConfiguration() {
		return null;
	}

	@Override
	public PluginClassLoader getPluginClassLoader() {
		return this.bootstrap.getPluginClassLoader();
	}

	@Override
	public DependencyManager getDependencyManager() {
		return null;
	}

	@Override
	public List<StorageType> getStorageTypes() {
		return Lists.newArrayList();
	}

	@Override
	public Config getMsgConfig() {
		return this.msgConfig;
	}
}
