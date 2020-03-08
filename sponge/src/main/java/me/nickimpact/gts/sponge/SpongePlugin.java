package me.nickimpact.gts.sponge;

import me.nickimpact.gts.common.plugin.GTSPlugin;
import me.nickimpact.gts.sponge.text.SpongeTokenService;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;

public interface SpongePlugin extends GTSPlugin {

	SpongeTokenService getTokenService();

	TextParsingUtils getTextParsingUtils();

	EconomyService getEconomy();

	PluginContainer getPluginContainer();

}
