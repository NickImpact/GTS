package com.nickimpact.GTS.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import com.nickimpact.GTS.guis.AdminUI;

/**
 * Created by Nick on 12/15/2016.
 */
public class EditCommand implements CommandExecutor {
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if(src instanceof Player){
			Player player = (Player)src;
			player.openInventory(new AdminUI(1).getInventory());
			return CommandResult.success();
		}
		throw new CommandException(Text.of("Only players can edit the GTS listings at this time.."));
	}
}
