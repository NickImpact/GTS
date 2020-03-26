package me.nickimpact.gts;

import co.aikar.commands.BaseCommand;
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
import me.nickimpact.gts.api.scheduling.SchedulerAdapter;
import me.nickimpact.gts.common.plugin.AbstractGTSPlugin;
import me.nickimpact.gts.common.plugin.bootstrap.GTSBootstrap;
import me.nickimpact.gts.common.tasks.SyncTask;
import me.nickimpact.gts.sponge.SpongePlugin;
import me.nickimpact.gts.sponge.listings.SpongeListing;
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
		GTSService.getInstance().getRegistry().registerBuilderSupplier(Listing.ListingBuilder.class, SpongeListing.SpongeListingBuilder::new);
	}

	@Override
	public void init() {
		super.init();
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
		return null;
	}

	@Override
	public GTSBootstrap getBootstrap() {
		return this.bootstrap;
	}

	@Override
	public Gson getGson() {
		return null;
	}

	@Override
	public SchedulerAdapter getScheduler() {
		return null;
	}

	@Override
	public SyncTask.Buffer getSyncTaskBuffer() {
		return null;
	}

	@Override
	public Path getConfigDir() {
		return null;
	}

	@Override
	public Config getConfiguration() {
		return null;
	}

	@Override
	public PluginClassLoader getPluginClassLoader() {
		return null;
	}

	@Override
	public DependencyManager getDependencyManager() {
		return null;
	}

	@Override
	public List<StorageType> getStorageTypes() {
		return null;
	}

	@Override
	public Config getMsgConfig() {
		return null;
	}
}
