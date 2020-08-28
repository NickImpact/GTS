package me.nickimpact.gts.common.discord;

import com.google.common.base.Throwables;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import me.nickimpact.gts.common.config.ConfigKeys;
import me.nickimpact.gts.common.utils.exceptions.ExceptionWriter;

import javax.net.ssl.HttpsURLConnection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class DiscordNotifier {

	private GTSPlugin plugin;

	public DiscordNotifier(GTSPlugin plugin) {
		this.plugin = plugin;
	}

	public Message forgeMessage(DiscordOption option, String content) {
		return new Message(null, plugin.getConfiguration().get(ConfigKeys.DISCORD_TITLE), plugin.getConfiguration().get(ConfigKeys.DISCORD_AVATAR), option)
				.addEmbed(new Embed(option.getColor().getRGB() & 16777215)
						.addField(new Field(option.getDescriptor(), content)));
	}

	public CompletableFuture<Void> sendMessage(Message message) {
		return makeFuture(() -> {
			if(plugin.getConfiguration().get(ConfigKeys.DISCORD_ENABLED)) {
				final List<String> URLS = message.getWebhooks();

				for (final String URL : URLS) {
					if (plugin.getConfiguration().get(ConfigKeys.DISCORD_DEBUG)) {
						plugin.getPluginLogger().info("[WebHook-Debug] Sending webhook payload to " + URL);
						plugin.getPluginLogger().info("[WebHook-Debug] Payload: " + message.getJsonString());
					}

					HttpsURLConnection connection = message.send(URL);
					int status = connection.getResponseCode();
					if (plugin.getConfiguration().get(ConfigKeys.DISCORD_DEBUG)) {
						plugin.getPluginLogger().info("[WebHook-Debug] Payload info received, status code: " + status);
					}
				}
			}
		});
	}

	private <T> CompletableFuture<T> makeFuture(Callable<T> supplier) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return supplier.call();
			} catch (Exception e) {
				ExceptionWriter.write(e);
				Throwables.throwIfUnchecked(e);
				throw new CompletionException(e);
			}
		});
	}

	private CompletableFuture<Void> makeFuture(DiscordNotifier.ThrowingRunnable runnable) {
		return CompletableFuture.runAsync(() -> {
			try {
				runnable.run();
			} catch (Exception e) {
				ExceptionWriter.write(e);
				Throwables.throwIfUnchecked(e);
				throw new CompletionException(e);
			}
		});
	}

	private interface ThrowingRunnable {
		void run() throws Exception;
	}
}
