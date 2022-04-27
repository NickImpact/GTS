package net.impactdev.gts.sponge.utils;

import net.impactdev.gts.api.util.groupings.SimilarPair;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.ConfigKey;
import net.impactdev.impactor.api.placeholders.PlaceholderSources;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.impactor.api.utilities.Time;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.config.types.time.TimeLanguageOptions;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.mariuszgromada.math.mxparser.Argument;
import org.spongepowered.plugin.PluginContainer;

import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class Utilities {

	private static PluginContainer container;

	@SuppressWarnings("unchecked")
	public static final MessageService PARSER = Impactor.getInstance().getRegistry().get(MessageService.class);

	public static PluginContainer getPluginContainer() {
		return container;
	}

	public static void setContainer(PluginContainer container) {
		if(container.metadata().id().equals("gts")) {
			Utilities.container = container;
		}
	}

	public static <T> T readMessageConfigOption(ConfigKey<T> key) {
		return GTSPlugin.instance().configuration().language().get(key);
	}

	public static Component parse(ConfigKey<String> key, PlaceholderSources sources) {
		return PARSER.parse(GTSPlugin.instance().configuration().language().get(key), sources);
	}

	public static List<Component> parseList(ConfigKey<List<String>> key, PlaceholderSources sources) {
		return GTSPlugin.instance().configuration().language().get(key).stream().map(x -> PARSER.parse(x, sources)).collect(Collectors.toList());
	}


	public static TextComponent translateTime(Time time) {
		long weeks = TimeUnit.SECONDS.toDays(time.getTime()) / 7;
		long days = TimeUnit.SECONDS.toDays(time.getTime()) % 7;
		long hours = TimeUnit.SECONDS.toHours(time.getTime()) % 24;
		long minutes = TimeUnit.SECONDS.toMinutes(time.getTime()) % 60;
		long seconds = time.getTime() % 60;

		BiFunction<TimeLanguageOptions, Long, String> measure = (key, value) -> {
			if(value > 1) {
				return key.getPlural();
			} else {
				return key.getSingular();
			}
		};

		StringJoiner joiner = new StringJoiner(" ");
		if(weeks > 0) {
			joiner.add(weeks + "").add(measure.apply(readMessageConfigOption(MsgConfigKeys.WEEKS), weeks));
		}

		if(days > 0) {
			joiner.add(days + "").add(measure.apply(readMessageConfigOption(MsgConfigKeys.DAYS), days));
		}

		if(hours > 0) {
			joiner.add(hours + "").add(measure.apply(readMessageConfigOption(MsgConfigKeys.HOURS), hours));
		}

		if(minutes > 0) {
			joiner.add(minutes + "").add(measure.apply(readMessageConfigOption(MsgConfigKeys.MINUTES), minutes));
		}

		if(seconds > 0) {
			joiner.add(seconds + "").add(measure.apply(readMessageConfigOption(MsgConfigKeys.SECONDS), seconds));
		}

		return LegacyComponentSerializer.legacyAmpersand().deserialize(joiner.toString());
	}

	public static TextComponent translateTimeHighest(Time time) {
		long weeks = TimeUnit.SECONDS.toDays(time.getTime()) / 7;
		long days = TimeUnit.SECONDS.toDays(time.getTime()) % 7;
		long hours = TimeUnit.SECONDS.toHours(time.getTime()) % 24;
		long minutes = TimeUnit.SECONDS.toMinutes(time.getTime()) % 60;
		long seconds = time.getTime() % 60;

		BiFunction<TimeLanguageOptions, Long, String> measure = (key, value) -> {
			if(value > 1) {
				return key.getPlural();
			} else {
				return key.getSingular();
			}
		};

		StringJoiner joiner = new StringJoiner(" ");
		if(weeks > 0) {
			joiner.add(weeks + "").add(measure.apply(readMessageConfigOption(MsgConfigKeys.WEEKS), weeks));
		}

		if(days > 0 && joiner.length() == 0) {
			joiner.add(days + "").add(measure.apply(readMessageConfigOption(MsgConfigKeys.DAYS), days));
		}

		if(hours > 0 && joiner.length() == 0) {
			joiner.add(hours + "").add(measure.apply(readMessageConfigOption(MsgConfigKeys.HOURS), hours));
		}

		if(minutes > 0 && joiner.length() == 0) {
			joiner.add(minutes + "").add(measure.apply(readMessageConfigOption(MsgConfigKeys.MINUTES), minutes));
		}

		if(seconds > 0 && joiner.length() == 0) {
			joiner.add(seconds + "").add(measure.apply(readMessageConfigOption(MsgConfigKeys.SECONDS), seconds));
		}

		return LegacyComponentSerializer.legacyAmpersand().deserialize(joiner.toString());
	}

	public static SimilarPair<Argument> calculateTimeFee(Time time) {
		long minutes = TimeUnit.SECONDS.toMinutes(time.getTime()) % 60;
		long hours = TimeUnit.SECONDS.toHours(time.getTime());

		return new SimilarPair<>(new Argument("hours", hours), new Argument("minutes", minutes));
	}
}
