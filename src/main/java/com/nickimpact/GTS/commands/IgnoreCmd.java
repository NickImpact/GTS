package com.nickimpact.GTS.commands;

import com.nickimpact.GTS.GTS;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

/**
 * Created by nickd on 5/10/2017.
 */
public class IgnoreCmd implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Optional<Boolean> ignore = args.getOne("ignore");

        if(src instanceof Player) {
            if(ignore.isPresent()){
                if (ignore.get()) {
                    if (GTS.getInstance().getIgnoreList().contains(src)) {
                        throw new CommandException(Text.of("You are already ignoring GTS broadcasts"));
                    }

                    GTS.getInstance().getIgnoreList().add((Player)src);
                    src.sendMessage(Text.of(
                            TextColors.GREEN, "GTS ", TextColors.GRAY, "\u00bb Ignore Status: ",
                            TextColors.GREEN, "True"
                    ));
                } else {
                    if(!GTS.getInstance().getIgnoreList().contains(src)){
                        throw new CommandException(Text.of("You aren't viewing GTS broadcasts..."));
                    }

                    GTS.getInstance().getIgnoreList().remove(src);
                    src.sendMessage(Text.of(
                            TextColors.GREEN, "GTS ", TextColors.GRAY, "\u00bb Ignore Status: ",
                            TextColors.RED, "False"
                    ));
                }
            } else {
                if(GTS.getInstance().getIgnoreList().contains(src)){
                    GTS.getInstance().getIgnoreList().remove(src);
                    src.sendMessage(Text.of(
                            TextColors.GREEN, "GTS ", TextColors.GRAY, "\u00bb Ignore Status: ",
                            TextColors.RED, "False"
                    ));
                } else {
                    GTS.getInstance().getIgnoreList().add((Player)src);
                    src.sendMessage(Text.of(
                            TextColors.GREEN, "GTS ", TextColors.GRAY, "\u00bb Ignore Status: ",
                            TextColors.GREEN, "True"
                    ));
                }
            }
        }

        return CommandResult.success();
    }
}
