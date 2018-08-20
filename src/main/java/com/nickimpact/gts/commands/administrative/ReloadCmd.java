package com.nickimpact.gts.commands.administrative;

import com.nickimpact.impactor.api.commands.SpongeCommand;
import com.nickimpact.impactor.api.commands.SpongeSubCommand;
import com.nickimpact.impactor.api.commands.annotations.Aliases;
import com.nickimpact.impactor.api.commands.annotations.Permission;
import com.nickimpact.impactor.api.plugins.SpongePlugin;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@Aliases({"reload"})
@Permission(admin = true)
public class ReloadCmd extends SpongeSubCommand {

	public ReloadCmd(SpongePlugin plugin) {
		super(plugin);
	}

	@Override
	public CommandElement[] getArgs() {
		return new CommandElement[0];
	}

	@Override
	public Text getDescription() {
		return Text.of();
	}

	@Override
	public Text getUsage() {
		return Text.of("/gts admin reload");
	}

	@Override
	public SpongeCommand[] getSubCommands() {
		return new SpongeCommand[0];
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		return CommandResult.success();
	}
}
