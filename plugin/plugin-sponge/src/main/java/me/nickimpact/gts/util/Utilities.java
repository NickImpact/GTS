package me.nickimpact.gts.util;

import com.nickimpact.impactor.api.Impactor;
import com.nickimpact.impactor.api.configuration.ConfigKey;
import com.nickimpact.impactor.api.json.factory.JObject;
import com.nickimpact.impactor.api.services.text.MessageService;
import com.nickimpact.impactor.api.utilities.Time;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Map;
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


}
