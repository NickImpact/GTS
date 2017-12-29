package com.nickimpact.gts.listeners;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.nickimpact.gts.GTS;
import com.nickimpact.gts.api.listings.Listing;
import com.nickimpact.gts.utils.ListingUtils;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.util.Map;
import java.util.Optional;

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
				.filter(Listing::checkHasExpired)
				.forEach(listing -> {
					listing.getEntry().giveEntry(player);
					ListingUtils.deleteEntry(listing);

					Map<String, Optional<Object>> replacements = Maps.newHashMap();
					replacements.put("player", Optional.of(player.getName()));
					replacements.put("pokemon", Optional.of(listing.getName()));

					//for(Text text : MessageConfig.getMessages("Generic.Remove.Expired", replacements))
					//	player.sendMessage(text);
				});
	}
}
