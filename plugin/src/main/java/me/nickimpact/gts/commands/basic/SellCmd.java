package me.nickimpact.gts.commands.basic;

import com.google.common.collect.Lists;
import com.nickimpact.impactor.api.commands.SpongeCommand;
import com.nickimpact.impactor.api.commands.SpongeSubCommand;
import com.nickimpact.impactor.api.commands.annotations.Aliases;
import com.nickimpact.impactor.api.plugins.SpongePlugin;
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
@Aliases({"sell", "add"})
public class SellCmd extends SpongeSubCommand {

	public static List<SpongeSubCommand> children = Lists.newArrayList();

	public SellCmd(SpongePlugin plugin) {
		super(plugin);
	}

	@Override
	public CommandElement[] getArgs() {
		return null;
	}

	@Override
	public Text getDescription() {
		return Text.of("Adds a listing to the market");
	}

	@Override
	public Text getUsage() {
		return Text.of("/gts sell <type>");
	}

	@SuppressWarnings("unchecked")
	@Override
	public SpongeCommand[] getSubCommands() {
		return new SpongeCommand[0];
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		return CommandResult.empty();
	}
}
