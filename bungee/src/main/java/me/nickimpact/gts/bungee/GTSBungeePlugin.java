package me.nickimpact.gts.bungee;

import co.aikar.commands.BaseCommand;
import com.google.gson.Gson;
import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.api.logging.Logger;
import com.nickimpact.impactor.api.platform.Platform;
import com.nickimpact.impactor.api.plugin.ImpactorPlugin;
import com.nickimpact.impactor.api.plugin.PluginInfo;
import com.nickimpact.impactor.api.storage.StorageType;
import com.nickimpact.impactor.api.storage.dependencies.DependencyManager;
import com.nickimpact.impactor.api.storage.dependencies.classloader.PluginClassLoader;
import lombok.AllArgsConstructor;
import me.nickimpact.gts.api.GTSService;
import me.nickimpact.gts.api.scheduling.SchedulerAdapter;
import me.nickimpact.gts.api.storage.GTSStorage;
import me.nickimpact.gts.bungee.messaging.BungeeMessagingFactory;
import me.nickimpact.gts.common.messaging.MessagingFactory;
import me.nickimpact.gts.common.plugin.AbstractGTSPlugin;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import me.nickimpact.gts.common.tasks.SyncTask;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

@AllArgsConstructor
public class GTSBungeePlugin extends AbstractGTSPlugin implements GTSPlugin {

	private GTSBungeeBootstrap bootstrap;

	@Override
	public void preInit() {
		super.preInit();

		GTSService.getInstance().getRegistry().register(GTSPlugin.class, this);
	}

	@Override
	public GTSBungeeBootstrap getBootstrap() {
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
	public Platform getPlatform() {
		return null;
	}

	@Override
	public PluginInfo getPluginInfo() {
		return null;
	}

	@Override
	public Logger getPluginLogger() {
		return this.bootstrap.getPluginLogger();
	}

	@Override
	public List<Config> getConfigs() {
		return null;
	}

	@Override
	public List<BaseCommand> getCommands() {
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

	@Override
	public Config getMsgConfig() {
		return null;
	}

	@Override
	public MessagingFactory<?> getMessagingFactory() {
		return new BungeeMessagingFactory(this);
	}
}
