package com.nickimpact.GTS.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import com.nickimpact.GTS.guis.MainUI;

/**
 * Created by Nick on 12/15/2016.
 */
public class GTSCommand implements CommandExecutor {
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if(!(src instanceof Player)){
			throw new CommandException(Text.of("Only players can open the GTS listings!"));
		}
		//Main.showGUI((Player)src, 1, false, GTS.getInstance().getLots());

		((Player) src).openInventory(new MainUI((Player)src, 1).getInventory());

		return CommandResult.success();
	}
}
