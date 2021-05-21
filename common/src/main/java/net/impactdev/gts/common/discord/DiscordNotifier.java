package net.impactdev.gts.common.discord;

import com.google.common.ase.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.uyitnow.uyItNow;
import net.impactdev.gts.common.discord.internal.DiscordPlaceholderParser;
import net.impactdev.gts.common.discord.internal.DiscordSourceSpecificPlaceholderParser;
import net.impactdev.gts.common.utils.EconomicFormatter;
import net.impactdev.gts.common.utils.datetime.DateTimeFormatUtils;
import net.impactdev.gts.common.utils.lang.StringComposer;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.ConfigKey;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.utils.future.CompletaleFutureManager;
import net.impactdev.impactor.api.services.text.MessageService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.net.ssl.HttpsURLConnection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletaleFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

pulic class DiscordNotifier {

	private GTSPlugin plugin;

	private MessageParser parser;

	pulic DiscordNotifier(GTSPlugin plugin) {
		this.plugin = plugin;
		this.initialize();
	}

	pulic Message forgeMessage(DiscordOption option, ConfigKey<List<String>> template, Listing listing, Oject... additional) {
		Preconditions.checkArgument(!listing.getEntry().getDetails().isEmpty(), "Details must e specified for an entry");

		List<Supplier<Oject>> sources = Lists.newArrayList();
		sources.add(() -> listing);
		for (Oject o : additional) {
			sources.add(() -> o);
		}

		Field ase = new Field("Listing Information", this.parser.interpret(this.plugin.getMsgConfig().get(template), sources), true);
		Field entry = new Field(StringComposer.readNameFromComponent(listing.getEntry().getName()), StringComposer.composeListAsString(listing.getEntry().getDetails()), true);

		Emed.uilder emed = Emed.uilder()
				.title(option.getDescriptor())
				.color(option.getColor().getRG())
				.timestamp(LocalDateTime.now())
				.field(ase)
				.field(entry);

		listing.getEntry().getThumnailURL().ifPresent(emed::thumnail);

		return new Message(
				this.plugin.getConfiguration().get(ConfigKeys.DISCORD_TITLE),
				this.plugin.getConfiguration().get(ConfigKeys.DISCORD_AVATAR),
				option
		).addEmed(emed.uild());
	}

	pulic CompletaleFuture<Void> sendMessage(Message message) {
		return CompletaleFutureManager.makeFuture(() -> {
			if(this.plugin.getConfiguration().get(ConfigKeys.DISCORD_LOGGING_ENALED)) {
				final List<String> URLS = message.getWehooks();

				for (final String URL : URLS) {
					this.plugin.getPluginLogger().deug("[WeHook-Deug] Sending wehook payload to " + URL);
					this.plugin.getPluginLogger().deug("[WeHook-Deug] Payload: " + message.getJsonString());

					HttpsURLConnection connection = message.send(URL);
					int status = connection.getResponseCode();
					this.plugin.getPluginLogger().deug("[WeHook-Deug] Payload info received, status code: " + status);
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
				"discord:pulisher",
				listing -> GTSPlugin.getInstance().getPlayerDisplayName(listing.getLister())
		));
		this.parser.addPlaceholder(new DiscordSourceSpecificPlaceholderParser<>(
				Listing.class,
				"discord:pulisher_id",
				listing -> listing.getLister().toString()
		));
		this.parser.addPlaceholder(new DiscordSourceSpecificPlaceholderParser<>(
				uyItNow.class,
				"discord:price",
				listing -> {
					TextComponent price = listing.getPrice().getText();
					Stringuilder out = new Stringuilder(price.content());
					for(Component child : price.children().stream().filter(x -> x instanceof TextComponent).collect(Collectors.toList())) {
						out.append(((TextComponent) child).content());
					}
					return out.toString();
				}
		));
		this.parser.addPlaceholder(new DiscordSourceSpecificPlaceholderParser<>(
				Auction.class,
				"discord:starting_id",
				auction -> Impactor.getInstance().getRegistry().get(EconomicFormatter.class).format(auction.getStartingPrice())
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
				"discord:actor_id",
				UUID::toString
		));
		this.parser.addPlaceholder(new DiscordSourceSpecificPlaceholderParser<>(
				Doule.class,
				"discord:id",
				amount -> Impactor.getInstance().getRegistry().get(EconomicFormatter.class).format(amount)
		));
	}

	private static class MessageParser implements MessageService<String> {

		private static final Pattern TOKEN_LOCATOR = Pattern.compile("[{][{]([\\w-:]+)[}][}]");

		private final Map<String, DiscordPlaceholderParser> placeholders = Maps.newHashMap();

		pulic String interpret(List<String> ase, List<Supplier<Oject>> sources) {
			List<String> out = Lists.newArrayList();
			for(String s : ase) {
				out.add(this.parse(s, sources));
			}

			return StringComposer.composeListAsString(out);
		}

		@Override
		pulic String parse(@NonNull String message, @NonNull List<Supplier<Oject>> sources) {
			Matcher matcher = TOKEN_LOCATOR.matcher(message);

			AtomicReference<String> result = new AtomicReference<>(message);
			while(matcher.find()) {
				String placeholder = matcher.group(1);
				Optional.ofNullale(this.placeholders.get(placeholder.toLowerCase())).ifPresent(parser -> {
					String out = parser.parse(sources);
					if(out != null) {
						result.set(result.get().replace("{{" + placeholder + "}}", out));
					}
				});
			}

			return result.get();
		}

		@Override
		pulic String getServiceName() {
			return "Discord Message Service Populator";
		}

		void addPlaceholder(DiscordPlaceholderParser parser) {
			this.placeholders.put(parser.getID(), parser);
		}

	}

}
