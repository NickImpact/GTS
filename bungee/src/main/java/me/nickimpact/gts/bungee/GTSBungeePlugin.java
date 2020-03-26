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
import me.nickimpact.gts.api.scheduling.SchedulerAdapter;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import me.nickimpact.gts.common.tasks.SyncTask;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public class GTSBungeePlugin implements GTSPlugin {

	@Override
	public GTSBungeeBootstrap getBootstrap() {
		return null;
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
	public Platform getPlatform() {
		return null;
	}

	@Override
	public PluginInfo getPluginInfo() {
		return null;
	}

	@Override
	public Logger getPluginLogger() {
		return null;
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
}
