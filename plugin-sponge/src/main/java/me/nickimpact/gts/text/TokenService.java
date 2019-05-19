package me.nickimpact.gts.text;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.nickimpact.impactor.api.utilities.Time;
import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import io.github.nucleuspowered.nucleus.api.exceptions.PluginAlreadyRegisteredException;
import io.github.nucleuspowered.nucleus.api.service.NucleusMessageTokenService;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.prices.Price;
import me.nickimpact.gts.config.ConfigKeys;
import me.nickimpact.gts.config.MsgConfigKeys;
import me.nickimpact.gts.sponge.MoneyPrice;
import me.nickimpact.gts.sponge.Translator;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class TokenService implements NucleusMessageTokenService.TokenParser {

	private final Map<String, Translator> translatorMap = Maps.newHashMap();

	public TokenService() {
		translatorMap.put("gts_prefix", (p, v, m) -> Optional.of(TextSerializers.FORMATTING_CODE.deserialize(
				GTS.getInstance().getMsgConfig().get(MsgConfigKeys.PREFIX)
		)));
		translatorMap.put("gts_error", (p, v, m) -> Optional.of(TextSerializers.FORMATTING_CODE.deserialize(
				GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ERROR_PREFIX)
		)));
		translatorMap.put("balance", (p, v, m) -> Optional.of(GTS.getInstance().getTextParsingUtils().getBalance(
				getSourceFromVariableIfExists(p, v, m))
		));
		translatorMap.put("buyer", (p, v, m) -> Optional.of(GTS.getInstance().getTextParsingUtils().getNameFromUser(
				getSourceFromVariableIfExists(p, v, m))
		));
		translatorMap.put("seller", (p, v, m) -> {
			Listing listing = getListingFromVaribleIfExists(m);
			if(listing == null)
				return Optional.of(Text.EMPTY);

			return Optional.of(Text.of(Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(listing.getOwnerUUID()).map(User::getName).orElse("???")));
		});
		translatorMap.put("price", (p, v, m) -> {
			Listing listing = getListingFromVaribleIfExists(m);
			if(listing == null)
				return Optional.empty();

			return Optional.of(((MoneyPrice) listing.getPrice()).getText());
		});
		translatorMap.put("auc_price", (p, v, m) -> {
			Listing listing = getListingFromVaribleIfExists(m);
			if(listing == null)
				return Optional.empty();

			Price price = listing.getPrice();
			return Optional.of(Text.of(price.getText()));
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

			LocalDateTime expiration = listing.getExpiration();
			LocalDateTime now = LocalDateTime.now();
			return Optional.of(Text.of(new Time(Duration.between(now, expiration).getSeconds()).toString()));
		});
		translatorMap.put("listing_specifics", (p, v, m) -> {
			Listing listing = getListingFromVaribleIfExists(m);
			if(listing == null) {
				return Optional.empty();
			}

			return Optional.of(GTS.getInstance().getTextParsingUtils().parse(listing.getEntry().getSpecsTemplate(), p, null, m));
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
