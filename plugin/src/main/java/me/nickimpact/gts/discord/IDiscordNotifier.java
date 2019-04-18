package me.nickimpact.gts.discord;

import java.util.concurrent.CompletableFuture;

public interface IDiscordNotifier {

	Message forgeMessage(DiscordOption option, String content);

	CompletableFuture<Void> sendMessage(Message message);
}
