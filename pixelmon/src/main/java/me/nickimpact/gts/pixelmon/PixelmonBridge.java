package me.nickimpact.gts.pixelmon;

import com.google.inject.Inject;
import com.nickimpact.impactor.api.logger.Logger;
import com.nickimpact.impactor.logging.ConsoleLogger;
import com.nickimpact.impactor.logging.SpongeLogger;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.GtsService;
import me.nickimpact.gts.api.text.Translator;
import me.nickimpact.gts.api.utils.ItemUtils;
import me.nickimpact.gts.pixelmon.entries.PokemonEntry;
import me.nickimpact.gts.pixelmon.text.NucleusPokemonTokens;
import me.nickimpact.gts.pixelmon.ui.PixelmonUI;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Map;

@Plugin(id = "gts_pixelmon", name = "GTS Pixelmon Bridge", version = "1.0.0", dependencies = @Dependency(id = "gts"))
public class PixelmonBridge {

	@Inject
	private org.slf4j.Logger fallback;
	private Logger logger;

	private GtsService service;

	@Listener
	public void onPreInit(GamePreInitializationEvent e) {
		logger = new ConsoleLogger(GTS.getInstance(), new SpongeLogger(GTS.getInstance(), fallback));
		service = Sponge.getServiceManager().provideUnchecked(GtsService.class);
		service.registerEntry(
				"Pokemon",
				PokemonEntry.class,
				new PixelmonUI(),
				"pixelmon:gs_ball"
		);
	}

	@Listener
	public void onServerStarted(GameStartingServerEvent e) {
		for(Map.Entry<String, Translator> token : NucleusPokemonTokens.getTokens().entrySet()) {
			if(!service.getTokensService().register(token.getKey(), token.getValue())) {
				logger.warn("Unable to register token {{" + token.getKey() + "}} as it's already registered!");
			}
		}
	}
}
