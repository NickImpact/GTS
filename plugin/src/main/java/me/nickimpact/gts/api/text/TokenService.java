package me.nickimpact.gts.api.text;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.configuration.ConfigKeys;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.time.Time;
import me.nickimpact.gts.configuration.MsgConfigKeys;
import me.nickimpact.gts.internal.ItemTokens;
import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import io.github.nucleuspowered.nucleus.api.exceptions.PluginAlreadyRegisteredException;
import io.github.nucleuspowered.nucleus.api.service.NucleusMessageTokenService;
import me.nickimpact.gts.internal.TextParsingUtils;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

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
public final class TokenService implements NucleusMessageTokenService.TokenParser {

	private final Map<String, Translator> translatorMap = Maps.newHashMap();

	public TokenService() {
		translatorMap.put("gts_prefix", (p, v, m) -> Optional.of(TextSerializers.FORMATTING_CODE.deserialize(
				GTS.getInstance().getMsgConfig().get(MsgConfigKeys.PREFIX)
		)));
		translatorMap.put("gts_error", (p, v, m) -> Optional.of(TextSerializers.FORMATTING_CODE.deserialize(
				GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ERROR_PREFIX)
		)));
		translatorMap.put("balance", (p, v, m) -> Optional.of(TextParsingUtils.getBalance(
				getSourceFromVariableIfExists(p, v, m))
		));
		translatorMap.put("buyer", (p, v, m) -> Optional.of(TextParsingUtils.getNameFromUser(
				getSourceFromVariableIfExists(p, v, m))
		));
		translatorMap.put("seller", (p, v, m) -> {
				Listing listing = getListingFromVaribleIfExists(m);
				if(listing == null)
					return Optional.of(Text.EMPTY);

				return Optional.of(Text.of(listing.getOwnerName()));
		});
		translatorMap.put("price", (p, v, m) -> {
			Listing listing = getListingFromVaribleIfExists(m);
			if(listing == null)
				return Optional.empty();

			return Optional.of(listing.getEntry().getPrice().getText());
		});
		translatorMap.put("max_listings", (p, v, m) -> Optional.of(Text.of(GTS.getInstance().getConfig().get(ConfigKeys.MAX_LISTINGS))));
		translatorMap.put("id", (p, v, m) -> {
			Listing listing = getListingFromVaribleIfExists(m);
			return Optional.of(listing != null ? Text.of(listing.getUuid()) : Text.EMPTY);
		});
		translatorMap.put("time_left", (p, v, m) -> {
			Listing listing = getListingFromVaribleIfExists(m);
			if(listing == null)
				return Optional.of(Text.EMPTY);

			Date expiration = listing.getExpiration();
			Date now = Date.from(Instant.now());
			return Optional.of(Text.of(new Time(Duration.between(now.toInstant(), expiration.toInstant()).getSeconds()).toString()));
		});
		translatorMap.put("listing_specifics", (p, v, m) -> {
			Listing listing = getListingFromVaribleIfExists(m);
			if(listing == null) {
				return Optional.empty();
			}

			return Optional.of(TextParsingUtils.parse(listing.getEntry().getSpecsTemplate(), p, null, m));
		});
		translatorMap.put("listing_name", (p, v, m) -> {
			Listing listing = getListingFromVaribleIfExists(m);
			if(listing == null) {
				return Optional.empty();
			}

			return Optional.of(TextSerializers.FORMATTING_CODE.deserialize(listing.getName()));
		});
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

	public boolean register(String key, Translator translator) {
		if(NucleusAPI.getMessageTokenService().registerPrimaryToken(key.toLowerCase(), GTS.getInstance().getPluginContainer(), key.toLowerCase())) {
			translatorMap.put(key, translator);
			return true;
		}

		return false;
	}

	public Set<String> getTokenNames() {
		return Sets.newHashSet(translatorMap.keySet());
	}

	private static CommandSource getSourceFromVariableIfExists(CommandSource source, String v, Map<String, Object> m) {
		if (m.containsKey(v) && m.get(v) instanceof CommandSource) {
			return (CommandSource)m.get(v);
		}

		return source;
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
