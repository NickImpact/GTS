package me.nickimpact.gts.sponge;

import com.google.common.collect.Lists;
import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.api.configuration.ConfigKey;
import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import io.github.nucleuspowered.nucleus.api.placeholder.PlaceholderVariables;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TextParsingUtils {

	private SpongePlugin plugin;

	public TextParsingUtils(SpongePlugin plugin) {
		this.plugin = plugin;
	}

	private NucleusTextTemplate getTemplate(String text) {
		return NucleusAPI.getTextTemplateFactory().createFromString(text);
	}

	private List<NucleusTextTemplate> getTemplates(List<String> text) {
		List<NucleusTextTemplate> templates = Lists.newArrayList();
		for(String str : text) {
			templates.add(getTemplate(str));
		}

		return templates;
	}

	public Text parse(String template, CommandSource source) {
		return this.parse(template, source, null);
	}

	public Text parse(String template, CommandSource source, @Nullable Map<String, Function<CommandSource, Optional<Text>>> tokens) {
		return this.parse(template, source, tokens, PlaceholderVariables.empty());
	}

	public Text parse(String template, CommandSource source, @Nullable Map<String, Function<CommandSource, Optional<Text>>> tokens, PlaceholderVariables variables) {
		NucleusTextTemplate ntt = this.getTemplate(template);
		if(variables == null) {
			variables = PlaceholderVariables.empty();
		}
		return ntt.getForCommandSource(source, tokens, variables);
	}

	public List<Text> parse(List<String> templates, CommandSource source) {
		return this.parse(templates, source, null);
	}

	public List<Text> parse(List<String> templates, CommandSource source, @Nullable Map<String, Function<CommandSource, Optional<Text>>> tokens) {
		return this.parse(templates, source, tokens, PlaceholderVariables.empty());
	}

	public List<Text> parse(List<String> templates, CommandSource source, @Nullable Map<String, Function<CommandSource, Optional<Text>>> tokens, PlaceholderVariables variables) {
		return this.getTemplates(templates).stream().map(ntt -> ntt.getForCommandSource(source, tokens, variables)).collect(Collectors.toList());
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

	public Text fetchAndParseMsg(CommandSource source, String def, @Nullable Map<String, Function<CommandSource, Optional<Text>>> tokens, PlaceholderVariables variables) {
		return parse(def, source, tokens, variables);
	}

	public Text fetchAndParseMsg(CommandSource source, ConfigKey<String> key, @Nullable Map<String, Function<CommandSource, Optional<Text>>> tokens, PlaceholderVariables variables) {
		return parse(this.plugin.getMsgConfig().get(key), source, tokens, variables);
	}

	public List<Text> fetchAndParseMsgs(CommandSource source, ConfigKey<List<String>> key, @Nullable Map<String, Function<CommandSource, Optional<Text>>> tokens, PlaceholderVariables variables) {
		return parse(this.plugin.getMsgConfig().get(key), source, tokens, variables);
	}

	public Text fetchAndParseMsg(CommandSource source, Config config, ConfigKey<String> key, @Nullable Map<String, Function<CommandSource, Optional<Text>>> tokens, PlaceholderVariables variables) {
		return parse(config.get(key), source, tokens, variables);
	}

	public List<Text> fetchAndParseMsgs(CommandSource source, Config config, ConfigKey<List<String>> key, @Nullable Map<String, Function<CommandSource, Optional<Text>>> tokens, PlaceholderVariables variables) {
		return parse(config.get(key), source, tokens, variables);
	}

}
