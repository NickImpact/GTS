package com.nickimpact.GTS.commands;

import com.google.common.collect.Maps;
import com.nickimpact.GTS.configuration.MessageConfig;
import com.nickimpact.GTS.GTS;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;

import java.util.HashMap;
import java.util.Optional;

/**
 * Created by Nick on 12/15/2016.
 */
public class ClearCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        int cleared = GTS.getInstance().getSql().clearLots();

        HashMap<String, Optional<Object>> textOptions = Maps.newHashMap();
        textOptions.put("cleared", Optional.of(cleared));
        for(Text text : MessageConfig.getMessages("Administrative.Clear", textOptions))
            src.sendMessage(text);
        return CommandResult.success();
    }
}
