package me.nickimpact.gts.sponge;

import org.spongepowered.api.service.economy.EconomyService;

public interface MainSpongePlugin extends SpongePlugin {

	EconomyService getEconomy();

}
