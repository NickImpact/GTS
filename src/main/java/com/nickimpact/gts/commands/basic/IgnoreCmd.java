package com.nickimpact.gts.commands.basic;

import com.nickimpact.gts.GTS;
import com.nickimpact.gts.GTSInfo;
import com.nickimpact.gts.api.commands.annotations.CommandAliases;
import com.nickimpact.gts.api.commands.SpongeCommand;
import com.nickimpact.gts.api.commands.SpongeSubCommand;
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
@CommandAliases({"ignore"})
public class IgnoreCmd extends SpongeSubCommand {

	@Override
	public CommandElement[] getArgs() {
		return new CommandElement[0];
	}

	@Override
	public Text getDescription() {
		return Text.of("Toggle ability to ignore GTS broadcasts");
	}

	@Override
	public Text getUsage() {
		return Text.of("/gts ignore");
	}

	@Override
	public SpongeCommand[] getSubCommands() {
		return new SpongeCommand[0];
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if(src instanceof Player) {
			Player player = (Player)src;
			if(GTS.getInstance().getIgnorers().contains(player.getUniqueId())) {
				GTS.getInstance().getIgnorers().remove(player.getUniqueId());
				GTS.getInstance().getStorage().removeIgnorer(player.getUniqueId());
				player.sendMessages(
						Text.of(GTSInfo.PREFIX, "You are no longer ignoring GTS broadcasts!")
				);
			} else {
				GTS.getInstance().getIgnorers().add(player.getUniqueId());
				GTS.getInstance().getStorage().addIgnorer(player.getUniqueId());
				player.sendMessages(
						Text.of(GTSInfo.PREFIX, "Now ignoring GTS broadcasts!")
				);
			}
		}

		return CommandResult.success();
	}
}
