package com.nickimpact.gts.listeners;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.nickimpact.gts.GTS;
import com.nickimpact.gts.api.listings.Listing;
import com.nickimpact.gts.configuration.MsgConfigKeys;
import com.nickimpact.gts.utils.ListingUtils;
import io.github.nucleuspowered.nucleus.api.exceptions.NucleusException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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

					Map<String, Object> variables = Maps.newHashMap();
					variables.put("listing_specifics", listing);
					variables.put("listing_name", listing);
					variables.put("time_left", listing);
					variables.put("id", listing);

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
				});
	}
}
