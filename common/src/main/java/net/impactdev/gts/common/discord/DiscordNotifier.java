package net.impactdev.gts.common.discord;

import net.impactdev.gts.common.utils.lang.StringComposer;
import net.impactdev.impactor.api.configuration.ConfigKey;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.config.updated.ConfigKeys;
import net.impactdev.gts.common.utils.future.CompletableFutureManager;

import javax.net.ssl.HttpsURLConnection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DiscordNotifier {

	private GTSPlugin plugin;

	public DiscordNotifier(GTSPlugin plugin) {
		this.plugin = plugin;
	}

	public Message forgeMessage(DiscordOption option, ConfigKey<List<String>> template, Listing listing) {
		Field field = new Field(listing.getEntry().getName().content(), StringComposer.composeListAsString(listing.getEntry().getDetails()));
		Embed.Builder embed = Embed.builder()
				.title(option.getDescriptor())
				.color(option.getColor().getRGB())
				.field(field);

		listing.getEntry().getThumbnailURL().ifPresent(embed::thumbnail);

		return new Message(
				this.plugin.getConfiguration().get(ConfigKeys.DISCORD_TITLE),
				this.plugin.getConfiguration().get(ConfigKeys.DISCORD_AVATAR),
				option
		).addEmbed(embed.build());
	}

	public CompletableFuture<Void> sendMessage(Message message) {
		return CompletableFutureManager.makeFuture(() -> {
			if(this.plugin.getConfiguration().get(ConfigKeys.DISCORD_LOGGING_ENABLED)) {
				final List<String> URLS = message.getWebhooks();

				for (final String URL : URLS) {
					if (this.plugin.getConfiguration().get(ConfigKeys.DISCORD_DEBUG_ENABLED)) {
						this.plugin.getPluginLogger().info("[WebHook-Debug] Sending webhook payload to " + URL);
						this.plugin.getPluginLogger().info("[WebHook-Debug] Payload: " + message.getJsonString());
					}

					HttpsURLConnection connection = message.send(URL);
					int status = connection.getResponseCode();
					if (this.plugin.getConfiguration().get(ConfigKeys.DISCORD_DEBUG_ENABLED)) {
						this.plugin.getPluginLogger().info("[WebHook-Debug] Payload info received, status code: " + status);
					}
				}
			}
		});
	}

}
