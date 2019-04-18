package me.nickimpact.gts.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.configuration.ConfigKeys;
import me.nickimpact.gts.configuration.MsgConfigKeys;
import me.nickimpact.gts.discord.Message;
import me.nickimpact.gts.internal.TextParsingUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class ListingTasks {

    public static void updateTask() {
        Sponge.getScheduler().createTaskBuilder().execute(() -> {
            final List<Listing> listings = ImmutableList.copyOf(GTS.getInstance().getListingsCache());
	        listings.stream().filter(listing -> listing.getExpiration().before(Date.from(Instant.now()))).forEach(listing -> {
	            if(expire(listing)) {
	        		ListingUtils.deleteEntry(listing);
	            }
            });
        }).interval(1, TimeUnit.SECONDS).submit(GTS.getInstance());
    }

    @SuppressWarnings("unchecked")
    private static boolean expire(Listing listing) {
		Optional<Player> owner = Sponge.getServer().getPlayer(listing.getOwnerUUID());
		if(!owner.isPresent()) {
			// Offline player provider
			if(listing.getEntry().supportsOffline()) {
				User user = GTS.getInstance().getUserStorageService().get(listing.getOwnerUUID()).orElse(null);
				return user != null && listing.getEntry().giveEntry(user);
			}
			return false;
		}

		Map<String, Object> variables = Maps.newHashMap();
		variables.put("listing", listing);
		Player player = owner.get();
	    if(!listing.getEntry().giveEntry(player))
	    	return false;

	    player.sendMessages(TextParsingUtils.parse(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.REMOVAL_EXPIRES), player, null, variables));

	    GTS.getInstance().getDiscordNotifier().ifPresent(notifier -> {
		    Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
		    tokens.put("gts_publisher", src -> Optional.of(Text.of(listing.getOwnerName())));
		    tokens.put("gts_publisher_id", src -> Optional.of(Text.of(listing.getOwnerUUID())));
		    tokens.put("gts_published_item", src -> Optional.of(Text.of(listing.getEntry().getName())));

		    List details = Lists.newArrayList("");
		    details.addAll(listing.getEntry().getDetails());
		    tokens.put("gts_published_item_details", src -> Optional.of(Text.of(StringUtils.stringListToString(details))));
		    tokens.put("gts_publishing_price", src -> Optional.of(Text.of(listing.getEntry().getPrice().getText().toPlain())));

		    Message message = notifier.forgeMessage(GTS.getInstance().getConfig().get(ConfigKeys.DISCORD_EXPIRE), StringUtils.textListToString(TextParsingUtils.fetchAndParseMsgs(
				    null, MsgConfigKeys.DISCORD_EXPIRATION_TEMPLATE, tokens, null)
		    ));
		    notifier.sendMessage(message);
	    });

	    return true;
    }
}
