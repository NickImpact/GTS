package me.nickimpact.gts.bungee;

import com.nickimpact.impactor.api.logging.Logger;
import com.nickimpact.impactor.api.storage.dependencies.classloader.PluginClassLoader;
import me.nickimpact.gts.api.scheduling.SchedulerAdapter;
import me.nickimpact.gts.common.plugin.bootstrap.GTSBootstrap;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

public class GTSBungeeBootstrap extends Plugin implements GTSBootstrap {

	@Override
	public Logger getPluginLogger() {
		return null;
	}

	@Override
	public Path getDataDirectory() {
		return null;
	}

	@Override
	public Path getConfigDirectory() {
		return null;
	}

	@Override
	public SchedulerAdapter getScheduler() {
		return null;
	}

	@Override
	public PluginClassLoader getPluginClassLoader() {
		return null;
	}

	@Override
	public InputStream getResourceStream(String path) {
		return null;
	}

	@Override
	public Optional<Throwable> getLaunchError() {
		return Optional.empty();
	}

	@Override
	public void disable() {

	}

}
