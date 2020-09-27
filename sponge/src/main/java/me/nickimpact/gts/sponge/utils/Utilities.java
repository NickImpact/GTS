package me.nickimpact.gts.sponge.utils;

import com.nickimpact.impactor.api.Impactor;
import com.nickimpact.impactor.api.configuration.ConfigKey;
import com.nickimpact.impactor.api.json.factory.JObject;
import com.nickimpact.impactor.api.services.text.MessageService;
import com.nickimpact.impactor.api.utilities.Time;
import me.nickimpact.gts.common.config.MsgConfigKeys;
import me.nickimpact.gts.common.config.updated.types.time.TimeKey;
import me.nickimpact.gts.common.config.updated.types.time.TimeLanguageOptions;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import net.kyori.text.TextComponent;
import net.kyori.text.serializer.gson.GsonComponentSerializer;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Utilities {

	@SuppressWarnings("unchecked")
	public static final MessageService<Text> PARSER = Impactor.getInstance().getRegistry().get(MessageService.class);

	public static <T> T readMessageConfigOption(ConfigKey<T> key) {
		return GTSPlugin.getInstance().getMsgConfig().get(key);
	}

	public static Text parse(ConfigKey<String> key, List<Supplier<Object>> sources) {
		return PARSER.parse(GTSPlugin.getInstance().getMsgConfig().get(key), sources);
	}

	public static List<Text> parseList(ConfigKey<List<String>> key, List<Supplier<Object>> sources) {
		return GTSPlugin.getInstance().getMsgConfig().get(key).stream().map(x -> PARSER.parse(x, sources)).collect(Collectors.toList());
	}

	public static Text translateComponent(TextComponent component) {
		return TextSerializers.JSON.deserialize(GsonComponentSerializer.INSTANCE.serialize(component));
	}

	public static Text translateTime(Time time) {
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

		return TextSerializers.FORMATTING_CODE.deserialize(joiner.toString());
	}
}
