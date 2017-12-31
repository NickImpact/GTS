package com.nickimpact.gts.commands.basic;

import com.google.common.collect.Lists;
import com.nickimpact.gts.api.commands.annotations.CommandAliases;
import com.nickimpact.gts.api.commands.SpongeCommand;
import com.nickimpact.gts.api.commands.SpongeSubCommand;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import java.util.List;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@CommandAliases({"sell", "add"})
public class SellCmd extends SpongeSubCommand {

	public static List<SpongeSubCommand> children = Lists.newArrayList();

	@Override
	public CommandElement[] getArgs() {
		return null;
	}

	@Override
	public Text getDescription() {
		return Text.of("Grants the ability to add a kind of element to the GTS listings");
	}

	@Override
	public SpongeCommand[] getSubCommands() {
		return children.toArray(new SpongeSubCommand[children.size()]);
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		return CommandResult.empty();
	}
}
