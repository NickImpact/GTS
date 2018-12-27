package me.nickimpact.gts.commands.basic;

import com.nickimpact.impactor.api.commands.SpongeCommand;
import com.nickimpact.impactor.api.commands.SpongeSubCommand;
import com.nickimpact.impactor.api.commands.annotations.Aliases;
import com.nickimpact.impactor.api.plugins.SpongePlugin;
import me.nickimpact.gts.ui.SellUI;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@Aliases({"sell", "add"})
public class SellCmd extends SpongeSubCommand {

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
		return Text.of("/gts sell");
	}

	@Override
	public SpongeCommand[] getSubCommands() {
		return new SpongeCommand[0];
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if(src instanceof Player) {
			new SellUI((Player) src).open((Player) src, 1);
			return CommandResult.success();
		}

		throw new CommandException(Text.of("Only players can use this command..."));
	}
}
