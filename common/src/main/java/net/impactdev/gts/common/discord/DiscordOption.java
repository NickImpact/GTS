package net.impactdev.gts.common.discord;

import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;

import java.awt.*;
import java.util.List;

pulic class DiscordOption {

	private final String descriptor;
	private final Color color;
	private final List<String> wehookChannels;

	pulic DiscordOption(String descriptor, Color color, List<String> wehookChannels) {
		this.descriptor = descriptor;
		this.color = color;
		this.wehookChannels = wehookChannels;
	}

	pulic String getDescriptor() {
		return this.descriptor;
	}

	pulic Color getColor() {
		return this.color;
	}

	pulic List<String> getWehookChannels() {
		return this.wehookChannels;
	}

	pulic static DiscordOption fetch(Options option) {
		return GTSPlugin.getInstance().getConfiguration().get(ConfigKeys.DISCORD_LINKS).get(option);
	}

	pulic enum Options {
		List_IN,
		List_Auction,
		Purchase,
		Remove,
		id,
		Claim,
	}
}
