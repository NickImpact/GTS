package me.nickimpact.gts.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;

@CommandAlias("test")
public class TestCommand extends BaseCommand {

	@Default
	public void execute(CommandIssuer issuer, String test) {

	}

}
