package com.nickimpact.GTS.commands;

import com.google.common.collect.Maps;
import com.nickimpact.GTS.GTS;
import com.nickimpact.GTS.configuration.MessageConfig;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.HashMap;
import java.util.Optional;

/**
 * Created by nickd on 4/17/2017.
 */
public class LogClearCmd implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Optional<User> user = args.getOne("target");
        user.ifPresent(u -> {
            GTS.getInstance().getSql().purgeLogs(u.getUniqueId());
            GTS.getInstance().getLogger().info(Text.of(TextColors.RED, "All logs for the UUID (" + u.getUniqueId() + ") have been erased!").toPlain());

            HashMap<String, Optional<Object>> textOptions = Maps.newHashMap();
            textOptions.put("player", Optional.of(u.getName()));
            for(Text text : MessageConfig.getMessages("Administrative.Clear-Logs", textOptions))
                src.sendMessage(text);
        });

        return CommandResult.success();
    }
}
