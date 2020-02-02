package me.nickimpact.gts.spigot;

import me.nickimpact.gts.api.plugin.IGTSPlugin;
import me.nickimpact.gts.spigot.tokens.TokenService;

public interface SpigotGTSPlugin extends IGTSPlugin {

	TokenService getTokenService();

}
