package me.nickimpact.gts;

import com.google.common.collect.Lists;
import com.nickimpact.impactor.api.plugin.PluginInfo;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;

import java.util.List;

public class GTSInfo implements PluginInfo {

	public static final String ID = "gts";
	public static final String NAME = "GTS";
	public static final String VERSION = "@version@";
	public static final String DESCRIPTION = "A marketing plugin set to allow players to sell there belongings";

	@Override
	public String getID() {
		return "gts";
	}

	@Override
	public String getName() {
		return "GTS";
	}

	@Override
	public String getVersion() {
		return "@version@";
	}

	@Override
	public String getDescription() {
		return "A marketing plugin set to allow players to sell there belongings";
	}

	void displayBanner() {
		List<String> output = Lists.newArrayList(
				"",
				"&3     _________________",
				"&3    / ____/_  __/ ___/       &aGTS " + this.getVersion(),
				"&3   / / __  / /  \\__ \\        &aRunning on: &e" + Sponge.getGame().getPlatform().getContainer(Platform.Component.IMPLEMENTATION).getName() + " " + Sponge.getGame().getPlatform().getContainer(Platform.Component.IMPLEMENTATION).getVersion().orElse(""),
				"&3  / /_/ / / /  ___/ /        &aAuthor: &3NickImpact",
				"&3  \\____/ /_/  /____/",
				""
		);

		GTSPlugin.getInstance().getPluginLogger().noTag(output);
	}

}
