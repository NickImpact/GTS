package me.nickimpact.gts.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import me.nickimpact.gts.ui.SpongeListingMenu;
import me.nickimpact.gts.ui.SpongeMainMenu;

@CommandAlias("test")
public class TestCommand extends BaseCommand {

	@Default
	public void execute(CommandIssuer issuer, String test) {

	}

	@Subcommand("ping")
	public void ping(CommandIssuer issuer) {
		GTSPlugin.getInstance().getMessagingService().sendPing();
	}

	@Subcommand("menu")
	public void menu(CommandIssuer issuer) {
		new SpongeMainMenu(issuer.getIssuer()).open();
	}

}
