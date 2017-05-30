package com.nickimpact.GTS.commands;

import com.nickimpact.GTS.GTS;
import com.nickimpact.GTS.utils.LotUtils;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
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
 * Created by Nick on 12/15/2016.
 */
public class AdditionCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if(src instanceof Player){
            Optional<String> note = args.getOne("note");

            Integer slot = args.<Integer>getOne("slot").get() - 1;
            if(slot < 0 || slot > 5){
                throw new CommandException(Text.of(TextColors.RED, "Error ", TextColors.GRAY, "| ", TextColors.WHITE, "You must specify a number between 1 - 6"));
            }

            // Optional arguments based on command contents
            Optional<Integer> price = args.getOne("price");
            if(price.isPresent())
                if(price.get() < 1)
                    throw new CommandException(Text.of(TextColors.RED, "Price can not be lower than $1.."));

            // We need a flag to state an entry can never expire
            boolean expires = !args.hasAny("n") && !args.hasAny("never-expires");
            if(!expires && !src.hasPermission("gts.command.addition.never-expire"))
                throw new CommandException(Text.of("Sorry, but you can't mark listings to not expire..."));

            // Fetch the custom time for a listing, if it exists
            Optional<Long> customExpires = args.getOne("expires");
            if(customExpires.isPresent()){
                if(customExpires.get() < 0){
                    throw new CommandException(Text.of(TextColors.RED, "Expiration can't be lower than 1 second!"));
                }
            }

            LotUtils.addPokemonStatic((Player) src, slot, note.orElse(""), price.get(), expires, customExpires.orElse(
                    GTS.getInstance().getConfig().getLotTime()
            ));

            return CommandResult.success();
        } else {
            throw new CommandException(Text.of("Only players can add a pokemon to the market!"));
        }
    }

    public static CommandSpec registerCommand(){
        return CommandSpec.builder()
                .permission("gts.command.add")
                .executor(new AdditionCommand())
                .arguments(GenericArguments.flags()
                                .flag("n", "-never-expires")
                                .buildWith(GenericArguments.none()),
                        GenericArguments.integer(Text.of("slot")),
                        GenericArguments.integer(Text.of("price")),
                        GenericArguments.optionalWeak(GenericArguments.longNum(Text.of("expires"))),
                        GenericArguments.optionalWeak(GenericArguments.remainingJoinedStrings(Text.of("note"))))
                .description(Text.of("Add a pokemon to the GTS"))
                .build();
    }
}
