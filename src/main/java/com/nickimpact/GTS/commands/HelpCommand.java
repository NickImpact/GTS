package com.nickimpact.GTS.commands;

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
        sender.sendMessage(Text.of(TextColors.AQUA, "/gts ", TextColors.GRAY, "\u00BB", TextColors.WHITE, " Open the GTS market by default"));
        sender.sendMessage(Text.of(TextColors.AQUA, "/gts add {slot} {price} ", TextColors.GRAY, "\u00BB", TextColors.WHITE, " Add a pokemon to the GTS"));
        sender.sendMessage(Text.of(TextColors.AQUA, "/gts search {pokemon...} ", TextColors.GRAY, "\u00BB", TextColors.WHITE, " Search for pokemon in the GTS"));
        if (sender.hasPermission("gts.admin")) {
            sender.sendMessage(Text.of(TextColors.AQUA, "/gts edit ", TextColors.GRAY, "\u00BB", TextColors.WHITE, " Edit an entry in the GTS"));
            sender.sendMessage(Text.of(TextColors.AQUA, "/gts clear ", TextColors.GRAY, "\u00BB", TextColors.WHITE, " Clear all entries in the GTS"));
            sender.sendMessage(Text.of(TextColors.AQUA, "/gts reload ", TextColors.GRAY, "\u00BB", TextColors.WHITE, " Reload configuration related to GTS"));
        }
        sender.sendMessage(Text.EMPTY);
        return CommandResult.success();
    }
}
