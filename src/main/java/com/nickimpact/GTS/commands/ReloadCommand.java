package com.nickimpact.GTS.commands;

import com.nickimpact.GTS.configuration.MessageConfig;
import com.nickimpact.GTS.GTS;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;

/**
 * Created by Nick on 12/15/2016.
 */
public class ReloadCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        GTS.getInstance().getConfig().reload();

        for(Text text : MessageConfig.getMessages("Administrative.Reload", null))
            src.sendMessage(text);
        return CommandResult.success();
    }
}
