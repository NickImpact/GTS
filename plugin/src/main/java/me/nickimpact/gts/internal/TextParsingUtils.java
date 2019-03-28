package me.nickimpact.gts.internal;

import com.google.common.collect.Lists;
import com.nickimpact.impactor.api.configuration.ConfigBase;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.listings.pricing.Price;
import com.nickimpact.impactor.api.configuration.ConfigKey;
import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import io.github.nucleuspowered.nucleus.api.exceptions.NucleusException;
import io.github.nucleuspowered.nucleus.api.text.NucleusTextTemplate;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class TextParsingUtils {

	private static NucleusTextTemplate getTemplate(String text) throws NucleusException {
		return NucleusAPI.getMessageTokenService().createFromString(text);
	}

	private static List<NucleusTextTemplate> getTemplates(List<String> text) throws NucleusException {
		List<NucleusTextTemplate> templates = Lists.newArrayList();
		for(String str : text) {
			templates.add(getTemplate(str));
		}

		return templates;
	}

	public static Text parse(String template, CommandSource source, @Nullable Map<String, Function<CommandSource, Optional<Text>>> tokens, @Nullable Map<String, Object> variables) {
		try {
			return parse(getTemplate(template), source, tokens, variables);
		} catch (NucleusException e) {
			return Text.EMPTY;
		}
	}

	public static List<Text> parse(Collection<String> templates, CommandSource source, @Nullable Map<String, Function<CommandSource, Optional<Text>>> tokens, @Nullable Map<String, Object> variables) {
		try {
			return parse(getTemplates(Lists.newArrayList(templates)), source, tokens, variables);
		} catch (NucleusException e) {
			return Lists.newArrayList();
		}
	}

	public static Text parse(NucleusTextTemplate template, CommandSource source, @Nullable Map<String, Function<CommandSource, Optional<Text>>> tokens, @Nullable Map<String, Object> variables) {
		return template.getForCommandSource(source, tokens, variables);
	}

	public static List<Text> parse(List<NucleusTextTemplate> templates, CommandSource source, @Nullable Map<String, Function<CommandSource, Optional<Text>>> tokens, @Nullable Map<String, Object> variables) {
		List<Text> output = Lists.newArrayList();
		for(NucleusTextTemplate template : templates) {
			output.add(parse(template, source, tokens, variables));
		}

		return output;
	}

	public static Text getNameFromUser(CommandSource source) {
		return Text.of(source.getName());
	}

	public static Text getBalance(CommandSource source) {
		if(source instanceof User) {
			EconomyService econ = GTS.getInstance().getEconomy();

			Optional<UniqueAccount> acc = econ.getOrCreateAccount(((User) source).getUniqueId());
			if(acc.isPresent())
				return econ.getDefaultCurrency().format(acc.get().getBalance(econ.getDefaultCurrency()));
		}

		return Text.of(GTS.getInstance().getEconomy().getDefaultCurrency().format(BigDecimal.ZERO));
	}

	public static Text getPriceInfo(Price price) {
		if(price == null)
			return Text.EMPTY;

		return price.getText();
	}

	public static Text fetchMsg(String def) {
		return TextSerializers.FORMATTING_CODE.deserialize(def);
	}

	public static Text fetchMsg(ConfigKey<String> key) {
		return TextSerializers.FORMATTING_CODE.deserialize(GTS.getInstance().getMsgConfig().get(key));
	}

	public static Text fetchMsg(CommandSource source, String def) {
		return fetchAndParseMsg(source, def, null, null);
	}

	public static Text fetchMsg(CommandSource source, ConfigKey<String> key) {
		return fetchAndParseMsg(source, key, null, null);
	}

	public static Text fetchAndParseMsg(CommandSource source, String def, @Nullable Map<String, Function<CommandSource, Optional<Text>>> tokens, @Nullable Map<String, Object> variables) {
		return parse(def, source, tokens, variables);
	}

	public static Text fetchAndParseMsg(CommandSource source, ConfigKey<String> key, @Nullable Map<String, Function<CommandSource, Optional<Text>>> tokens, @Nullable Map<String, Object> variables) {
		return parse(GTS.getInstance().getMsgConfig().get(key), source, tokens, variables);
	}

	public static List<Text> fetchMsgs(ConfigKey<List<String>> key) {
		List<Text> output = Lists.newArrayList();
		for(String str : GTS.getInstance().getMsgConfig().get(key)) {
			output.add(TextSerializers.FORMATTING_CODE.deserialize(str));
		}
		return output;
	}

	public static List<Text> fetchMsgs(CommandSource source, ConfigKey<List<String>> key) {
		return fetchAndParseMsgs(source, key, null, null);
	}

	public static List<Text> fetchAndParseMsgs(CommandSource source, ConfigKey<List<String>> key, @Nullable Map<String, Function<CommandSource, Optional<Text>>> tokens, @Nullable Map<String, Object> variables) {
		return parse(GTS.getInstance().getMsgConfig().get(key), source, tokens, variables);
	}

	public static Text fetchAndParseMsg(CommandSource source, ConfigBase config, ConfigKey<String> key, @Nullable Map<String, Function<CommandSource, Optional<Text>>> tokens, @Nullable Map<String, Object> variables) {
		return parse(config.get(key), source, tokens, variables);
	}

	public static List<Text> fetchAndParseMsgs(CommandSource source, ConfigBase config, ConfigKey<List<String>> key, @Nullable Map<String, Function<CommandSource, Optional<Text>>> tokens, @Nullable Map<String, Object> variables) {
		return parse(config.get(key), source, tokens, variables);
	}
}
