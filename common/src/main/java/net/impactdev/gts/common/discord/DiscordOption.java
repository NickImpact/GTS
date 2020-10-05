package net.impactdev.gts.common.discord;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.impactdev.gts.common.config.updated.ConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;

import java.awt.*;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class DiscordOption {

	private final String descriptor;
	private final Color color;
	private final List<String> webhookChannels;

	public static DiscordOption fetch(Options option) {
		return GTSPlugin.getInstance().getConfiguration().get(ConfigKeys.DISCORD_LINKS).get(option);
	}

	public enum Options {
		List,
		Purchase,
		Remove,
		Bid,
		Expire,
	}
}
