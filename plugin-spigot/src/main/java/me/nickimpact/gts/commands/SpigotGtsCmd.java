package me.nickimpact.gts.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.enums.CommandResults;
import me.nickimpact.gts.api.holders.EntryClassification;
import me.nickimpact.gts.api.searching.Searcher;
import me.nickimpact.gts.config.MsgConfigKeys;
import me.nickimpact.gts.spigot.MessageUtils;
import me.nickimpact.gts.ui.SpigotMainUI;
import me.nickimpact.gts.ui.SpigotSellUI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@CommandAlias("gts")
@Description("Controls the functionality of GTS")
@CommandPermission("gts.command.gts.base")
public class SpigotGtsCmd extends BaseCommand {

	@Default
	public void execute(Player player) {
		new SpigotMainUI(player, null, null).open();
	}

	@Subcommand("sell|add")
	@CommandPermission("gts.command.sell.base")
	public class SellSub extends BaseCommand {

		@Default
		@Syntax("(type) (additional arguments) - Allows you to sell something. No type = User GUI")
		public void execute(CommandIssuer player, @Optional EntryClassification classification, @Optional String... additionals) {
			if(classification == null) {
				//new SpigotSellUI(player).open();
				player.sendMessage(GTS.getInstance().getTokenService().process(MsgConfigKeys.SELL_CMD_INVALID, player.getIssuer(), null, null));
			} else {
				if(player.hasPermission("gts.command.sell." + classification.getPrimaryIdentifier().toLowerCase())) {
					boolean perm = false;
					List<String> addons = Arrays.stream(additionals).map(String::toLowerCase).collect(Collectors.toList());
					if(addons.contains("-p")) {
						perm = true;
					}

					classification.getCmdHandler().apply(player, addons, perm);
				}
			}
		}
	}

	@Subcommand("search")
	@CommandPermission("gts.command.search.base")
	public class Search extends BaseCommand {

		@Default
		@Description("Searches the GTS for a set of listings matching the specified conditions")
		public void execute(Player player, String key, @Split(" ") String criteria) {
			java.util.Optional<Searcher> searcher = GTS.getInstance().getAPIService().getSearcher(key);
			if(searcher.isPresent()) {
				new SpigotMainUI(player, searcher.get(), criteria).open();
			} else {
				player.sendMessage(GTS.getInstance().getTokenService().process(MsgConfigKeys.SEARCH_NO_OPTION, player, null, null));
			}
		}

	}

	@HelpCommand
	public void onHelp(CommandSender sender, CommandHelp help) {
		help.showHelp();
	}
}
