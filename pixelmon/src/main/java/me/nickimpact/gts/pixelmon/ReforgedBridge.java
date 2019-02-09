package me.nickimpact.gts.pixelmon;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.nickimpact.impactor.api.commands.SpongeCommand;
import com.nickimpact.impactor.api.configuration.AbstractConfig;
import com.nickimpact.impactor.api.configuration.AbstractConfigAdapter;
import com.nickimpact.impactor.api.configuration.ConfigBase;
import com.nickimpact.impactor.api.logger.Logger;
import com.nickimpact.impactor.api.plugins.PluginInfo;
import com.nickimpact.impactor.api.plugins.SpongePlugin;
import com.nickimpact.impactor.api.services.plan.PlanData;
import com.nickimpact.impactor.logging.ConsoleLogger;
import com.nickimpact.impactor.logging.SpongeLogger;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import lombok.Getter;
import me.nickimpact.gts.api.GtsService;
import me.nickimpact.gts.api.events.DataReceivedEvent;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.text.Translator;
import me.nickimpact.gts.pixelmon.config.PokemonConfigKeys;
import me.nickimpact.gts.pixelmon.config.PokemonMsgConfigKeys;
import me.nickimpact.gts.pixelmon.entries.ReforgedEntry;
import me.nickimpact.gts.pixelmon.entries.removable.PokemonEntry;
import me.nickimpact.gts.pixelmon.text.NucleusPokemonTokens;
import me.nickimpact.gts.pixelmon.ui.PixelmonUI;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
@Plugin(id = "gts_reforged", name = "GTS Reforged Bridge", version = "1.0.4", dependencies = @Dependency(id = "gts"))
public class ReforgedBridge extends SpongePlugin {

	@Getter private static ReforgedBridge instance;

	@Inject
	private org.slf4j.Logger fallback;
	private Logger logger;

	private GtsService service;

	private File configDir = new File("config/gts/");
	private ConfigBase config;
	private ConfigBase msgConfig;

	@Listener
	public void onPreInit(GamePreInitializationEvent e) {
		instance = this;
		logger = new ConsoleLogger(this, new SpongeLogger(this, fallback));

		this.config = new AbstractConfig(this, new AbstractConfigAdapter(this), new PokemonConfigKeys(), "reforged.conf");
		this.config.init();
		this.msgConfig = new AbstractConfig(this, new AbstractConfigAdapter(this), new PokemonMsgConfigKeys(), "lang/reforged-en_us.conf");
		this.msgConfig.init();
	}

	@Listener
	public void onInit(GameInitializationEvent e) {
		service = Sponge.getServiceManager().provideUnchecked(GtsService.class);
		service.registerEntry(
				"Pokemon",
				ReforgedEntry.class,
				new PixelmonUI(),
				"pixelmon:gs_ball",
				ReforgedEntry::handleCommand
		);
		try {
			service.getRegistry(GtsService.RegistryType.ENTRY).register(PokemonEntry.class);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	@Listener
	public void onServerStarted(GameStartingServerEvent e) {
		for(Map.Entry<String, Translator> token : NucleusPokemonTokens.getTokens().entrySet()) {
			service.getTokensService().register(token.getKey(), token.getValue());
		}
	}

	@Listener
	public void onDataReceived(DataReceivedEvent e) {
		if(!e.filter(listing -> listing.getEntry() instanceof PokemonEntry).isEmpty()) {
			e.filterAndEdit(listing -> listing.getEntry() instanceof PokemonEntry, listings -> {
				for (Listing listing : listings) {
					listing.setEntry(new ReforgedEntry((Pokemon) listing.getEntry().getEntry(), listing.getEntry().getPrice()));
				}
			});
		}
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
				return "1.0.0";
			}

			@Override
			public String getDescription() {
				return "";
			}
		};
	}

	@Override
	public Optional<PlanData> getPlanData() {
		return Optional.empty();
	}

	@Override
	public List<ConfigBase> getConfigs() {
		return Lists.newArrayList();
	}

	@Override
	public List<SpongeCommand> getCommands() {
		return Lists.newArrayList();
	}

	@Override
	public List<Object> getListeners() {
		return Lists.newArrayList();
	}

	@Override
	public void onDisconnect() {}

	@Override
	public void onReload() {
		this.config.reload();
		this.msgConfig.reload();
	}

	@Override
	public Path getConfigDir() {
		return this.configDir.toPath();
	}
}
