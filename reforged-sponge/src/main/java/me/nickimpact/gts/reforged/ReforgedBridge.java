package me.nickimpact.gts.reforged;

import co.aikar.commands.BaseCommand;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
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
import me.nickimpact.gts.reforged.config.PokemonConfigKeys;
import me.nickimpact.gts.reforged.config.PokemonMsgConfigKeys;
import me.nickimpact.gts.reforged.deprecated.PokemonEntry;
import me.nickimpact.gts.reforged.entries.ReforgedEntry;
import me.nickimpact.gts.reforged.entries.ReforgedUI;
import me.nickimpact.gts.reforged.text.PokemonTokens;
import me.nickimpact.gts.sponge.service.SpongeGtsService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.economy.EconomyService;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

@Getter
@Plugin(id = "gts_reforged", name = "GTS Reforged Bridge", version = "5.0.3", dependencies = @Dependency(id = "gts"))
public class ReforgedBridge extends AbstractSpongePlugin implements Extension {

	@Getter private static ReforgedBridge instance;
	private SpongeGtsService service;

	@Inject
	private org.slf4j.Logger fallback;
	private Logger logger;

	private Path configDir;
	private Config config;
	private Config msgConfig;

	private EconomyService ecomony;

	@Listener(order = Order.LATE)
	public void onPreInit(GamePreInitializationEvent e) {
		instance = this;
		this.logger = new SpongeLogger(this, fallback);

		this.configDir = PluginInstance.getInstance().getConfigDir();
		this.config = new SpongeConfig(new SpongeConfigAdapter(this, configDir.resolve("reforged.conf").toFile()), new PokemonConfigKeys());
		this.msgConfig = new SpongeConfig(new SpongeConfigAdapter(this, configDir.resolve("lang/reforged-en_us.conf").toFile()), new PokemonMsgConfigKeys());

		service = (SpongeGtsService) Sponge.getServiceManager().provideUnchecked(GtsService.class);

		List<String> identifiers = Lists.newArrayList(this.msgConfig.get(PokemonMsgConfigKeys.REFERENCE_TITLES));
		identifiers.add("reforged");
		service.registerEntry(
				identifiers,
				ReforgedEntry.class,
				new ReforgedUI(),
				"pixelmon:gs_ball",
				ReforgedEntry::execute
		);

		service.getAllDeprecatedTypes().add(PokemonEntry.class);
	}

	@Listener
	public void onServerStart(GameStartedServerEvent e) {
		service.registerTokens(new PokemonTokens());
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
		return Lists.newArrayList(config, msgConfig);
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
		return plugin -> {};
	}

	@Override
	public Config getConfiguration() {
		return this.config;
	}
}
