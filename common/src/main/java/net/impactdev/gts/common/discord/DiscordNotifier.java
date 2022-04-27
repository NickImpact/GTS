package net.impactdev.gts.common.discord;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.common.components.GTSFlattenerListener;
import net.impactdev.gts.common.discord.internal.DiscordPlaceholderParser;
import net.impactdev.gts.common.discord.internal.DiscordSourceSpecificPlaceholderParser;
import net.impactdev.gts.common.utils.EconomicFormatter;
import net.impactdev.gts.common.utils.datetime.DateTimeFormatUtils;
import net.impactdev.gts.common.utils.lang.StringComposer;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.configuration.ConfigKey;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.utils.future.CompletableFutureManager;
import net.impactdev.impactor.api.placeholders.PlaceholderSources;
import net.impactdev.impactor.api.services.text.MessageService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.flattener.FlattenerListener;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DiscordNotifier {

	private final GTSPlugin plugin;

	private MessageParser parser;

	public DiscordNotifier(GTSPlugin plugin) {
		this.plugin = plugin;
		this.initialize();
	}

	public CompletableFuture<Void> forgeAndSend(DiscordOption option, ConfigKey<List<String>> template, final Listing listing, final PlaceholderSources sources) {
		return CompletableFutureManager.makeFuture(() -> {
			Preconditions.checkArgument(!listing.getEntry().getDetails().isEmpty(), "Details must be specified for an entry");
			Config config = this.plugin.config().orElseThrow(NoSuchElementException::new);

			PlaceholderSources context = PlaceholderSources.builder()
					.from(sources)
					.appendIfAbsent(Listing.class, () -> listing)
					.build();

			Field base = new Field("Listing Information", this.parser.interpret(this.plugin.configuration().language().get(template), context), true);
			Field entry = new Field(StringComposer.readNameFromComponent(listing.getEntry().getName()), StringComposer.composeListAsString(listing.getEntry().getDetails()), true);

			Embed.Builder embed = Embed.builder()
					.title(option.getDescriptor())
					.color(option.getColor().getRGB())
					.timestamp(LocalDateTime.now())
					.field(base)
					.field(entry);

			listing.getEntry().getThumbnailURL().ifPresent(embed::thumbnail);

			Message message = new Message(
					config.get(ConfigKeys.DISCORD_TITLE),
					config.get(ConfigKeys.DISCORD_AVATAR),
					option
			).addEmbed(embed.build());

			if(config.get(ConfigKeys.DISCORD_LOGGING_ENABLED)) {
				final List<String> URLS = message.getWebhooks();

				for (final String URL : URLS) {
					this.plugin.logger().debug("[WebHook-Debug] Sending webhook payload to " + URL);
					this.plugin.logger().debug("[WebHook-Debug] Payload: " + message.getJsonString());

					HttpsURLConnection connection = message.send(URL);
					int status = connection.getResponseCode();
					this.plugin.logger().debug("[WebHook-Debug] Payload info received, status code: " + status);
				}
			}
		});
	}

	private void initialize() {
		this.parser = new MessageParser();
		this.parser.addPlaceholder(new DiscordSourceSpecificPlaceholderParser<>(
				Listing.class,
				"discord:listing_id",
				listing -> listing.getID().toString()
		));
		this.parser.addPlaceholder(new DiscordSourceSpecificPlaceholderParser<>(
				Listing.class,
				"discord:publisher",
				listing -> GTSPlugin.instance().playerDisplayName(listing.getLister()).join()
		));
		this.parser.addPlaceholder(new DiscordSourceSpecificPlaceholderParser<>(
				Listing.class,
				"discord:publisher_id",
				listing -> listing.getLister().toString()
		));
		this.parser.addPlaceholder(new DiscordSourceSpecificPlaceholderParser<>(
				BuyItNow.class,
				"discord:price",
				listing -> {
					TextComponent price = listing.getPrice().getText();
					StringBuilder out = new StringBuilder(price.content());
					for(Component child : price.children().stream().filter(x -> x instanceof TextComponent).collect(Collectors.toList())) {
						out.append(((TextComponent) child).content());
					}
					return out.toString();
				}
		));
		this.parser.addPlaceholder(new DiscordSourceSpecificPlaceholderParser<>(
				Auction.class,
				"discord:starting_bid",
				auction -> {
					final StringBuilder builder = new StringBuilder();
					FlattenerListener listener = new FlattenerListener() {

						@Override
						public void component(@NotNull String text) {
							builder.append(text);
						}
					};

					ComponentFlattener.textOnly().flatten(Impactor.getInstance().getRegistry().get(EconomicFormatter.class).format(auction.getStartingPrice()), listener);
					return builder.toString();
				}));
		this.parser.addPlaceholder(new DiscordSourceSpecificPlaceholderParser<>(
				Listing.class,
				"discord:expiration",
				DateTimeFormatUtils::formatExpiration
		));
		this.parser.addPlaceholder(new DiscordSourceSpecificPlaceholderParser<>(
				UUID.class,
				"discord:actor",
				actor -> GTSPlugin.instance().playerDisplayName(actor).join()
		));
		this.parser.addPlaceholder(new DiscordSourceSpecificPlaceholderParser<>(
				UUID.class,
				"discord:actor_id",
				UUID::toString
		));
		this.parser.addPlaceholder(new DiscordSourceSpecificPlaceholderParser<>(
				Double.class,
				"discord:bid",
				amount -> {
					GTSFlattenerListener listener = new GTSFlattenerListener();
					ComponentFlattener.textOnly().flatten(Impactor.getInstance().getRegistry().get(EconomicFormatter.class).format(amount), listener);
					return listener.result();
				}
		));
	}

	private static class MessageParser {

		private static final Pattern TOKEN_LOCATOR = Pattern.compile("[{][{]([\\w-:]+)[}][}]");

		private final Map<String, DiscordPlaceholderParser> placeholders = Maps.newHashMap();

		public String interpret(List<String> base, PlaceholderSources sources) {
			List<String> out = Lists.newArrayList();
			for(String s : base) {
				out.add(this.parse(s, sources));
			}

			return StringComposer.composeListAsString(out);
		}


		void addPlaceholder(DiscordPlaceholderParser parser) {
			this.placeholders.put(parser.getID(), parser);
		}

		public String parse(@NonNull String message, PlaceholderSources sources) {
			Matcher matcher = TOKEN_LOCATOR.matcher(message);

			AtomicReference<String> result = new AtomicReference<>(message);
			while(matcher.find()) {
				String placeholder = matcher.group(1);
				Optional.ofNullable(this.placeholders.get(placeholder.toLowerCase())).ifPresent(parser -> {
					String out = parser.parse(sources);
					if(out != null) {
						result.set(result.get().replace("{{" + placeholder + "}}", out));
					}
				});
			}

			return result.get();
		}
	}

}
