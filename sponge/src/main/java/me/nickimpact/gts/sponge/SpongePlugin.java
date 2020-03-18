package me.nickimpact.gts.sponge;

import me.nickimpact.gts.api.GtsService;
import me.nickimpact.gts.api.registry.GTSRegistry;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;

public interface SpongePlugin extends GTSPlugin {

	static SpongePlugin getInstance() {
		return GtsService.getInstance().getRegistry().get(SpongePlugin.class);
	}

	EconomyService getEconomy();

	PluginContainer getPluginContainer();

}
