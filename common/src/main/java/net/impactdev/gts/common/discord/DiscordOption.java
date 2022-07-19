package net.impactdev.gts.common.discord;

import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;

import java.awt.*;
import java.util.List;
import java.util.NoSuchElementException;

public class DiscordOption {

	private final String descriptor;
	private final Color color;
	private final List<String> webhookChannels;

	public DiscordOption(String descriptor, Color color, List<String> webhookChannels) {
		this.descriptor = descriptor;
		this.color = color;
		this.webhookChannels = webhookChannels;
	}

	public String getDescriptor() {
		return this.descriptor;
	}

	public Color getColor() {
		return this.color;
	}

	public List<String> getWebhookChannels() {
		return this.webhookChannels;
	}

	public static DiscordOption fetch(Options option) {
		return GTSPlugin.instance().config()
				.orElseThrow(NoSuchElementException::new)
				.get(ConfigKeys.DISCORD_LINKS)
				.get(option);
	}

	public enum Options {
		List_BIN,
		List_Auction,
		Purchase,
		Remove,
		Bid,
		Claim,
	}
}
