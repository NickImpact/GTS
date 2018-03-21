package com.nickimpact.gts.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.nickimpact.gts.GTS;
import com.nickimpact.gts.api.listings.Listing;
import com.nickimpact.gts.api.listings.data.AuctionData;
import com.nickimpact.gts.configuration.MsgConfigKeys;
import io.github.nucleuspowered.nucleus.api.exceptions.NucleusException;
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
            long start = System.nanoTime();

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
						        try {
							        Map<String, Object> variables = Maps.newHashMap();
							        variables.put("dummy", listing.getEntry().getElement());
							        variables.put("dummy2", listing);
							        variables.put("dummy3", listing.getEntry());
							        player.sendMessages(GTS.getInstance().getTextParsingUtils().parse(
									        GTS.getInstance().getMsgConfig().get(MsgConfigKeys.AUCTION_SOLD),
									        player,
									        null,
									        variables
							        ));
						        } catch (NucleusException e) {
							        e.printStackTrace();
						        }
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

            long end = System.nanoTime();
	        //GTS.getInstance().getConsole().ifPresent(console -> console.sendMessage(Text.of(
			//        GTSInfo.DEBUG, "Execution Time: ", ((end - start) / Math.pow(10, 6)) + " ms"
	        //)));

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
	    variables.put("dummy", listing);
	    variables.put("dummy2", listing.getEntry());
	    variables.put("dummy3", listing.getEntry().getElement());

	    Player player = owner.get();
	    if(!listing.getEntry().giveEntry(player))
	    	return false;

	    try {
		    player.sendMessages(GTS.getInstance().getTextParsingUtils().parse(
				    GTS.getInstance().getMsgConfig().get(MsgConfigKeys.REMOVAL_EXPIRES),
				    player,
				    null,
				    variables
		    ));
	    } catch (NucleusException e) {
		    e.printStackTrace();
	    }

	    return true;
    }

    private static boolean award(User user, Listing listing) {
    	if(user == null || !user.getPlayer().isPresent())
    		return false;

    	if(!listing.getEntry().giveEntry(user.getPlayer().get()))
    	    return false;

	    try {
		    listing.getEntry().getPrice().pay(user);
	    } catch (Exception e) {
		    e.printStackTrace();
	    }

	    Map<String, Object> variables = Maps.newHashMap();
	    variables.put("dummy", listing.getEntry().getElement());
	    variables.put("dummy2", listing);
	    variables.put("dummy3", listing.getEntry());
	    try {
		    user.getPlayer().get().sendMessages(
				    GTS.getInstance().getTextParsingUtils().parse(
						    GTS.getInstance().getMsgConfig().get(MsgConfigKeys.AUCTION_WIN),
						    user.getPlayer().get(),
						    null,
						    variables
				    )
		    );
	    } catch (NucleusException e) {
		    e.printStackTrace();
	    }

	    try {
	    	List<Text> broadcast = GTS.getInstance().getTextParsingUtils().parse(
				    GTS.getInstance().getMsgConfig().get(MsgConfigKeys.AUCTION_WIN_BROADCAST),
				    user.getPlayer().get(),
				    null,
				    null
		    );
		    Sponge.getServer().getOnlinePlayers().stream()
				    .filter(pl -> GTS.getInstance().getIgnorers().contains(pl.getUniqueId()))
				    .forEach(pl -> pl.sendMessages(broadcast));
	    } catch (NucleusException e) {
		    e.printStackTrace();
	    }
	    return true;
    }
}
