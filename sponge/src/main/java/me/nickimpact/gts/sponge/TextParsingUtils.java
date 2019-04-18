package me.nickimpact.gts.sponge;

import com.google.common.collect.Lists;
import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.api.configuration.ConfigKey;
import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import io.github.nucleuspowered.nucleus.api.exceptions.NucleusException;
import io.github.nucleuspowered.nucleus.api.text.NucleusTextTemplate;
import me.nickimpact.gts.api.listings.prices.Price;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class TextParsingUtils {

	private SpongePlugin plugin;

	public TextParsingUtils(SpongePlugin plugin) {
		this.plugin = plugin;
	}

	private NucleusTextTemplate getTemplate(String text) throws NucleusException {
		return NucleusAPI.getMessageTokenService().createFromString(text);
	}

	private List<NucleusTextTemplate> getTemplates(List<String> text) throws NucleusException {
		List<NucleusTextTemplate> templates = Lists.newArrayList();
		for(String str : text) {
			templates.add(getTemplate(str));
		}

		return templates;
	}

	public Text parse(String template, CommandSource source, @Nullable Map<String, Function<CommandSource, Optional<Text>>> tokens, @Nullable Map<String, Object> variables) {
		try {
			return parse(getTemplate(template), source, tokens, variables);
		} catch (NucleusException e) {
			return Text.EMPTY;
		}
	}

	public List<Text> parse(Collection<String> templates, CommandSource source, @Nullable Map<String, Function<CommandSource, Optional<Text>>> tokens, @Nullable Map<String, Object> variables) {
		try {
			return parse(getTemplates(Lists.newArrayList(templates)), source, tokens, variables);
		} catch (NucleusException e) {
			return Lists.newArrayList();
		}
	}

	public Text parse(NucleusTextTemplate template, CommandSource source, @Nullable Map<String, Function<CommandSource, Optional<Text>>> tokens, @Nullable Map<String, Object> variables) {
		return template.getForCommandSource(source, tokens, variables);
	}

	public List<Text> parse(List<NucleusTextTemplate> templates, CommandSource source, @Nullable Map<String, Function<CommandSource, Optional<Text>>> tokens, @Nullable Map<String, Object> variables) {
		List<Text> output = Lists.newArrayList();
		for(NucleusTextTemplate template : templates) {
			output.add(parse(template, source, tokens, variables));
		}

		return output;
	}

	public Text getNameFromUser(CommandSource source) {
		return Text.of(source.getName());
	}

	public Text getBalance(CommandSource source) {
		if(source instanceof User) {
			EconomyService econ = plugin.getEconomy();

			Optional<UniqueAccount> acc = econ.getOrCreateAccount(((User) source).getUniqueId());
			if(acc.isPresent())
				return econ.getDefaultCurrency().format(acc.get().getBalance(econ.getDefaultCurrency()));
		}

		return Text.of(plugin.getEconomy().getDefaultCurrency().format(BigDecimal.ZERO));
	}

	public Text getPriceInfo(Price<Text> price) {
		if(price == null)
			return Text.EMPTY;

		return price.getText();
	}

	public Text fetchMsg(String def) {
		return TextSerializers.FORMATTING_CODE.deserialize(def);
	}

	public Text fetchMsg(ConfigKey<String> key) {
		return TextSerializers.FORMATTING_CODE.deserialize(plugin.getMsgConfig().get(key));
	}

	public Text fetchMsg(CommandSource source, String def) {
		return fetchAndParseMsg(source, def, null, null);
	}

	public Text fetchMsg(CommandSource source, ConfigKey<String> key) {
		return fetchAndParseMsg(source, key, null, null);
	}

	public Text fetchAndParseMsg(CommandSource source, String def, @Nullable Map<String, Function<CommandSource, Optional<Text>>> tokens, @Nullable Map<String, Object> variables) {
		return parse(def, source, tokens, variables);
	}

	public Text fetchAndParseMsg(CommandSource source, ConfigKey<String> key, @Nullable Map<String, Function<CommandSource, Optional<Text>>> tokens, @Nullable Map<String, Object> variables) {
		return parse(this.plugin.getMsgConfig().get(key), source, tokens, variables);
	}

	public List<Text> fetchMsgs(ConfigKey<List<String>> key) {
		List<Text> output = Lists.newArrayList();
		for(String str : this.plugin.getMsgConfig().get(key)) {
			output.add(TextSerializers.FORMATTING_CODE.deserialize(str));
		}
		return output;
	}

	public List<Text> fetchMsgs(CommandSource source, ConfigKey<List<String>> key) {
		return fetchAndParseMsgs(source, key, null, null);
	}

	public List<Text> fetchAndParseMsgs(CommandSource source, ConfigKey<List<String>> key, @Nullable Map<String, Function<CommandSource, Optional<Text>>> tokens, @Nullable Map<String, Object> variables) {
		return parse(this.plugin.getMsgConfig().get(key), source, tokens, variables);
	}

	public Text fetchAndParseMsg(CommandSource source, Config config, ConfigKey<String> key, @Nullable Map<String, Function<CommandSource, Optional<Text>>> tokens, @Nullable Map<String, Object> variables) {
		return parse(config.get(key), source, tokens, variables);
	}

	public List<Text> fetchAndParseMsgs(CommandSource source, Config config, ConfigKey<List<String>> key, @Nullable Map<String, Function<CommandSource, Optional<Text>>> tokens, @Nullable Map<String, Object> variables) {
		return parse(config.get(key), source, tokens, variables);
	}

}
