package me.nickimpact.gts.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.holders.EntryClassification;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.ListingManager;
import me.nickimpact.gts.api.searching.Searcher;
import me.nickimpact.gts.config.MsgConfigKeys;
import me.nickimpact.gts.ui.SellUI;
import me.nickimpact.gts.ui.SpongeMainUI;
import org.spongepowered.api.command.CommandSource;
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
		new SpongeMainUI(player, null, null).open();
	}

	@Subcommand("sell|add")
	@CommandPermission("gts.command.sell.base")
	@Description("Allows you to sell something. No type specified = User GUI")
	@Syntax("(type) (additional arguments)")
	public void sell(CommandIssuer issuer, @Optional EntryClassification classification, @Optional String... additionals) {
		if(issuer.isPlayer()) {
			Player player = issuer.getIssuer();

			if (classification == null) {
				if(GTS.getInstance().getAPIService().getEntryRegistry().getClassifications().size() == 1) {
					if(additionals.length == 0) {
						GTS.getInstance().getAPIService().getEntryRegistry().getClassifications().get(0).getUi().createFor(player).getDisplay().open(player);
						return;
					} else {
						boolean perm = false;
						List<String> addons = Arrays.stream(additionals).map(String::toLowerCase).collect(Collectors.toList());
						if (addons.contains("-p")) {
							perm = true;
						}

						GTS.getInstance().getAPIService().getEntryRegistry().getClassifications().get(0).getCmdHandler().apply(issuer, addons, perm);
					}
				}
				new SellUI(player).open();
			} else {
				if (player.hasPermission("gts.command.sell." + classification.getPrimaryIdentifier().toLowerCase())) {
					if (additionals.length == 0) {
						classification.getUi().createFor(player).getDisplay().open(player);
					} else {
						boolean perm = false;
						List<String> addons = Arrays.stream(additionals).map(String::toLowerCase).collect(Collectors.toList());
						if (addons.contains("-p")) {
							perm = true;
						}

						classification.getCmdHandler().apply(issuer, addons, perm);
					}
				}
			}
		}
	}

	@Subcommand("ignore")
	@Description("Allows you to ignore GTS broadcasts")
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

	@Subcommand("search")
	@Description("Allows you to search the GTS for specific options")
	@CommandPermission("gts.command.search.base")
	public class Search extends BaseCommand {

		@Default
		@Description("Searches the GTS for a set of listings matching the specified conditions")
		public void execute(Player player, String key, @Split(" ") String criteria) {
			java.util.Optional<Searcher> searcher = GTS.getInstance().getAPIService().getSearcher(key);
			if(searcher.isPresent()) {
				new SpongeMainUI(player, searcher.get(), criteria).open();
			} else {
				player.sendMessage(GTS.getInstance().getTextParsingUtils().fetchAndParseMsg(player, MsgConfigKeys.SEARCH_NO_OPTION, null, null));
			}
		}

	}

	@Subcommand("admin")
	@Description("Admin functionality to GTS")
	@CommandPermission("gts.command.admin.base")
	public class Admin extends BaseCommand {

		@Default
		public void base(CommandSource issuer) {
			issuer.sendMessage(GTS.getInstance().getTextParsingUtils().parse("{{gts_error}} Functionality coming soon...", issuer, null, null));
		}

		@Subcommand("edit")
		@CommandPermission("gts.command.admin.edit")
		public void edit(Player player) {
			player.sendMessage(GTS.getInstance().getTextParsingUtils().parse("{{gts_error}} Functionality coming soon...", player, null, null));
		}

		@Subcommand("fix")
		@CommandPermission("gts.command.admin.fix")
		public void fix(CommandSource issuer) {
			((List<Listing>) GTS.getInstance().getAPIService().getListingManager().getListings()).removeIf(listing -> listing.getEntry().getElement() == null);
			issuer.sendMessage(GTS.getInstance().getTextParsingUtils().parse("{{gts_prefix}} Removed any broken listings!", issuer, null, null));
		}

	}

	@HelpCommand
	public void onHelp(Player player, CommandHelp help) {
		help.showHelp();
	}
}
