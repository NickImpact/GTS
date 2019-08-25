package me.nickimpact.gts.generations;

import com.google.common.collect.Lists;
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
import me.nickimpact.gts.api.plugin.Extension;
import me.nickimpact.gts.api.plugin.PluginInstance;
import me.nickimpact.gts.generations.config.PokemonConfigKeys;
import me.nickimpact.gts.generations.config.PokemonMsgConfigKeys;
import me.nickimpact.gts.generations.entries.PokemonEntry;
import me.nickimpact.gts.generations.text.NucleusPokemonTokens;
import me.nickimpact.gts.generations.ui.PixelmonUI;
import me.nickimpact.gts.sponge.service.SpongeGtsService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

@Getter
@Plugin(id = "gts_generations", name = "GTS Generations Bridge", version = "5.0.3", dependencies = @Dependency(id = "gts"))
public class GenerationsBridge extends AbstractSpongePlugin implements Extension {

	@Getter private static GenerationsBridge instance;

    @Inject
    private org.slf4j.Logger fallback;
	private Logger logger;

	private SpongeGtsService service;

	private Path configDir;
	private Config config;
	private Config msgConfig;

	@Listener(order = Order.LATE)
    public void onPreInit(GamePreInitializationEvent e) {
    	instance = this;
        this.logger = new SpongeLogger(this, fallback);

	    this.configDir = PluginInstance.getInstance().getConfigDir();
	    this.config = new SpongeConfig(new SpongeConfigAdapter(this, configDir.resolve("generations.conf").toFile()), new PokemonConfigKeys());
	    this.msgConfig = new SpongeConfig(new SpongeConfigAdapter(this, configDir.resolve("lang/generations-en_us.conf").toFile()), new PokemonMsgConfigKeys());

	    service = (SpongeGtsService) Sponge.getServiceManager().provideUnchecked(GtsService.class);

	    List<String> identifiers = Lists.newArrayList(this.msgConfig.get(PokemonMsgConfigKeys.REFERENCE_TITLES));
	    identifiers.add("generations");
	    service.registerEntry(
			    identifiers,
			    PokemonEntry.class,
			    new PixelmonUI(),
			    "pixelmon:gs_ball",
			    PokemonEntry::execute
	    );

	    service.getAllDeprecatedTypes().add(me.nickimpact.gts.generations.deprecated.PokemonEntry.class);
    }

	@Listener
	public void onServerStarted(GameStartingServerEvent e) {
		service.registerTokens(new NucleusPokemonTokens());
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
				return "gts_generations";
			}

			@Override
			public String getName() {
				return "GTS Generations Bridge";
			}

			@Override
			public String getVersion() {
				return "5.0.3";
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
		return Lists.newArrayList();
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
    	return x -> {};
//		this.config.reload();
//		this.msgConfig.reload();
	}

	@Override
	public Path getConfigDir() {
		return this.configDir;
	}

	@Override
	public Config getConfiguration() {
		return this.config;
	}

	@Override
	public GtsService getAPIService() {
		return this.service;
	}
}
