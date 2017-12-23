package com.nickimpact.gts.utils;

import com.google.common.collect.ImmutableList;
import com.nickimpact.gts.GTS;
import com.nickimpact.gts.GTSInfo;
import com.nickimpact.gts.api.listings.Listing;
import com.nickimpact.gts.api.listings.data.AuctionData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import java.sql.Date;
import java.time.Instant;
import java.util.List;
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

	        	if(listing.getAucData() != null) {
		            AuctionData ad = listing.getAucData();
		            if(ad.getHighBidder() == null)
			            successful = expire(listing);
		            else {
		            	successful = award(PlayerUtils.getUserFromUUID(ad.getHighBidder()).orElse(null), listing);
		            }
	            } else {
		            successful = expire(listing);
	            }

	            if(successful) {
	        		LotUtils.deleteEntry(listing);
	        		GTS.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.DEBUG_PREFIX, "Removing listing from market with ID: " + listing.getID())));
	            }
            });

            long end = System.nanoTime();
	        //GTS.getInstance().getConsole().ifPresent(console -> console.sendMessage(Text.of(
			//        GTSInfo.DEBUG_PREFIX, "Execution Time: ", ((end - start) / Math.pow(10, 6)) + " ms"
	        //)));

        }).interval(1, TimeUnit.SECONDS).submit(GTS.getInstance());
    }

    private static boolean expire(Listing listing) {
		Optional<Player> owner = Sponge.getServer().getPlayer(listing.getOwnerUUID());
		if(!owner.isPresent())
			return false;

	    Player player = owner.get();
	    player.sendMessages(Text.of(GTSInfo.PREFIX, "Returning your listing!"));
	    boolean task = listing.getEntry().giveEntry(player);

	    // Send a message about the expiration

	    return true;
    }

    private static boolean award(User user, Listing listing) {
    	if(user == null || !user.getPlayer().isPresent())
    		return false;

    	boolean task = listing.getEntry().giveEntry(user.getPlayer().get());
    	// Send a message about the auction

	    return true;
    }
}
