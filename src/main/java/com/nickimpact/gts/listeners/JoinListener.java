package com.nickimpact.gts.listeners;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.nickimpact.gts.GTS;
import com.nickimpact.gts.api.listings.Listing;
import com.nickimpact.gts.configuration.MsgConfigKeys;
import com.nickimpact.gts.logs.Log;
import com.nickimpact.gts.logs.LogAction;
import com.nickimpact.gts.utils.ListingUtils;
import io.github.nucleuspowered.nucleus.api.exceptions.NucleusException;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.util.Map;

/**
 * On a client connection, we need to check and see if any entries in the GTS have expired for the player
 * logging in. If so, we will collect these entries, return them to the owner, and purge them from the system.
 *
 * @author NickImpact
 */
public class JoinListener {

	@Listener
	public void onJoin(ClientConnectionEvent.Join e) {
		Player player = e.getTargetEntity();

		ImmutableList.copyOf(GTS.getInstance().getListingsCache()).stream()
				.filter(listing -> listing.getOwnerUUID().equals(player.getUniqueId()))
				.filter(Listing::hasExpired)
				.forEach(listing -> {
					Map<String, Object> variables = Maps.newHashMap();
					variables.put("listing_specifics", listing);
					variables.put("listing_name", listing);
					variables.put("time_left", listing);
					variables.put("id", listing);
					if(listing.getEntry().giveEntry(player)) {
						ListingUtils.deleteEntry(listing);
						Log expires = Log.builder()
								.action(LogAction.Expiration)
								.source(player.getUniqueId())
								.hover(Log.forgeTemplate(player, listing, LogAction.Expiration))
								.build();
						GTS.getInstance().getStorage().addLog(expires);
						try {
							player.sendMessages(GTS.getInstance().getTextParsingUtils().parse(
									GTS.getInstance().getMsgConfig().get(MsgConfigKeys.REMOVAL_EXPIRES),
									player,
									null,
									variables
							));
						} catch (NucleusException e1) {
							e1.printStackTrace();
						}
					} else {
						try {
							player.sendMessages(GTS.getInstance().getTextParsingUtils().parse(
									"{{gts_error}} &7Your &a{{listing_name}} &7listing has expired, but was unable to be returned. It is now in a queue for retrieval once you meet the proper conditions...",
									player,
									null,
									variables
							));
						} catch (NucleusException e1) {
							e1.printStackTrace();
						}
					}
				});
	}
}
