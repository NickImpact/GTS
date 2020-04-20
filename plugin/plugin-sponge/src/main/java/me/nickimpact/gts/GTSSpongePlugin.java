package me.nickimpact.gts;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.SpongeCommandManager;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.api.logging.Logger;
import com.nickimpact.impactor.api.platform.Platform;
import com.nickimpact.impactor.api.plugin.ImpactorPlugin;
import com.nickimpact.impactor.api.plugin.PluginInfo;
import com.nickimpact.impactor.api.storage.StorageType;
import com.nickimpact.impactor.api.storage.dependencies.DependencyManager;
import com.nickimpact.impactor.api.storage.dependencies.classloader.PluginClassLoader;
import me.nickimpact.gts.api.GTSService;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.manager.ListingManager;
import me.nickimpact.gts.api.scheduling.SchedulerAdapter;
import me.nickimpact.gts.api.storage.GTSStorage;
import me.nickimpact.gts.commands.TestCommand;
import me.nickimpact.gts.common.messaging.MessagingFactory;
import me.nickimpact.gts.common.plugin.AbstractGTSPlugin;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import me.nickimpact.gts.common.tasks.SyncTask;
import me.nickimpact.gts.manager.SpongeListingManager;
import me.nickimpact.gts.messaging.SpongeMessagingFactory;
import me.nickimpact.gts.messaging.interpreters.SpongePingPongInterpreter;
import me.nickimpact.gts.sponge.SpongePlugin;
import me.nickimpact.gts.sponge.listings.SpongeQuickPurchase;
import me.nickimpact.gts.sponge.service.SpongeGtsService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public class GTSSpongePlugin extends AbstractGTSPlugin implements SpongePlugin {

	private GTSSpongeBootstrap bootstrap;

	private PluginInfo info = new GTSInfo();


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
		return null;
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
		return null;
	}
}
