package me.nickimpact.gts.spigot;

import me.nickimpact.gts.common.plugin.GTSPlugin;
import me.nickimpact.gts.spigot.tokens.TokenService;

public interface SpigotGTSPlugin extends GTSPlugin {

	TokenService getTokenService();

}
