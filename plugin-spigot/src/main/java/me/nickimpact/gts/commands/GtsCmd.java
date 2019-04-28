package me.nickimpact.gts.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.holders.EntryClassification;
import me.nickimpact.gts.listings.SpigotItemEntry;
import me.nickimpact.gts.listings.SpigotItemUI;
import me.nickimpact.gts.spigot.SpigotListing;
import me.nickimpact.gts.ui.MainUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

@CommandAlias("gts")
@Description("Controls the functionality of GTS")
@CommandPermission("gts.command.gts.base")
public class GtsCmd extends BaseCommand {

	@Default
	public void execute(Player player) {
		player.sendMessage(ChatColor.YELLOW + "Base command triggered");
		new MainUI(player).open();
	}

	@Subcommand("sell|add")
	@CommandPermission("gts.command.sell.base")
	public class SellSub extends BaseCommand {

		@Default
		public void execute(Player player, @Optional EntryClassification classification, @Optional String... additionals) {
			if(classification == null) {
				player.sendMessage(ChatColor.YELLOW + "Sell command received for UI usage");
				new SpigotItemUI().createFor(player).getDisplay().open(player);
			} else {
				if(player.hasPermission("gts.command.sell." + classification.getPrimaryIdentifier().toLowerCase())) {
					player.sendMessage(ChatColor.YELLOW + "Sell command received with classification type: " + classification.getPrimaryIdentifier());
					if(classification.getClassification().equals(SpigotItemEntry.class)) {
						GTS.getInstance().getPluginLogger().debug(Arrays.toString(additionals));
						SpigotListing listing = SpigotListing.builder()
								.id(UUID.randomUUID())
								.owner(player.getUniqueId())
								.entry(new SpigotItemEntry(new ItemStack(Material.matchMaterial("pixelmon_poke_ball"))))
								.price(500.0)
								.expiration(Date.from(Instant.now().plusSeconds(86400)))
								.build();
						GTS.getInstance().getAPIService().getListingManager().addToMarket(player.getUniqueId(), listing);
					}
				}
			}
		}
	}
}
