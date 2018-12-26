package me.nickimpact.gts.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.GTSInfo;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.data.AuctionData;
import me.nickimpact.gts.configuration.ConfigKeys;
import me.nickimpact.gts.configuration.MsgConfigKeys;
import me.nickimpact.gts.discord.Message;
import me.nickimpact.gts.internal.TextParsingUtils;
import me.nickimpact.gts.logs.Log;
import me.nickimpact.gts.logs.LogAction;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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
	            boolean successful;

	            AuctionData ad = listing.getAucData();
	        	if(ad != null && ad.getHighBidder() != null) {
	        		successful = award(PlayerUtils.getUserFromUUID(ad.getHighBidder()).orElse(null), listing);
	        		// Even if we can't give the winning player their award, due to them being offline, at least
			        // give the auctioneer their winnings
	        		if(!ad.isOwnerReceived()) {
				        try {
					        listing.getEntry().getPrice().reward(listing.getOwnerUUID());
					        ad.setOwnerReceived(true);
					        if(!successful) {
					        	GTS.getInstance().getStorage().updateListing(listing);
					        }
					        Sponge.getServer().getPlayer(listing.getOwnerUUID()).ifPresent(player -> {
									Map<String, Object> variables = Maps.newHashMap();
									variables.put("listing", listing);
									player.sendMessages(TextParsingUtils.parse(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.AUCTION_SOLD), player, null, variables));
					        });
				        } catch (Exception e) {
					        e.printStackTrace();
				        }
			        }
	            } else {
		            successful = expire(listing);
	            }

	            if(successful) {
	        		ListingUtils.deleteEntry(listing);
	            }
            });
        }).interval(1, TimeUnit.SECONDS).submit(GTS.getInstance());
    }

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

		final String b = TextParsingUtils.parse("{{seller}}'s {{listing_specifics}} listing has now expired!", player, null, variables).toPlain();
		GTS.getInstance().getDiscordNotifier().ifPresent(notifier -> {
			Message message = notifier.forgeMessage(GTS.getInstance().getConfig().get(ConfigKeys.DISCORD_EXPIRE), b);
			notifier.sendMessage(message);
		});

	    Log expires = Log.builder()
			    .action(LogAction.Expiration)
			    .source(player.getUniqueId())
			    .hover(Log.forgeTemplate(player, listing, LogAction.Expiration))
			    .build();
	    GTS.getInstance().getStorage().addLog(expires);

	    return true;
    }

    private static boolean award(User user, Listing listing) {
    	if(user == null || !user.getPlayer().isPresent())
    		return false;

    	if(!listing.getEntry().giveEntry(user.getPlayer().get()))
    	    return false;

	    try {
		    if(!listing.getEntry().getPrice().canPay(user.getPlayer().get())) {
			    user.getPlayer().get().sendMessage(Text.of(GTSInfo.ERROR, "Your balance was too low to afford your bid..."));
			    return false;
		    }
		    listing.getEntry().getPrice().pay(user);
	    } catch (Exception e) {
		    e.printStackTrace();
	    }

	    Map<String, Object> variables = Maps.newHashMap();
		variables.put("listing", listing);
		user.getPlayer().get().sendMessages(TextParsingUtils.parse(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.AUCTION_WIN), user.getPlayer().get(), null, variables));

		List<Text> broadcast = TextParsingUtils.parse(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.AUCTION_WIN_BROADCAST), user.getPlayer().get(), null, variables);
		Sponge.getServer().getOnlinePlayers().stream()
				.filter(pl -> GTS.getInstance().getIgnorers().contains(pl.getUniqueId()))
				.forEach(pl -> pl.sendMessages(broadcast));

	    return true;
    }
}
