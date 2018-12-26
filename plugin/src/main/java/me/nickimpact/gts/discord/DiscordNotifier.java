package me.nickimpact.gts.discord;

import com.google.common.base.Throwables;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.discord.IDiscordNotifier;
import me.nickimpact.gts.configuration.ConfigKeys;
import lombok.Getter;

import javax.net.ssl.HttpsURLConnection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Getter
public class DiscordNotifier implements IDiscordNotifier {

	@Override
	public Message forgeMessage(DiscordOption option, String content) {
		return new Message(null, GTS.getInstance().getConfig().get(ConfigKeys.DISCORD_TITLE), GTS.getInstance().getConfig().get(ConfigKeys.DISCORD_AVATAR), option)
				.addEmbed(new Embed(option.getColor().getRGB() & 16777215)
						.addField(new Field(option.getDescriptor(), content)));
	}

	@Override
	public CompletableFuture<Void> sendMessage(Message message) {
		return makeFuture(() -> {
			final List<String> URLS = message.getWebhooks();

			for(final String URL : URLS) {
				if (GTS.getInstance().getConfig().get(ConfigKeys.DISCORD_DEBUG)) {
					GTS.getInstance().getLogger().info("[WebHook-Debug] Sending webhook payload to " + URL);
					GTS.getInstance().getLogger().info("[WebHook-Debug] Payload: " + message.getJsonString());
				}

				HttpsURLConnection connection = message.send(URL);
				int status = connection.getResponseCode();
				if (GTS.getInstance().getConfig().get(ConfigKeys.DISCORD_DEBUG)) {
					GTS.getInstance().getLogger().info("[WebHook-Debug] Payload info received, status code: " + status);
				}
			}
		});
	}

	private <T> CompletableFuture<T> makeFuture(Callable<T> supplier) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return supplier.call();
			} catch (Exception e) {
				Throwables.propagateIfPossible(e);
				throw new CompletionException(e);
			}
		}, GTS.getInstance().getAsyncExecutorService());
	}

	private CompletableFuture<Void> makeFuture(DiscordNotifier.ThrowingRunnable runnable) {
		return CompletableFuture.runAsync(() -> {
			try {
				runnable.run();
			} catch (Exception e) {
				Throwables.propagateIfPossible(e);
				throw new CompletionException(e);
			}
		}, GTS.getInstance().getAsyncExecutorService());
	}

	private interface ThrowingRunnable {
		void run() throws Exception;
	}
}
