package me.nickimpact.gts.listeners;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.config.MsgConfigKeys;
import me.nickimpact.gts.sponge.SpongeListing;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class JoinListener {

	@Listener
	public void onPlayerJoin(ClientConnectionEvent.Join e, @First Player player) {
		ImmutableList.copyOf((List<SpongeListing>)GTS.getInstance().getAPIService().getListingManager().getListings()).stream()
				.filter(listing -> listing.getOwnerUUID().equals(player.getUniqueId()) && listing.hasExpired())
				.forEach(listing -> {
					Map<String, Object> variables = Maps.newHashMap();
					variables.put("listing_specifics", listing);
					variables.put("listing_name", listing);
					variables.put("time_left", listing);
					variables.put("id", listing);
					if(listing.getEntry().giveEntry(player)) {
						GTS.getInstance().getAPIService().getListingManager().deleteListing(listing);
						player.sendMessages(GTS.getInstance().getTextParsingUtils().parse(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.REMOVAL_EXPIRES), player, null, variables));
					}
				});
		GTS.getInstance().getAPIService().getStorage().getAllSoldListingsForPlayer(player.getUniqueId()).thenAccept(sold -> {
			sold.forEach(l -> {
				Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
				tokens.put("listing_name", src -> Optional.of(Text.of(l.getNameOfEntry())));
				tokens.put("gts_price", src -> Optional.of(GTS.getInstance().getEconomy().getDefaultCurrency().format(new BigDecimal(l.getMoneyReceived()))));
				player.sendMessage(GTS.getInstance().getTextParsingUtils().fetchAndParseMsg(player, MsgConfigKeys.SOLD_LISTING_INFORM, tokens, null));

				GTS.getInstance().getAPIService().getStorage().deleteSoldListing(l.getId(), player.getUniqueId());
			});
		});
	}
}
