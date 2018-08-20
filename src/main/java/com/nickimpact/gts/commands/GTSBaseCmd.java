package com.nickimpact.gts.commands;

import com.nickimpact.gts.commands.basic.AucCmd;
import com.nickimpact.gts.commands.basic.HelpCmd;
import com.nickimpact.gts.commands.basic.IgnoreCmd;
import com.nickimpact.gts.commands.basic.SellCmd;
import com.nickimpact.gts.ui.MainUI;
import com.nickimpact.impactor.api.commands.SpongeCommand;
import com.nickimpact.impactor.api.commands.annotations.Aliases;
import com.nickimpact.impactor.api.plugins.SpongePlugin;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@Aliases({"gts"})
public class GTSBaseCmd extends SpongeCommand {

	public GTSBaseCmd(SpongePlugin plugin) {
		super(plugin);
	}

	@Override
	public CommandElement[] getArgs() {
		return null;
	}

	@Override
	public Text getDescription() {
		return Text.of("Represents the base command to everything GTS");
	}

	@Override
	public Text getUsage() {
		return Text.of("/gts");
	}

	@Override
	public SpongeCommand[] getSubCommands() {
		return new SpongeCommand[] {
				new SellCmd(this.plugin),
				new IgnoreCmd(this.plugin),
				new HelpCmd(this.plugin),
				new AucCmd(this.plugin),
				new AdminBaseCmd(this.plugin)
		};
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if(src instanceof Player) {
			new MainUI(((Player)src)).open((Player) src, 1);
		} else if(src instanceof ConsoleSource) {
			// Send help to console
		} else {
			throw new CommandException(Text.of("Command blocks can't access gts data..."));
		}

		return CommandResult.success();
	}
}
