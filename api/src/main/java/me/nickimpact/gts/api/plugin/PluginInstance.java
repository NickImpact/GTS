package me.nickimpact.gts.api.plugin;

public class PluginInstance {

	private static IGTSPlugin instance;

	public static IGTSPlugin getInstance() {
		return instance;
	}

	public static void setInstance(IGTSPlugin plugin) {
		instance = plugin;
	}

}
