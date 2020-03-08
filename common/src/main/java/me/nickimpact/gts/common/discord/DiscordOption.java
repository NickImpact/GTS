package me.nickimpact.gts.common.discord;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.*;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class DiscordOption {

	private final String descriptor;
	private final Color color;
	private final List<String> webhookChannels;

}
