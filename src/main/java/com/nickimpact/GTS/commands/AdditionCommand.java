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

            Optional<String> poke = null;
            if(args.hasAny("pokemon"))
                poke = args.getOne("pokemon");
            if(poke != null && poke.isPresent())
                if(!EnumPokemon.hasPokemonAnyCase(poke.get()))
                    throw new CommandException(Text.of(TextColors.RED, "That requested pokemon doesn't exist!"));

            // We need a flag to state an entry can never expire
            if((args.hasAny("n") || args.hasAny("never-expires")) && src.hasPermission("gts.command.addition.never-expire")){
                if(price.isPresent())
                    if(note.isPresent())
                        LotUtils.addPokemonStatic((Player)src, slot, note.get(), price.get(), false, 0);
                    else
                        LotUtils.addPokemonStatic((Player)src, slot, "", price.get(), false, 0);
                else
                    if(poke != null && poke.isPresent())
                        if(note.isPresent())
                            LotUtils.addPokemon4Pokemon((Player) src, slot, note.get(), poke.get(), false, 0);
                        else
                            LotUtils.addPokemon4Pokemon((Player) src, slot, "", poke.get(), false, 0);
                    else
                        throw new CommandException(Text.of(TextColors.RED, "Not enough arguments!"));

                return CommandResult.success();
            } else if((args.hasAny("n") || args.hasAny("never-expires")) && !src.hasPermission("gts.command.add.never-expire")){
                throw new CommandException(Text.of("You don't have the permission to mark listings as never-expiring!"));
            }

            // Fetch the custom time for a listing, if it exists
            Optional<Long> customExpires = args.getOne("expires");
            if(customExpires.isPresent()){
                if(customExpires.get() < 0){
                    throw new CommandException(Text.of(TextColors.RED, "Expiration can't be lower than 1 second!"));
                }
                if(price.isPresent())
                    if(note.isPresent())
                        LotUtils.addPokemonStatic((Player) src, slot, note.get(), price.get(), true, customExpires.get());
                    else
                        LotUtils.addPokemonStatic((Player) src, slot, "", price.get(), true, customExpires.get());
                else
                    if(poke != null && poke.isPresent())
                        if(note.isPresent())
                            LotUtils.addPokemon4Pokemon((Player) src, slot, note.get(), poke.get(), true, customExpires.get());
                        else
                            LotUtils.addPokemon4Pokemon((Player) src, slot, "", poke.get(), true, customExpires.get());
            } else {
                if(price.isPresent())
                    if(note.isPresent())
                        LotUtils.addPokemonStatic((Player) src, slot, note.get(), price.get(), true, GTS.getInstance().getConfig().getLotTime());
                    else
                        LotUtils.addPokemonStatic((Player) src, slot, "", price.get(), true, GTS.getInstance().getConfig().getLotTime());
                else
                    if(poke != null && poke.isPresent())
                        if(note.isPresent())
                            LotUtils.addPokemon4Pokemon((Player) src, slot, note.get(), poke.get(), true, GTS.getInstance().getConfig().getLotTime());
                        else
                            LotUtils.addPokemon4Pokemon((Player) src, slot, "", poke.get(), true, GTS.getInstance().getConfig().getLotTime());
                    else
                        throw new CommandException(Text.of("Invalid pokemon addition"));
            }
            return CommandResult.success();
        }
        throw new CommandException(Text.of("Only players can add a pokemon to the market!"));
    }

    public static CommandSpec registerCommand(){

        return CommandSpec.builder()
                .permission("gts.command.add")
                .executor(new AdditionCommand())
                .arguments(GenericArguments.flags()
                                .flag("n", "-never-expires")
                                .valueFlag(GenericArguments.string(Text.of("pokemon")), "-poke", "-pokemon")
                                .buildWith(GenericArguments.none()),
                        GenericArguments.integer(Text.of("slot")),
                        GenericArguments.optionalWeak(GenericArguments.integer(Text.of("price"))),
                        GenericArguments.optionalWeak(GenericArguments.longNum(Text.of("expires"))),
                        GenericArguments.optionalWeak(GenericArguments.remainingJoinedStrings(Text.of("note"))))
                .description(Text.of("Add a pokemon to the GTS"))
                .build();
    }
}
