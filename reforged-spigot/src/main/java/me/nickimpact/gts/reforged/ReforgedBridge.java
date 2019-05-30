package me.nickimpact.gts.reforged;

import com.google.common.collect.Lists;
import com.nickimpact.impactor.api.commands.Command;
import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.api.logging.Logger;
import com.nickimpact.impactor.api.platform.Platform;
import com.nickimpact.impactor.api.plugin.ImpactorPlugin;
import com.nickimpact.impactor.api.plugin.PluginInfo;
import com.nickimpact.impactor.spigot.configuration.SpigotConfig;
import com.nickimpact.impactor.spigot.configuration.SpigotConfigAdapter;
import com.nickimpact.impactor.spigot.logging.SpigotLogger;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import lombok.Getter;
import me.nickimpact.gts.api.GtsService;
import me.nickimpact.gts.api.enums.CommandResults;
import me.nickimpact.gts.api.plugin.Extension;
import me.nickimpact.gts.reforged.config.ReforgedKeys;
import me.nickimpact.gts.reforged.entry.ReforgedEntry;
import me.nickimpact.gts.reforged.ui.ReforgedUI;
import me.nickimpact.gts.spigot.SpigotGtsService;
import me.nickimpact.gts.spigot.SpigotListing;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class ReforgedBridge extends JavaPlugin implements Extension, Listener {

	@Getter
	private static ReforgedBridge instance;

	private SpigotGtsService service;

	private Logger logger;

	private Path configDir;
	private Config config;

	@Override
	public void onLoad() {
		instance = this;
		this.logger = new SpigotLogger(this);
		this.logger.info("Integrating with GTS Service...");
		this.service = (SpigotGtsService) GtsService.getInstance();

		this.configDir = Paths.get("./plugins/config/GTS");
		this.config = new SpigotConfig(new SpigotConfigAdapter(this, new File(this.configDir.toFile(), "reforged.conf")), new ReforgedKeys());

		this.service.registerEntry(
				Lists.newArrayList("pokemon", "reforged"),
				ReforgedEntry.class,
				new ReforgedUI(),
				"PIXELMON_GS_BALL",
				(src, args) -> {
					if(src instanceof Player) {
						Player player = (Player) src;
						PlayerPartyStorage storage = Pixelmon.storageManager.getParty(player.getUniqueId());
						Pokemon first = storage.get(0);
						SpigotListing listing = SpigotListing.builder()
								.id(UUID.randomUUID())
								.owner(player.getUniqueId())
								.entry(new ReforgedEntry(first))
								.price(50000)
								.expiration(LocalDateTime.now().plusDays(1))
								.build();
						listing.publish(this, player.getUniqueId());
						return CommandResults.SUCCESSFUL;
					}

					return CommandResults.FAILED;
				}
		);
		this.logger.info("Integration successful!");
	}

	@Override
	public GtsService getAPIService() {
		return this.service;
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
	public Platform getPlatform() {
		return Platform.Spigot;
	}

	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo() {
			@Override
			public String getID() {
				return "gts-reforged-bridge";
			}

			@Override
			public String getName() {
				return "GTS Reforged Bridge";
			}

			@Override
			public String getVersion() {
				return "4.2.0";
			}

			@Override
			public String getDescription() {
				return "XXX";
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
