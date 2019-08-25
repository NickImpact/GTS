package me.nickimpact.gts.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import me.nickimpact.gts.api.holders.EntryClassification;
import me.nickimpact.gts.spigot.MessageUtils;
import me.nickimpact.gts.ui.SpigotMainUI;
import me.nickimpact.gts.ui.SpigotSellUI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("gts")
@Description("Controls the functionality of GTS")
@CommandPermission("gts.command.gts.base")
public class SpigotGtsCmd extends BaseCommand {

	@Default
	public void execute(Player player) {
		new SpigotMainUI(player).open();
	}

	@Subcommand("sell|add")
	@CommandPermission("gts.command.sell.base")
	public class SellSub extends BaseCommand {

		@Default
		@Syntax("(type) (additional arguments) - Allows you to sell something. No type = User GUI")
		public void execute(Player player, @Optional EntryClassification classification, @Optional String... additionals) {
			if(classification == null) {
				//new SpigotSellUI(player).open();
				player.sendMessage(MessageUtils.parse("You must specify the type of thing you wish to sell!", true));
			} else {
				if(player.hasPermission("gts.command.sell." + classification.getPrimaryIdentifier().toLowerCase())) {
					classification.getCmdHandler().apply(player, additionals);
				}
			}
		}
	}

	@HelpCommand
	public void onHelp(CommandSender sender, CommandHelp help) {
		help.showHelp();
	}
}
