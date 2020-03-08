package me.nickimpact.gts.sponge.text;

import com.google.common.reflect.TypeToken;
import com.nickimpact.impactor.api.utilities.Time;
import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import io.github.nucleuspowered.nucleus.api.placeholder.Placeholder;
import io.github.nucleuspowered.nucleus.api.placeholder.PlaceholderParser;
import io.github.nucleuspowered.nucleus.api.placeholder.PlaceholderVariables;
import me.nickimpact.gts.config.ConfigKeys;
import me.nickimpact.gts.config.MsgConfigKeys;
import me.nickimpact.gts.sponge.MoneyPrice;
import me.nickimpact.gts.sponge.SpongeListing;
import me.nickimpact.gts.sponge.SpongePlugin;
import me.nickimpact.gts.sponge.text.placeholders.ListingPlaceholderVariableKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Function;

public final class SpongeTokenService {

	private SpongePlugin plugin;

	public static final ListingPlaceholderVariableKey listingKey = new ListingPlaceholderVariableKey();

	public SpongeTokenService(SpongePlugin plugin) {
		this.plugin = plugin;
		this.register("gts_prefix", placeholder -> Text.of(plugin.getMsgConfig().get(MsgConfigKeys.PREFIX)));
		this.register("gts_error", placeholder -> Text.of(plugin.getMsgConfig().get(MsgConfigKeys.ERROR_PREFIX)));
		this.register("balance", new RequiredSourcePlaceholderParser(src -> plugin.getTextParsingUtils().getBalance(src)));
		this.register("buyer", new RequiredSourcePlaceholderParser(src -> plugin.getTextParsingUtils().getNameFromUser(src)));
		this.register("seller", placeholder -> {
			Optional<Listing> listing = placeholder.getVariables().get(listingKey);
			if(listing.isPresent()) {
				return Text.of(Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(listing.get().getOwnerUUID()).map(User::getName).orElse("Unknown"));
			}
			return Text.EMPTY;
		});
		this.register("max_listings", placeholder -> Text.of(plugin.getConfiguration().get(ConfigKeys.MAX_LISTINGS)));
		this.register("price", placeholder -> getOrDefault(placeholder, listingKey, listing -> ((MoneyPrice) listing.getPrice()).getText()));
		this.register("id", placeholder -> getOrDefault(placeholder, listingKey, listing -> Text.of(listing.getUuid().toString())));
		this.register("time_left", placeholder -> getOrDefault(placeholder, listingKey, listing -> {
			if(listing.getExpiration().equals(LocalDateTime.MAX)) {
				return Text.of("Infinite");
			}

			LocalDateTime expiration = listing.getExpiration();
			LocalDateTime now = LocalDateTime.now();
			return Text.of(new Time(Duration.between(now, expiration).getSeconds()).toString());
		}));
		this.register("listing_specifics", placeholder -> getOrDefault(placeholder, listingKey, listing -> plugin.getTextParsingUtils().parse(listing.getEntry().getSpecsTemplate(), placeholder.getAssociatedSource().orElse(Sponge.getServer().getConsole()))));
		this.register("listing_name", placeholder -> getOrDefault(placeholder, listingKey, listing -> Text.of(listing.getName())));
	}

	public void register(String key, PlaceholderParser parser) {
		NucleusAPI.getPlaceholderService().registerToken(plugin.getPluginContainer(), key.toLowerCase(), parser);
	}

	public <T> Text getOrDefault(Placeholder.Standard placeholder, PlaceholderVariables.Key<T> key, Function<T, Text> handler) {
		Optional<T> instance = placeholder.getVariables().get(key);
		if(instance.isPresent()) {
			return handler.apply(instance.get());
		} else {
			Optional<SpongeListing> listing = placeholder.getVariables().get(listingKey).map(l -> (SpongeListing) l);
			if(listing.isPresent()) {
				if(TypeToken.of(listing.get().getEntry().getEntry().getClass()).equals(key.getValueClass())) {
					return handler.apply((T) listing.get().getEntry().getElement());
				}
			}
		}
		return Text.EMPTY;
	}
}
