package com.nickimpact.GTS.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.GTS.GTS;
import com.nickimpact.GTS.guis.MainUI;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.text.Text;

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

        ((Player) src).openInventory(new MainUI((Player)src, 1).getInventory(),
                                     Cause.of(NamedCause.source(GTS.getInstance())));

        return CommandResult.success();
    }
}
