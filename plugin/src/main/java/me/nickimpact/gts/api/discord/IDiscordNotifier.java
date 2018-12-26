package me.nickimpact.gts.api.discord;

import me.nickimpact.gts.discord.DiscordOption;
import me.nickimpact.gts.discord.Message;

import java.util.concurrent.CompletableFuture;

public interface IDiscordNotifier {

	Message forgeMessage(DiscordOption option, String content);

	CompletableFuture<Void> sendMessage(Message message);
}
