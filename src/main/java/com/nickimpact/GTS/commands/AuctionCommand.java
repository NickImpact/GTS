package com.nickimpact.GTS.commands;

import com.nickimpact.GTS.GTS;
import com.nickimpact.GTS.utils.LotUtils;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

/**
 * Created by nickd on 4/11/2017.
 */
public class AuctionCommand implements CommandExecutor{
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if(src instanceof Player) {
            Optional<String> note = args.getOne("note");

            Integer slot = args.<Integer>getOne("slot").get() - 1;
            if (slot < 0 || slot > 5) {
                throw new CommandException(Text.of(TextColors.RED, "Error ", TextColors.GRAY, "| ", TextColors.WHITE, "You must specify a number between 1 - 6"));
            }

            Integer price = args.<Integer>getOne("price").get();
            if(price < 1)
                throw new CommandException(Text.of(TextColors.RED, "Starting price can not be lower than $0..."));

            Integer increment = args.<Integer>getOne("increment").get();
            if(increment < 1)
                throw new CommandException(Text.of(TextColors.RED, "Increment can not be lower than $1..."));

            Optional<Long> time = args.getOne("expires");
            if(time.isPresent())
                if(note.isPresent())
                    LotUtils.addPokemonAuc((Player)src, slot, note.get(), price, increment, time.get());
                else
                    LotUtils.addPokemonAuc((Player)src, slot, "", price, increment, time.get());
            else
                if(note.isPresent())
                    LotUtils.addPokemonAuc((Player)src, slot, note.get(), price, increment, GTS.getInstance().getConfig().getAucTime());
                else
                    LotUtils.addPokemonAuc((Player)src, slot, "", price, increment, GTS.getInstance().getConfig().getAucTime());

            return CommandResult.success();
        }
        throw new CommandException(Text.of("Only players can add a pokemon to the market!"));
    }

    public static CommandSpec registerCommand(){
        return CommandSpec.builder()
                .permission("gts.command.auction")
                .executor(new AuctionCommand())
                .arguments(GenericArguments.integer(Text.of("slot")),
                        GenericArguments.integer(Text.of("price")),
                        GenericArguments.integer(Text.of("increment")),
                        GenericArguments.optionalWeak(GenericArguments.longNum(Text.of("expires"))),
                        GenericArguments.optionalWeak(GenericArguments.remainingJoinedStrings(Text.of("note"))))
                .description(Text.of("Add a pokemon to the GTS"))
                .build();
    }
}
