package com.nickimpact.GTS.Commands;

import com.google.common.collect.Lists;
import com.nickimpact.GTS.GTS;
import com.nickimpact.GTS.Inventories.Main;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Nick on 12/15/2016.
 */
public class SearchCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if(src instanceof Player){
            if(args.hasAny("pokemon")) {
                List<String> pokemon = Lists.newArrayList(args.getOne("pokemon").get().toString().split(" "));
                Main.showGUI((Player) src, 1, true, pokemon);
                return CommandResult.success();
            } else {
                throw new CommandException(Text.of("No arguments passed to the search command.."));
            }
        } else {
            throw new CommandException(Text.of("Only players may use this command.."));
        }
    }
}
