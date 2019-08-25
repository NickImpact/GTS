package me.nickimpact.gts.sponge;

import me.nickimpact.gts.api.plugin.IGTSPlugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;

public interface SpongePlugin extends IGTSPlugin {

	TextParsingUtils getTextParsingUtils();

	EconomyService getEconomy();

	PluginContainer getPluginContainer();

}
