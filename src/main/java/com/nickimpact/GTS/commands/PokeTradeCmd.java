package com.nickimpact.GTS.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.GTS.GTS;
import com.nickimpact.GTS.GTSInfo;
import com.nickimpact.GTS.configuration.MessageConfig;
import com.nickimpact.GTS.guis.builder.BuilderBase;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.block.trait.IntegerTraits;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class PokeTradeCmd implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (GTS.getInstance().getConfig().cmdTradeEnabled()) {
            if (src instanceof Player) {
                int slot = args.<Integer>getOne("slot").get() - 1;
                String pokemon = args.<String>getOne("pokemon").get();
                String note = args.<String>getOne("note").orElse("");
                HashMap<String, Object> specs = getSpecs(args);

                if(slot < 0 && slot > 5)
                    throw new CommandException(Text.of("Invalid slot"));

                if(!EnumPokemon.hasPokemonAnyCase(pokemon))
                    throw new CommandException(Text.of("The pokemon asked for is not within the mod..."));

                // Valid pokemon, so let's continue operation
                Player player = (Player)src;

                Optional<PlayerStorage> storage = PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID((MinecraftServer) Sponge.getServer(), player.getUniqueId());
                if(storage.isPresent()){
                    if(storage.get().partyPokemon[slot] == null) {
                        HashMap<String, Optional<Object>> textOptions = Maps.newHashMap();
                        textOptions.put("slot", Optional.of(slot + 1));

                        for (Text text : MessageConfig.getMessages("Generic.Addition.Error.Empty Slot", textOptions))
                            player.sendMessage(text);
                    }

                }

                player.openInventory(new BuilderBase(player, pokemon, specs, slot, note, args.hasAny("-e"),
                                                     args.<Long>getOne("time").orElse(GTS.getInstance().getConfig().getLotTime()))
                                             .getInventory(),
                                     Cause.of(NamedCause.source(GTS.getInstance()))
                );
            } else {
                throw new CommandException(Text.of("Only players may use this feature of GTS..."));
            }
            return CommandResult.success();
        }

        throw new CommandException(Text.of("This command has been disabled via the config!"));
    }

    private HashMap<String, Object> getSpecs(CommandContext args){
        HashMap<String, Object> specs = Maps.newHashMap();
        for(String flag : getFlags(args)){
            specs.put(flag, args.getOne(flag).orElse(true));
        }
        return specs;
    }

    private List<String> getFlags(CommandContext args) {
        List<String> flags = Lists.newArrayList(
                "size", "growth",
                "lvl", "level",
                "ab", "ability",
                "ba", "ball",
                "evHP", "evAtk", "evDef", "evSpAtk", "evSpDef", "evSpeed",
                "ivHP", "ivAtk", "ivDef", "ivSpAtk", "ivSpDef", "ivSpeed",
                "ge", "gender",
                "na", "nature",
                "f", "form",
                "s", "shiny",
                "p", "particle",
                "st", "specialTexture", "halloween", "roasted", "zombie",
                "he", "heldItem"
        );

        List<String> foundFlags = Lists.newArrayList();

        for (String flag : flags) {
            if (args.hasAny(flag)) {
                foundFlags.add(flag);
            }
        }

        return foundFlags;
    }

    public static CommandSpec registerCommand() {

        // Command: /gts trade [flags] (slot) (pokemon)

        return CommandSpec.builder()
                .permission("gts.command.trade")
                .executor(new PokeTradeCmd())
                .arguments(
                        GenericArguments.flags()
                                .valueFlag(GenericArguments.string(Text.of("growth")), "-size", "-growth")
                                .valueFlag(GenericArguments.string(Text.of("ability")), "-ab", "-ability")
                                .valueFlag(GenericArguments.string(Text.of("ball")), "-ba", "-ball")
                                .valueFlag(GenericArguments.string(Text.of("gender")), "-ge", "-gender")
                                .valueFlag(GenericArguments.string(Text.of("nature")), "-na", "-nature")
                                .valueFlag(GenericArguments.string(Text.of("heldItem")), "-he", "-heldItem")
                                .valueFlag(GenericArguments.integer(Text.of("level")), "-lvl", "-level")
                                .valueFlag(GenericArguments.integer(Text.of("evHP")), "-evHP")
                                .valueFlag(GenericArguments.integer(Text.of("evAtk")), "-evAtk")
                                .valueFlag(GenericArguments.integer(Text.of("evDef")), "-evDef")
                                .valueFlag(GenericArguments.integer(Text.of("evSpAtk")), "-evSpAtk")
                                .valueFlag(GenericArguments.integer(Text.of("evSpDef")), "-evSpDef")
                                .valueFlag(GenericArguments.integer(Text.of("evSpeed")), "-evSpeed")
                                .valueFlag(GenericArguments.integer(Text.of("ivHP")), "-ivHP")
                                .valueFlag(GenericArguments.integer(Text.of("ivAtk")), "-ivAtk")
                                .valueFlag(GenericArguments.integer(Text.of("ivDef")), "-ivDef")
                                .valueFlag(GenericArguments.integer(Text.of("ivSpAtk")), "-ivSpAtk")
                                .valueFlag(GenericArguments.integer(Text.of("ivSpDef")), "-ivSpDef")
                                .valueFlag(GenericArguments.integer(Text.of("ivSpeed")), "-ivSpeed")
                                .valueFlag(GenericArguments.integer(Text.of("form")), "-f", "-form")
                                .valueFlag(GenericArguments.string(Text.of("particle")), "p", "-particle")
                                .valueFlag(GenericArguments.string(Text.of("note")), "-note")
                                .flag("s", "-shiny")
                                .flag("-st", "-specialTexture")
                                .flag("-halloween", "-zombie")
                                .flag("-roasted")
                                .flag("e")
                                .buildWith(GenericArguments.none()),
                        GenericArguments.integer(Text.of("slot")),
                        GenericArguments.string(Text.of("pokemon")),
                        GenericArguments.optionalWeak(GenericArguments.longNum(Text.of("time")))
                )
                .description(Text.of("Add a pokemon to the GTS"))
                .build();
    }
}
