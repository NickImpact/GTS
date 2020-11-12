package net.impactdev.gts.common.discord;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.common.discord.internal.DiscordPlaceholderParser;
import net.impactdev.gts.common.discord.internal.DiscordSourceSpecificPlaceholderParser;
import net.impactdev.gts.common.utils.EconomicFormatter;
import net.impactdev.gts.common.utils.datetime.DateTimeFormatUtils;
import net.impactdev.gts.common.utils.lang.StringComposer;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.ConfigKey;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.config.updated.ConfigKeys;
import net.impactdev.gts.common.utils.future.CompletableFutureManager;
import net.impactdev.impactor.api.json.factory.JObject;
import net.impactdev.impactor.api.services.text.MessageService;
import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.net.ssl.HttpsURLConnection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DiscordNotifier {

	private GTSPlugin plugin;

	private MessageParser parser;

	public DiscordNotifier(GTSPlugin plugin) {
		this.plugin = plugin;
		this.initialize();
	}

	public Message forgeMessage(DiscordOption option, ConfigKey<List<String>> template, Listing listing, Object... additional) {
		Preconditions.checkArgument(!listing.getEntry().getDetails().isEmpty(), "Details must be specified for an entry");

		List<Supplier<Object>> sources = Lists.newArrayList();
		sources.add(() -> listing);
		for (Object o : additional) {
			sources.add(() -> o);
		}

		Field base = new Field("Listing Information", this.parser.interpret(this.plugin.getMsgConfig().get(template), sources), true);
		Field entry = new Field(listing.getEntry().getName().content(), StringComposer.composeListAsString(listing.getEntry().getDetails()), true);

		Embed.Builder embed = Embed.builder()
				.title(option.getDescriptor())
				.color(option.getColor().getRGB())
				.timestamp(LocalDateTime.now())
				.field(base)
				.field(entry);

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
					this.plugin.getPluginLogger().debug("[WebHook-Debug] Sending webhook payload to " + URL);
					this.plugin.getPluginLogger().debug("[WebHook-Debug] Payload: " + message.getJsonString());

					HttpsURLConnection connection = message.send(URL);
					int status = connection.getResponseCode();
					this.plugin.getPluginLogger().debug("[WebHook-Debug] Payload info received, status code: " + status);
				}
			}
		});
	}

	private void initialize() {
		this.parser = new MessageParser();
		this.parser.addPlaceholder(new DiscordSourceSpecificPlaceholderParser<>(
				Listing.class,
				"discord:publisher",
				listing -> GTSPlugin.getInstance().getPlayerDisplayName(listing.getLister())
		));
		this.parser.addPlaceholder(new DiscordSourceSpecificPlaceholderParser<>(
				Listing.class,
				"discord:publisher_id",
				listing -> listing.getLister().toString()
		));
		this.parser.addPlaceholder(new DiscordSourceSpecificPlaceholderParser<>(
				Listing.class,
				"discord:price",
				listing -> {
					if(!BuyItNow.class.isAssignableFrom(listing.getClass())) {
						return null;
					}

					BuyItNow bin = (BuyItNow) listing;
					TextComponent price = bin.getPrice().getText();
					StringBuilder out = new StringBuilder(price.content());
					for(Component child : price.children().stream().filter(x -> x instanceof TextComponent).collect(Collectors.toList())) {
						out.append(((TextComponent) child).content());
					}
					return out.toString();
				}
		));
		this.parser.addPlaceholder(new DiscordSourceSpecificPlaceholderParser<>(
				Listing.class,
				"discord:expiration",
				DateTimeFormatUtils::formatExpiration
		));
		this.parser.addPlaceholder(new DiscordSourceSpecificPlaceholderParser<>(
				UUID.class,
				"discord:actor",
				actor -> GTSPlugin.getInstance().getPlayerDisplayName(actor)
		));
		this.parser.addPlaceholder(new DiscordSourceSpecificPlaceholderParser<>(
				UUID.class,
				"discord:actor",
				UUID::toString
		));
		this.parser.addPlaceholder(new DiscordSourceSpecificPlaceholderParser<>(
				Double.class,
				"discord:bid",
				amount -> Impactor.getInstance().getRegistry().get(EconomicFormatter.class).format(amount)
		));
	}

	private static class MessageParser implements MessageService<String> {

		private static final Pattern TOKEN_LOCATOR = Pattern.compile("[{][{]([\\w-:]+)[}][}]");

		private final Map<String, DiscordPlaceholderParser> placeholders = Maps.newHashMap();

		public String interpret(List<String> base, List<Supplier<Object>> sources) {
			List<String> out = Lists.newArrayList();
			for(String s : base) {
				out.add(this.parse(s, sources));
			}

			return StringComposer.composeListAsString(out);
		}

		@Override
		public String parse(@NonNull String message, @NonNull List<Supplier<Object>> sources) {
			Matcher matcher = TOKEN_LOCATOR.matcher(message);

			AtomicReference<String> result = new AtomicReference<>(message);
			while(matcher.find()) {
				String placeholder = matcher.group(1);
				Optional.ofNullable(this.placeholders.get(placeholder.toLowerCase())).ifPresent(parser -> {
					String out = parser.parse(sources);
					if(out != null) {
						result.set(result.get().replaceAll("[{][{]" + placeholder + "[}][}]", out));
					}
				});
			}

			return result.get();
		}

		@Override
		public String getServiceName() {
			return "Discord Message Service Populator";
		}

		void addPlaceholder(DiscordPlaceholderParser parser) {
			this.placeholders.put(parser.getID(), parser);
		}

	}

}
