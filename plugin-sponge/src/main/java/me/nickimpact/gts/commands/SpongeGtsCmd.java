package me.nickimpact.gts.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.google.common.collect.Lists;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.holders.EntryClassification;
import me.nickimpact.gts.api.listings.ListingManager;
import me.nickimpact.gts.config.MsgConfigKeys;
import me.nickimpact.gts.ui.SellUI;
import me.nickimpact.gts.ui.SpongeMainUI;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@CommandAlias("gts")
@Description("Controls the functionality of GTS")
@CommandPermission("gts.command.gts.base")
public class SpongeGtsCmd extends BaseCommand {

	@Default
	public void execute(Player player) {
		new SpongeMainUI(player).open();
	}

	@Subcommand("sell|add")
	@CommandPermission("gts.command.sell.base")
	@Syntax("(type) (additional arguments) - Allows you to sell something. No type = User GUI")
	public void sell(Player player, @Optional EntryClassification classification, @Optional String... additionals) {
		if(classification == null) {
			new SellUI(player).open();
		} else {
			if(player.hasPermission("gts.command.sell." + classification.getPrimaryIdentifier().toLowerCase())) {
				if(additionals.length == 0) {
					classification.getUi().createFor(player).getDisplay().open(player);
				} else {
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

	@Subcommand("ignore")
	@CommandPermission("gts.command.ignore.base")
	public class IgnoreSub extends BaseCommand {

		@Default
		@Description("Silences all broadcasts from GTS")
		public void execute(Player player) {
			ListingManager manager = GTS.getInstance().getAPIService().getListingManager();
			if(manager.getIgnorers().contains(player.getUniqueId())) {
				GTS.getInstance().getAPIService().getStorage().removeIgnorer(player.getUniqueId());
				player.sendMessage(GTS.getInstance().getTextParsingUtils().fetchAndParseMsg(player, MsgConfigKeys.IGNORE_OFF, null, null));
			} else {
				GTS.getInstance().getAPIService().getStorage().addIgnorer(player.getUniqueId());
				player.sendMessage(GTS.getInstance().getTextParsingUtils().fetchAndParseMsg(player, MsgConfigKeys.IGNORE_ON, null, null));
			}
		}
	}

	@HelpCommand
	public void onHelp(Player player, CommandHelp help) {

	}
}
