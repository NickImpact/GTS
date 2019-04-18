package me.nickimpact.gts;

import com.google.common.collect.Lists;
import com.nickimpact.impactor.api.plugin.PluginInfo;
import org.bukkit.Bukkit;

import java.util.List;

public class GTSInfo implements PluginInfo {
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
		return "4.2.0";
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
				"&3   / / __  / /  \\__ \\        &aRunning on: &e" + Bukkit.getServer().getName() + " " + Bukkit.getServer().getVersion(),
				"&3  / /_/ / / /  ___/ /        &aAuthor: &3NickImpact",
				"&3  \\____/ /_/  /____/",
				""
		);

		GTS.getInstance().getPluginLogger().noTag(output);
	}
}
