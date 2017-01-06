package com.nickimpact.GTS.Commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

/**
 * Created by Nick on 12/16/2016.
 */
public class HelpCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource sender, CommandContext args) throws CommandException {
        sender.sendMessage(Text.of(TextColors.YELLOW, "-------------------", TextColors.RED, "GTS Help", TextColors.YELLOW, "--------------------"));
        sender.sendMessage(Text.of(TextColors.GRAY, "Friendly Command Helper: "));
        sender.sendMessage(Text.of(TextColors.YELLOW, "{}", TextColors.GRAY, "= Required   ", TextColors.YELLOW, "<>", TextColors.GRAY,"= Optional   ", TextColors.YELLOW,"... ", TextColors.GRAY,"= Accepts more than 1"));
        sender.sendMessage(Text.EMPTY);
        sender.sendMessage(Text.of(TextColors.AQUA, "/gts ", TextColors.GRAY, "» §fOpen the GTS market by default"));
        sender.sendMessage(Text.of(TextColors.AQUA, "/gts add {slot} {price} ", TextColors.GRAY, "» §fAdd a pokemon to the GTS"));
        sender.sendMessage(Text.of(TextColors.AQUA, "/gts search {pokemon...} ", TextColors.GRAY, "» §fSearch for pokemon in the GTS"));
        if (sender.hasPermission("gts.admin")) {
            sender.sendMessage(Text.of(TextColors.AQUA, "/gts edit ", TextColors.GRAY, "» §fEdit an entry in the GTS"));
            sender.sendMessage(Text.of(TextColors.AQUA, "/gts clear ", TextColors.GRAY, "» §fClear all entries in the GTS"));
            sender.sendMessage(Text.of(TextColors.AQUA, "/gts reload ", TextColors.GRAY, "» §fReload configuration related to GTS"));
        }
        sender.sendMessage(Text.EMPTY);
        return CommandResult.success();
    }
}
