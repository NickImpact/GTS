package me.nickimpact.gts.tasks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.nickimpact.impactor.api.configuration.Config;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.config.ConfigKeys;
import me.nickimpact.gts.config.MsgConfigKeys;
import me.nickimpact.gts.discord.DiscordNotifier;
import me.nickimpact.gts.discord.Message;
import me.nickimpact.gts.spigot.SpigotListing;
import me.nickimpact.gts.spigot.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.time.LocalDateTime;
import java.util.List;

public class SpigotListingTasks implements ListingTasks<SpigotListing> {

	@Override
	public void createExpirationTask() {
		Bukkit.getScheduler().runTaskTimer(GTS.getInstance(), () -> {
			final List<SpigotListing> listings = ImmutableList.copyOf(GTS.getInstance().getAPIService().getListingManager().getListings());
			listings.stream().filter(listing -> listing.getExpiration().isBefore(LocalDateTime.now())).forEach(listing -> {
				if(expire(listing)) {
					GTS.getInstance().getAPIService().getListingManager().deleteListing(listing);
				}
			});
		}, 0, 20);
	}

	@Override
	public boolean expire(SpigotListing listing) {
		OfflinePlayer owner = Bukkit.getOfflinePlayer(listing.getOwnerUUID());
		if(!owner.isOnline()) {
			// Offline player provider
			if(listing.getEntry().supportsOffline()) {
				return listing.getEntry().giveEntry(owner);
			}
			return false;
		}

		if(!listing.getEntry().giveEntry(owner)) {
			return false;
		}

		Config config = GTS.getInstance().getMsgConfig();
		owner.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("%s Your %s listing has expired, and has been returned to you!", config.get(MsgConfigKeys.PREFIX), listing.getName())));

		List<String> details = Lists.newArrayList("");
		details.addAll(listing.getEntry().getDetails());
		String discord = MessageUtils.asSingleWithNewlines(Lists.newArrayList(
				"Publisher: " + Bukkit.getOfflinePlayer(listing.getOwnerUUID()).getName(),
				"Publisher Identifier: " + listing.getOwnerUUID().toString(),
				"",
				"Published Item: " + listing.getName(),
				"Item Details: " + MessageUtils.asSingleWithNewlines(details)
		));

		DiscordNotifier notifier = new DiscordNotifier(GTS.getInstance());
		Message message = notifier.forgeMessage(GTS.getInstance().getConfiguration().get(ConfigKeys.DISCORD_EXPIRE), discord);
		notifier.sendMessage(message);

		return true;
	}

}
