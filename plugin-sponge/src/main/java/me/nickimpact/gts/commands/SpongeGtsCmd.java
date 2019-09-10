package me.nickimpact.gts.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.holders.EntryClassification;
import me.nickimpact.gts.api.listings.ListingManager;
import me.nickimpact.gts.config.MsgConfigKeys;
import me.nickimpact.gts.ui.SellUI;
import me.nickimpact.gts.ui.SpongeMainUI;
import org.spongepowered.api.entity.living.player.Player;

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
	public class SellSub extends BaseCommand {

		@Default
		@Syntax("(type) (additional arguments) - Allows you to sell something. No type = User GUI")
		public void execute(Player player, @Optional EntryClassification classification, @Optional String... additionals) {
			if(classification == null) {
				new SellUI(player).open();
			} else {
				if(player.hasPermission("gts.command.sell." + classification.getPrimaryIdentifier().toLowerCase())) {
					if(additionals.length == 0) {
						classification.getUi().createFor(player).getDisplay().open(player);
					} else {
						Config config = GTS.getInstance().getConfiguration();
						Config msgConfig = GTS.getInstance().getMsgConfig();
						TextParsingUtils parser = GTS.getInstance().getTextParsingUtils();

						if(config.get(ConfigKeys.MIN_PRICING_ENABLED)) {
							// Should be MIN_MONEY_PRICE. Not sure where that is or if its handled elsewhere
							if(Integer.parseInt(additionals[0]) >= config.get(ConfigKeys.MAX_MONEY_PRICE)) {
								player.sendMessage(parser.fetchAndParseMsg(player, MsgConfigKeys.MIN_PRICE_ERROR, null, null));
							} else {
								classification.getCmdHandler().apply(player, additionals);
							}
						} else {
							classification.getCmdHandler().apply(player, additionals);
						}
					}
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
