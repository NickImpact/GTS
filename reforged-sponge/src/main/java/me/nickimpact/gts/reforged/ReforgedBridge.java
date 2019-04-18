package me.nickimpact.gts.reforged;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.nickimpact.impactor.api.commands.Command;
import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.api.logging.Logger;
import com.nickimpact.impactor.api.platform.Platform;
import com.nickimpact.impactor.api.plugin.ImpactorPlugin;
import com.nickimpact.impactor.api.plugin.PluginInfo;
import com.nickimpact.impactor.sponge.AbstractSpongePlugin;
import com.nickimpact.impactor.sponge.configuration.SpongeConfig;
import com.nickimpact.impactor.sponge.configuration.SpongeConfigAdapter;
import com.nickimpact.impactor.sponge.logging.SpongeLogger;
import lombok.Getter;
import me.nickimpact.gts.api.GtsService;
import me.nickimpact.gts.api.dependencies.DependencyManager;
import me.nickimpact.gts.api.dependencies.classloader.PluginClassLoader;
import me.nickimpact.gts.reforged.config.PokemonConfigKeys;
import me.nickimpact.gts.reforged.config.PokemonMsgConfigKeys;
import me.nickimpact.gts.reforged.entries.ReforgedEntry;
import me.nickimpact.gts.sponge.SpongePlugin;
import me.nickimpact.gts.sponge.TextParsingUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.economy.EconomyService;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

@Getter
@Plugin(id = "gts_reforged", name = "GTS Reforged Bridge", version = "1.1.2", dependencies = @Dependency(id = "gts"))
public class ReforgedBridge extends AbstractSpongePlugin implements SpongePlugin {

	@Getter private static ReforgedBridge instance;
	private GtsService service;

	@Inject
	private org.slf4j.Logger fallback;
	private Logger logger;

	@Inject
	@ConfigDir(sharedRoot = false)
	private Path configDir;
	private Config config;
	private Config msgConfig;

	private EconomyService ecomony;
	private TextParsingUtils textParsingUtils;

	@Listener
	public void onPreInit(GamePreInitializationEvent e) {
		instance = this;
		this.logger = new SpongeLogger(fallback);

		this.config = new SpongeConfig(new SpongeConfigAdapter(configDir.resolve("reforged.conf")), new PokemonConfigKeys());
		this.msgConfig = new SpongeConfig(new SpongeConfigAdapter(configDir.resolve("lang/reforged-en_us.conf")), new PokemonMsgConfigKeys());

		this.textParsingUtils = new TextParsingUtils(this);
	}

	@Listener
	public void onInit(GameInitializationEvent e) {
		service = Sponge.getServiceManager().provideUnchecked(GtsService.class);
		service.registerEntry(
				this.msgConfig.get(PokemonMsgConfigKeys.REFERENCE_TITLES),
				ReforgedEntry.class,
				null,
				"pixelmon:gs_ball",
				null
		);
	}

	@Listener
	public void onEconomyRegistration(ChangeServiceProviderEvent e) {
		if(e.getService().equals(EconomyService.class)) {
			this.ecomony = (EconomyService) e.getNewProviderRegistration().getProvider();
		}
	}

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
		return new PluginInfo() {
			@Override
			public String getID() {
				return "gts_reforged";
			}

			@Override
			public String getName() {
				return "GTS Reforged Bridge";
			}

			@Override
			public String getVersion() {
				return "1.1.0";
			}

			@Override
			public String getDescription() {
				return "";
			}
		};
	}

	@Override
	public Logger getPluginLogger() {
		return this.logger;
	}

	@Override
	public List<Config> getConfigs() {
		return Lists.newArrayList(config, msgConfig);
	}

	@Override
	public List<Command> getCommands() {
		return Lists.newArrayList();
	}

	@Override
	public List<Object> getListeners() {
		return Lists.newArrayList();
	}

	@Override
	public Consumer<ImpactorPlugin> onReload() {
		return plugin -> {};
	}

	@Override
	public TextParsingUtils getTextParsingUtils() {
		return this.textParsingUtils;
	}

	@Override
	public EconomyService getEconomy() {
		return null;
	}

	@Override
	public Config getConfiguration() {
		return null;
	}
}
