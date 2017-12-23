package com.nickimpact.gts.api.text;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.nickimpact.gts.GTS;
import com.nickimpact.gts.GTSInfo;
import com.nickimpact.gts.api.configuration.ConfigKeys;
import com.nickimpact.gts.api.exceptions.TokenAlreadyRegisteredException;
import com.nickimpact.gts.api.listings.Listing;
import com.nickimpact.gts.api.listings.pricing.Price;
import com.nickimpact.gts.api.time.Time;
import com.nickimpact.gts.entries.pixelmon.Pokemon;
import com.nickimpact.gts.internal.ItemTokens;
import com.nickimpact.gts.internal.PokemonTokens;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import io.github.nucleuspowered.nucleus.api.exceptions.PluginAlreadyRegisteredException;
import io.github.nucleuspowered.nucleus.api.service.NucleusMessageTokenService;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public final class Tokens implements NucleusMessageTokenService.TokenParser {

	private final Map<String, Translator> translatorMap = Maps.newHashMap();

	public Tokens() {
		translatorMap.put("player", (p, v, m) -> Optional.of(GTS.getInstance().getTextParsingUtils().getNameFromUser(
				getSourceFromVariableIfExists(p, v, m))
		));
		translatorMap.put("gts_prefix", (p, v, m) -> Optional.of(GTSInfo.PREFIX));
		translatorMap.put("balance", (p, v, m) -> Optional.of(GTS.getInstance().getTextParsingUtils().getBalance(
				getSourceFromVariableIfExists(p, v, m))
		));
		translatorMap.put("buyer", (p, v, m) -> Optional.of(GTS.getInstance().getTextParsingUtils().getNameFromUser(
				getSourceFromVariableIfExists(p, v, m))
		));
		translatorMap.put("seller", (p, v, m) -> Optional.of(GTS.getInstance().getTextParsingUtils().getNameFromUser(
				getSourceFromVariableIfExists(p, v, m))
		));
		translatorMap.put("price", (p, v, m) -> Optional.of(GTS.getInstance().getTextParsingUtils().getPriceInfo(
				getPriceFromVariableIfExists(m)
		)));
		translatorMap.put("increment", (p, v, m) -> Optional.of(GTS.getInstance().getTextParsingUtils().getPriceInfo(
				getPriceFromVariableIfExists(m)
		)));
		translatorMap.put("max_listings", (p, v, m) -> Optional.of(Text.of(GTS.getInstance().getConfig().get(ConfigKeys.MAX_LISTINGS))));
		translatorMap.put("id", (p, v, m) -> {
			Listing listing = getListingFromVaribleIfExists(m);
			return Optional.of(listing != null ? Text.of(listing.getID()) : Text.EMPTY);
		});
		translatorMap.put("time_left", (p, v, m) -> {
			Listing listing = getListingFromVaribleIfExists(m);
			if(listing == null)
				return Optional.of(Text.EMPTY);

			Date expiration = listing.getExpiration();
			Date now = Date.from(Instant.now());
			Time time = new Time(Duration.between(now.toInstant(), expiration.toInstant()).getSeconds());

			return Optional.of(Text.of(time.toString()));
		});
		translatorMap.putAll(PokemonTokens.getTokens());
		translatorMap.putAll(ItemTokens.getTokens());

		try {
			NucleusAPI.getMessageTokenService().register(
					GTS.getInstance().getPluginContainer(),
					this
			);
			this.getTokenNames().forEach(x -> NucleusAPI.getMessageTokenService().registerPrimaryToken(x.toLowerCase(), GTS.getInstance().getPluginContainer(), x.toLowerCase()));
		} catch (PluginAlreadyRegisteredException e) {
			e.printStackTrace();
		}
	}

	public void register(String key, Translator translator) throws TokenAlreadyRegisteredException {
		if(translatorMap.containsKey(key))
			throw new TokenAlreadyRegisteredException(key);

		translatorMap.put(key, translator);
		NucleusAPI.getMessageTokenService().registerPrimaryToken(key.toLowerCase(), GTS.getInstance().getPluginContainer(), key.toLowerCase());
	}

	private Set<String> getTokenNames() {
		return Sets.newHashSet(translatorMap.keySet());
	}

	private static CommandSource getSourceFromVariableIfExists(CommandSource source, String v, Map<String, Object> m) {
		if (m.containsKey(v) && m.get(v) instanceof CommandSource) {
			return (CommandSource)m.get(v);
		}

		return source;
	}

	private static Price getPriceFromVariableIfExists(Map<String, Object> m) {
		Optional<Object> opt = m.values().stream().filter(val -> val instanceof Price).findAny();
		return (Price) opt.orElse(null);
	}

	private static Listing getListingFromVaribleIfExists(Map<String, Object> m) {
		Optional<Object> opt = m.values().stream().filter(val -> val instanceof Listing).findAny();
		return (Listing) opt.orElse(null);
	}

	@Nonnull
	@Override
	public Optional<Text> parse(String tokenInput, CommandSource source, Map<String, Object> variables) {
		String[] split = tokenInput.split("\\|", 2);
		String var = "";
		if (split.length == 2) {
			var = split[1];
		}

		return translatorMap.getOrDefault(split[0].toLowerCase(), (p, v, m) -> Optional.empty()).get(source, var, variables);
	}
}
