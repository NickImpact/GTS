package me.nickimpact.gts.bungee;

import com.nickimpact.impactor.api.logging.Logger;
import com.nickimpact.impactor.api.storage.dependencies.classloader.PluginClassLoader;
import com.nickimpact.impactor.bungee.logging.BungeeLogger;
import me.nickimpact.gts.api.scheduling.SchedulerAdapter;
import me.nickimpact.gts.bungee.scheduling.BungeeSchedulerAdapter;
import me.nickimpact.gts.common.plugin.bootstrap.GTSBootstrap;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

public class GTSBungeeBootstrap extends Plugin implements GTSBootstrap {

	private GTSBungeePlugin plugin;

	private BungeeLogger logger;
	private BungeeSchedulerAdapter scheduler;

	private Throwable exception;

	public GTSBungeeBootstrap() {
		this.scheduler = new BungeeSchedulerAdapter(this);
		this.plugin = new GTSBungeePlugin(this);
	}

	@Override
	public void onLoad() {
		this.logger = new BungeeLogger(this.getLogger());
		try {
			this.plugin.preInit();
		} catch (Exception e) {
			exception = e;
			e.printStackTrace();
		}
		this.logger.info("&eTest");
	}

	@Override
	public void onEnable() {
		try {
			this.plugin.init();
		} catch (Exception e) {
			exception = e;
			e.printStackTrace();
		}

		try {
			this.plugin.started();
		} catch (Exception e) {
			exception = e;
			e.printStackTrace();
		}
	}

	@Override
	public Logger getPluginLogger() {
		return this.logger;
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
		return this.scheduler;
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
