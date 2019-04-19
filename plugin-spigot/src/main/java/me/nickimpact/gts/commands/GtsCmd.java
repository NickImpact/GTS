package me.nickimpact.gts.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.nickimpact.gts.api.holders.EntryClassification;
import me.nickimpact.gts.ui.MainUI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

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
		public void execute(Player player, @Optional EntryClassification classification) {
			if(classification == null) {
				player.sendMessage(ChatColor.YELLOW + "Sell command received for UI usage");
			} else {
				if(player.hasPermission("gts.command.sell." + classification.getPrimaryIdentifier().toLowerCase())) {
					player.sendMessage(ChatColor.YELLOW + "Sell command received with classification type: " + classification.getPrimaryIdentifier());
				}
			}
		}
	}
}
