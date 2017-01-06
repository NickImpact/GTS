package com.nickimpact.GTS.Commands;

import com.nickimpact.GTS.Configuration.MessageConfig;
import com.nickimpact.GTS.GTS;
import com.nickimpact.GTS.Utils.Lot;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

import java.util.List;

/**
 * Created by Nick on 12/15/2016.
 */
public class ClearCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        GTS.getInstance().getSql().returnLots();
        src.sendMessage(MessageConfig.getMessage("Admin.Clear"));
        return CommandResult.success();
    }
}
