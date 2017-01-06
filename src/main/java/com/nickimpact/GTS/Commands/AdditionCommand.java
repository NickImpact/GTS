package com.nickimpact.GTS.Commands;

import com.nickimpact.GTS.Utils.LotUtils;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

/**
 * Created by Nick on 12/15/2016.
 */
public class AdditionCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if(src instanceof Player){
            Integer slot = args.<Integer>getOne("slot").get() - 1;
            if(slot < 0 || slot > 5){
                throw new CommandException(Text.of(TextColors.RED, "Error ", TextColors.GRAY, "| ", TextColors.WHITE, "You must specify a number between 1 - 6"));
            }
            Integer price = args.<Integer>getOne("price").get();
            LotUtils.addPokemonToMarket((Player)src, slot, price);
            return CommandResult.success();
        }
        throw new CommandException(Text.of("Only players can add a pokemon to the market!"));
    }
}
