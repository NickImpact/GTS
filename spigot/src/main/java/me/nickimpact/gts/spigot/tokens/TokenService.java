package me.nickimpact.gts.spigot.tokens;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.api.configuration.ConfigKey;
import com.nickimpact.impactor.api.utilities.Time;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.prices.Price;
import me.nickimpact.gts.api.plugin.PluginInstance;
import me.nickimpact.gts.config.ConfigKeys;
import me.nickimpact.gts.config.MsgConfigKeys;
import me.nickimpact.gts.spigot.MoneyPrice;
import me.nickimpact.gts.spigot.SpigotGTSPlugin;
import me.nickimpact.gts.spigot.SpigotListing;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TokenService {

	private static final Pattern TOKEN = Pattern.compile("(^[^{]+)?([{][{][\\w-:]+[}][}])(.+)?");
	private static final Pattern SUFFIX_PATTERN = Pattern.compile(":([sp]+)$", Pattern.CASE_INSENSITIVE);
	private static final Function<String, Optional<String>> translate = str -> Optional.of(ChatColor.translateAlternateColorCodes('&', str));

	private final SpigotGTSPlugin plugin;
	private final Map<String, Translator> translators = Maps.newHashMap();

	public TokenService(SpigotGTSPlugin plugin) {
		this.plugin = plugin;
		translators.put("player", (p, v, m) -> {
			if(p instanceof Player) {
				return Optional.of(Bukkit.getPlayer(((Player) p).getUniqueId()).getDisplayName());
			} else {
				return Optional.of("-"); // Console or Command Block, typically
			}
		});
		translators.put("gts_prefix", (p, v, m) -> translate.apply(plugin.getMsgConfig().get(MsgConfigKeys.PREFIX)));
		translators.put("gts_error", (p, v, m) -> translate.apply(plugin.getMsgConfig().get(MsgConfigKeys.ERROR_PREFIX)));
		translators.put("balance", (p, v, m) -> Optional.of(getBalance(getSourceFromVariableIfExists(p, v, m))));
		translators.put("buyer", (p, v, m) -> Optional.of(getSourceFromVariableIfExists(p, v, m).getName()));
		translators.put("seller", (p, v, m) -> {
			SpigotListing listing = getListingFromVariableIfExists(m);
			if(listing == null)
				return Optional.empty();

			return Optional.of(Bukkit.getOfflinePlayer(listing.getOwnerUUID()).getName());
		});
		translators.put("price", (p, v, m) -> {
			SpigotListing listing = getListingFromVariableIfExists(m);
			if(listing == null)
				return Optional.empty();

			return Optional.of(((MoneyPrice) listing.getPrice()).getText());
		});
		translators.put("auc_price", (p, v, m) -> {
			SpigotListing listing = getListingFromVariableIfExists(m);
			if(listing == null)
				return Optional.empty();

			Price price = listing.getPrice();
			return Optional.of((String) price.getText());
		});
		translators.put("max_listings", (p, v, m) -> Optional.of("" + plugin.getConfiguration().get(ConfigKeys.MAX_LISTINGS)));
		translators.put("id", (p, v, m) -> {
			Listing listing = getListingFromVariableIfExists(m);
			return Optional.of(listing != null ? listing.getUuid().toString() : "");
		});
		translators.put("time_left", (p, v, m) -> {
			Listing listing = getListingFromVariableIfExists(m);
			if(listing == null)
				return Optional.empty();
			if(listing.getExpiration().equals(LocalDateTime.MAX)) {
				return Optional.of("Infinite");
			}

			LocalDateTime expiration = listing.getExpiration();
			LocalDateTime now = LocalDateTime.now();
			return Optional.of(new Time(Duration.between(now, expiration).getSeconds()).toString());
		});
		translators.put("listing_specifics", (p, v, m) -> {
			Listing listing = getListingFromVariableIfExists(m);
			if(listing == null) {
				return Optional.empty();
			}

			return Optional.of(process(listing.getEntry().getSpecsTemplate(), p, null, m));
		});
		translators.put("listing_name", (p, v, m) -> {
			Listing listing = getListingFromVariableIfExists(m);
			if(listing == null) {
				return Optional.empty();
			}

			return translate.apply(listing.getName());
		});
		translators.put("gts_max_price", (p, v, m) -> Optional.of(Bukkit.getServicesManager().getRegistration(Economy.class).getProvider().format(PluginInstance.getInstance().getConfiguration().get(ConfigKeys.MAX_MONEY_PRICE))));
	}

	@SuppressWarnings("unchecked")
	public <T> T process(ConfigKey<T> key, CommandSender source, Map<String, Function<CommandSender, Optional<String>>> tokens, Map<String, Object> variables) {
		return process(plugin.getMsgConfig(), key, source, tokens, variables);
	}

	public <T> T process(Config config, ConfigKey<T> key, CommandSender source, Map<String, Function<CommandSender, Optional<String>>> tokens, Map<String, Object> variables) {
		T base = config.get(key);
		if(base instanceof List) {
			List<String> out = Lists.newArrayList();
			for(String line : ((List<String>) base)) {
				out.add(this.process(line, source, tokens, variables));
			}

			return (T) out;
		} else {
			return (T) this.process((String) base, source, tokens, variables);
		}
	}

	public String process(String input, CommandSender source, Map<String, Function<CommandSender, Optional<String>>> tokens, Map<String, Object> variables) {
		String reference = input;
		List<String> arguments = Lists.newArrayList();
		while(!reference.isEmpty()) {
			Matcher m = TOKEN.matcher(reference);
			if(m.find()) {
				if(m.group(1) != null) {
					arguments.add(m.group(1));
					reference = reference.replaceFirst("^[^{]+", "");
				}

				String token = m.group(2);
				String out = tokens != null && tokens.containsKey(token.replace("{{", "").replace("}}", "")) ?
						tokens.get(token.replace("{{", "").replace("}}", "")).apply(source).orElse(token) :
						this.parse(source, token, variables).orElse(token);
				arguments.add(out);
				reference = reference.replaceFirst("[{][{][\\w-:]+[}][}]", "");
			} else {
				arguments.add(reference);
				break;
			}
		}

		StringBuilder sb = new StringBuilder();
		for(String arg : arguments) {
			sb.append(ChatColor.translateAlternateColorCodes('&', arg));
		}

		return sb.toString();
	}

	public void register(String token, Translator translator) throws AlreadyRegisteredException {
		Preconditions.checkArgument(token != null, "Token cannot be null");
		Preconditions.checkArgument(translator != null, "Translator cannot be null");

		if(this.translators.containsKey(token)) {
			throw new AlreadyRegisteredException(token);
		}

		this.translators.put(token, translator);
	}

	private Optional<String> parseToken(String token, CommandSender source, Map<String, Object> variables) {
		String[] split = token.split("\\|", 2);
		String var = "";
		if(split.length == 2) {
			var = split[1];
		}

		return this.translators.getOrDefault(split[0].toLowerCase(), (p, v, m) -> Optional.empty()).get(source, var, variables);
	}

	private Optional<String> parse(CommandSender source, String token, Map<String, Object> variables) {
		token = token.toLowerCase().trim().replace("{{", "").replace("}}", "");
		Matcher m = SUFFIX_PATTERN.matcher(token);
		boolean appendSpace = false;
		boolean prependSpace = false;

		if(m.find(0)) {
			String match = m.group(1).toLowerCase();
			appendSpace = match.contains("s");
			prependSpace = match.contains("p");

			token = token.replaceAll(SUFFIX_PATTERN.pattern(), "");
		}

		Optional<String> result = parseToken(token, source, variables);
		if(appendSpace) {
			result = result.map(x -> x.isEmpty() ? x : x + " ");
		}

		if(prependSpace) {
			result = result.map(x -> x.isEmpty() ? x : " " + x);
		}

		return result;
	}

	private static CommandSender getSourceFromVariableIfExists(CommandSender source, String token, Map<String, Object> variables) {
		if(variables.containsKey(token) && variables.get(token) instanceof CommandSender) {
			return (CommandSender) variables.get(token);
		}

		return source;
	}

	private static SpigotListing getListingFromVariableIfExists(Map<String, Object> variables) {
		Optional<SpigotListing> listing = variables.values().stream().filter(val -> val instanceof SpigotListing).map(val -> (SpigotListing) val).findAny();
		return listing.orElse(null);
	}

	private static String getBalance(CommandSender source) {
		Economy economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
		if(source instanceof OfflinePlayer) {
			return economy.format(economy.getBalance((OfflinePlayer) source));
		}

		return economy.format(0.0);
	}

}
