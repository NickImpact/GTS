package me.nickimpact.gts;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.nickimpact.impactor.api.commands.Command;
import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.api.logging.Logger;
import com.nickimpact.impactor.api.platform.Platform;
import com.nickimpact.impactor.api.plugin.ImpactorPlugin;
import com.nickimpact.impactor.api.plugin.PluginInfo;
import com.nickimpact.impactor.sponge.AbstractSpongePlugin;
import com.nickimpact.impactor.sponge.logging.SpongeLogger;
import lombok.Getter;
import me.nickimpact.gts.api.GtsService;
import me.nickimpact.gts.api.dependencies.DependencyManager;
import me.nickimpact.gts.api.dependencies.classloader.PluginClassLoader;
import me.nickimpact.gts.sponge.MainSpongePlugin;
import me.nickimpact.gts.sponge.TextParsingUtils;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.service.economy.EconomyService;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

@Getter
public class GTS extends AbstractSpongePlugin implements MainSpongePlugin {

	@Getter private static GTS instance;

	private GTSInfo info = new GTSInfo();

	@Inject
	private org.slf4j.Logger fallback;
	private SpongeLogger logger;

	@Inject
	@ConfigDir(sharedRoot = false)
	private Path configDir;

	private Config config;
	private Config msgConfig;

	private GtsService service;

	private EconomyService economy;
	private TextParsingUtils textParsingUtils;

	@Override
	public GtsService getAPIService() {
		return this.service;
	}

	@Override
	public ScheduledExecutorService getAsyncExecutor() {
		return null;
	}

	@Override
	public Gson getGson() {
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
	public Platform getPlatform() {
		return Platform.Sponge;
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
	public List<Command> getCommands() {
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
	public Config getConfiguration() {
		return this.config;
	}
}
