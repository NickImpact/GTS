package com.nickimpact.gts.commands.basic;

import com.nickimpact.gts.api.commands.annotations.CommandAliases;
import com.nickimpact.gts.api.commands.SpongeCommand;
import com.nickimpact.gts.api.commands.SpongeSubCommand;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@CommandAliases({"help"})
public class HelpCmd extends SpongeSubCommand {

	@Override
	public CommandElement[] getArgs() {
		return new CommandElement[0];
	}

	@Override
	public Text getDescription() {
		return Text.of();
	}

	@Override
	public SpongeCommand[] getSubCommands() {
		return new SpongeCommand[0];
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

		src.sendMessage(Text.of(TextColors.GRAY, "Friendly Command Helper: "));
		src.sendMessage(Text.of(TextColors.YELLOW, "{}", TextColors.GRAY, "= Required   ", TextColors.YELLOW, "<>", TextColors.GRAY,"= Optional   ", TextColors.YELLOW,"... ", TextColors.GRAY,"= Accepts more than 1"));


		return CommandResult.success();
	}
}
