package me.nickimpact.gts.commands;

import me.nickimpact.gts.commands.administrative.ClearCmd;
import me.nickimpact.gts.commands.administrative.EditCmd;
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
@Aliases({"admin"})
@Permission
public class AdminBaseCmd extends SpongeSubCommand {

	public AdminBaseCmd(SpongePlugin plugin) {
		super(plugin);
	}

	@Override
	public CommandElement[] getArgs() {
		return new CommandElement[0];
	}

	@Override
	public Text getDescription() {
		return Text.of("The administrative bridge for GTS");
	}

	@Override
	public Text getUsage() {
		return Text.of("/gts admin <clear/edit>");
	}

	@Override
	public SpongeCommand[] getSubCommands() {
		return new SpongeCommand[]{
				new ClearCmd(this.plugin),
				new EditCmd(this.plugin)
		};
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		throw new CommandException(Text.of("/gts admin requires a specific action"));
	}
}
