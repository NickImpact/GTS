package com.nickimpact.GTS.commands;

import com.google.common.collect.Lists;
import com.nickimpact.GTS.GTS;
import com.nickimpact.GTS.guis.Main;
import com.nickimpact.GTS.utils.Lot;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Moveset;
import com.pixelmonmod.pixelmon.enums.EnumEggGroup;
import com.pixelmonmod.pixelmon.enums.EnumGrowth;
import com.pixelmonmod.pixelmon.enums.EnumNature;
import com.pixelmonmod.pixelmon.enums.heldItems.EnumHeldItems;
import com.pixelmonmod.pixelmon.enums.items.EnumPokeballs;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Nick on 12/15/2016.
 */
public class SearchCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if(src instanceof Player){
            if(args.hasAny("pokemons")) {
                List<String> flags;
                if((flags = getFlags(args)).size() > 0){
                    List<String> pokemon = Lists.newArrayList(args.getOne("pokemons").get().toString().split(" "));
                    List<Lot> validPokemon = forgeValidSearch(args, flags, pokemon);
                    Main.showGUI((Player) src, 1, true, validPokemon);
                    return CommandResult.success();
                } else {
                    List<String> pokemon = Lists.newArrayList(args.getOne("pokemons").get().toString().split(" "));
                    List<Lot> validPokemon = Lists.newArrayList();
                    for(Lot lot : GTS.getInstance().getSql().getAllLots()){
                        String name = lot.getItem().getName();
                        for(String poke : pokemon){
                            if(poke.equalsIgnoreCase(name)) validPokemon.add(lot);
                        }
                    }
                    Main.showGUI((Player) src, 1, true, validPokemon);
                    return CommandResult.success();
                }
            } else {
                List<String> flags;
                if((flags = getFlags(args)).size() == 0)
                    throw new CommandException(Text.of("No arguments passed to the search command.."));
                else {
                    Main.showGUI((Player) src, 1, true, forgeValidSearch(args, flags, Lists.newArrayList()));
                    return CommandResult.success();
                }
            }
        } else {
            throw new CommandException(Text.of("Only players may use this command.."));
        }
    }

    private List<String> getFlags(CommandContext args) {
        List<String> flags = Lists.newArrayList("size", "growth",
                                                "lvl", "level",
                                                "ab", "ability",
                                                "ba", "ball",
                                                "evHP", "evAtk", "evDef", "evSpAtk", "evSpDef", "evSpeed",
                                                "ivHP", "ivAtk", "ivDef", "ivSpAtk", "ivSpDef", "ivSpeed",
                                                "ge", "gender",
                                                "na", "nature",
                                                "f", "form",
                                                "s", "shiny",
                                                "friendship",
                                                "hasMove",
                                                "eggGroup",
                                                "st", "specialTexture", "halloween", "roasted", "zombie",
                                                "minPrice", "maxPrice", "seller",
                                                "he", "heldItem",
                                                "auc", "auction",
                                                "cash", "pokemon");

        List<String> foundFlags = Lists.newArrayList();

        for(String flag : flags){
            if(args.hasAny(flag)){
                foundFlags.add(flag);
            }
        }

        return foundFlags;
    }

    public static CommandSpec registerCommand(){

        return CommandSpec.builder()
                .executor(new SearchCommand())
                .arguments(GenericArguments.flags()
                        .valueFlag(GenericArguments.string(Text.of("growth")), "-size", "-growth")
                        .valueFlag(GenericArguments.string(Text.of("ability")), "-ab", "-ability")
                        .valueFlag(GenericArguments.string(Text.of("ball")), "-ba", "-ball")
                        .valueFlag(GenericArguments.string(Text.of("gender")), "-ge", "-gender")
                        .valueFlag(GenericArguments.string(Text.of("nature")), "-na", "-nature")
                        .valueFlag(GenericArguments.string(Text.of("hasMove")), "-hasMove")
                        .valueFlag(GenericArguments.string(Text.of("eggGroup")), "-eggGroup")
                        .valueFlag(GenericArguments.string(Text.of("heldItem")), "-he", "-heldItem")
                        .valueFlag(GenericArguments.string(Text.of("seller")), "-seller")
                        .valueFlag(GenericArguments.integer(Text.of("level")), "-lvl", "-level")
                        .valueFlag(GenericArguments.integer(Text.of("evhp")), "-evHP")
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
                        .valueFlag(GenericArguments.integer(Text.of("shiny")), "s", "shiny")
                        .valueFlag(GenericArguments.integer(Text.of("friendship")), "-friendship")
                        .flag("-st", "-specialTexture")
                            .flag("-halloween", "-zombie")
                            .flag("-roasted")
                        .valueFlag(GenericArguments.integer(Text.of("minPrice")), "-minPrice")
                        .valueFlag(GenericArguments.integer(Text.of("maxPrice")), "-maxPrice")
                        .flag("-auc", "-auction")
                        .flag("-cash")
                        .flag("-pokemon")
                        .buildWith(GenericArguments.none()),

                        GenericArguments.optionalWeak(GenericArguments.remainingJoinedStrings(Text.of("pokemons"))))
                .description(Text.of("Search for a pokemon within the GTS"))
                .build();
    }

    private static List<Lot> forgeValidSearch(CommandContext args, List<String> flags, List<String> pokemon) throws CommandException{
        List<Lot> lots = GTS.getInstance().getSql().getAllLots();
        List<Lot> validListings = Lists.newArrayList();
        for(Lot lot : lots){
            EntityPixelmon poke = lot.getItem().getPokemon(lot, Sponge.getServer().getPlayer(lot.getOwner()).get());
            boolean addLot = true;
            if(pokemon.size() == 0 || pokemon.stream().anyMatch(p -> p.equalsIgnoreCase(poke.getName()))) {
                for (String flag : flags) {
                    switch (flag) {
                        case "s":
                        case "shiny":
                            if (!poke.getIsShiny())
                                addLot = false;
                            break;
                        case "ab":
                        case "ability":
                            String ability = args.<String>getOne(flag).get();
                            try {
                                Class.forName("com.pixelmonmod.pixelmon.entities.pixelmon.abilities." + ability);
                            } catch (ClassNotFoundException e) {
                                throw new CommandException(Text.of("That passed ability doesn't exist..."));
                            }

                            if (!poke.getAbility().getLocalizedName().equalsIgnoreCase(ability))
                                addLot = false;
                            break;
                        case "size":
                        case "growth":
                            String growth = args.<String>getOne(flag).get();
                            if(!EnumGrowth.hasGrowth(growth))
                                throw new CommandException(Text.of("The specified growth doesn't exist..."));
                            if (!poke.getGrowth().name().equalsIgnoreCase(growth))
                                addLot = false;
                            break;
                        case "lvl":
                        case "level":
                            int lvl = args.<Integer>getOne(flag).get();
                            if(lvl < 1 || lvl > PixelmonConfig.maxLevel)
                                throw new CommandException(Text.of("The level must be between 1-" + PixelmonConfig.maxLevel + "..."));

                            if (poke.getLvl().getLevel() != lvl)
                                addLot = false;
                            break;
                        case "ba":
                        case "ball":
                            String ball = args.<String>getOne(flag).get();
                            if(!EnumPokeballs.hasPokeball(ball))
                                throw new CommandException(Text.of("The specified pokeball doesn't exist..."));

                            if (!poke.caughtBall.name().equalsIgnoreCase(ball))
                                addLot = false;
                            break;
                        case "ge":
                        case "gender":
                            String gender = args.<String>getOne(flag).get();
                            if(!gender.equalsIgnoreCase("male") && !gender.equalsIgnoreCase("female") && !gender.equalsIgnoreCase("none") && !gender.equalsIgnoreCase("genderless"))
                                throw new CommandException(Text.of("The specified gender doesn't exist..."));

                            if (!poke.gender.name().equalsIgnoreCase(gender))
                                addLot = false;
                            break;
                        case "na":
                        case "nature":
                            String nature = args.<String>getOne(flag).get();
                            if(!EnumNature.hasNature(nature))
                                throw new CommandException(Text.of("The specified nature doesn't exist..."));

                            if (!poke.getNature().name().equalsIgnoreCase(nature))
                                addLot = false;
                            break;
                        case "f":
                        case "form":
                            int form = args.<Integer>getOne(flag).get();
                            if(form < -1)
                                throw new CommandException(Text.of("Form values must be -1 or more..."));

                            if (poke.getForm() != form)
                                addLot = false;
                            break;
                        case "evHP":
                            int evHp = args.<Integer>getOne(flag).get();
                            if(evHp < 0 || evHp > 255)
                                throw new CommandException(Text.of("EVs must be between 0-255..."));

                            if (poke.stats.EVs.HP < evHp)
                                addLot = false;
                            break;
                        case "evAtk":
                            int evAtk = args.<Integer>getOne(flag).get();
                            if(evAtk < 0 || evAtk > 255)
                                throw new CommandException(Text.of("EVs must be between 0-255..."));

                            if (poke.stats.EVs.Attack < evAtk)
                                addLot = false;
                            break;
                        case "evDef":
                            int evDef = args.<Integer>getOne(flag).get();
                            if(evDef < 0 || evDef > 255)
                                throw new CommandException(Text.of("EVs must be between 0-255..."));

                            if (poke.stats.EVs.Defence < evDef)
                                addLot = false;
                            break;
                        case "evSpAtk":
                            int evSpAtk = args.<Integer>getOne(flag).get();
                            if(evSpAtk < 0 || evSpAtk > 255)
                                throw new CommandException(Text.of("EVs must be between 0-255..."));

                            if (poke.stats.EVs.SpecialAttack < evSpAtk)
                                addLot = false;
                            break;
                        case "evSpDef":
                            int evSpDef = args.<Integer>getOne(flag).get();
                            if(evSpDef < 0 || evSpDef > 255)
                                throw new CommandException(Text.of("EVs must be between 0-255..."));

                            if (poke.stats.EVs.SpecialDefence < evSpDef)
                                addLot = false;
                            break;
                        case "evSpeed":
                            int evSpeed = args.<Integer>getOne(flag).get();
                            if(evSpeed < 0 || evSpeed > 255)
                                throw new CommandException(Text.of("EVs must be between 0-255..."));

                            if (poke.stats.EVs.Speed < evSpeed)
                                addLot = false;
                            break;
                        case "ivHP":
                            int ivHp = args.<Integer>getOne(flag).get();
                            if(ivHp < 0 || ivHp > 31)
                                throw new CommandException(Text.of("IVs must be between 0-31..."));

                            if (poke.stats.IVs.HP < ivHp)
                                addLot = false;
                            break;
                        case "ivAtk":
                            int ivAtk = args.<Integer>getOne(flag).get();
                            if(ivAtk < 0 || ivAtk > 31)
                                throw new CommandException(Text.of("IVs must be between 0-31..."));

                            if (poke.stats.IVs.Attack < ivAtk)
                                addLot = false;
                            break;
                        case "ivDef":
                            int ivDef = args.<Integer>getOne(flag).get();
                            if(ivDef < 0 || ivDef > 31)
                                throw new CommandException(Text.of("IVs must be between 0-31..."));

                            if (poke.stats.IVs.Defence < ivDef)
                                addLot = false;
                            break;
                        case "ivSpAtk":
                            int ivSpAtk = args.<Integer>getOne(flag).get();
                            if(ivSpAtk < 0 || ivSpAtk > 31)
                                throw new CommandException(Text.of("IVs must be between 0-31..."));

                            if (poke.stats.IVs.SpAtt < ivSpAtk)
                                addLot = false;
                            break;
                        case "ivSpDef":
                            int ivSpDef = args.<Integer>getOne(flag).get();
                            if(ivSpDef < 0 || ivSpDef > 31)
                                throw new CommandException(Text.of("IVs must be between 0-31..."));

                            if (poke.stats.IVs.SpDef < ivSpDef)
                                addLot = false;
                            break;
                        case "ivSpeed":
                            int ivSpeed = args.<Integer>getOne(flag).get();
                            if(ivSpeed < 0 || ivSpeed > 31)
                                throw new CommandException(Text.of("IVs must be between 0-31..."));

                            if (poke.stats.IVs.SpDef < ivSpeed)
                                addLot = false;
                            break;
                        case "friendship":
                            int fr = args.<Integer>getOne(flag).get();
                            if(fr < 0 || fr > 255)
                                throw new CommandException(Text.of("Friendship must be between 0-255..."));

                            if (poke.friendship.getFriendship() < args.<Integer>getOne(flag).get())
                                addLot = false;
                            break;
                        case "hasMove":
                            Moveset moves = poke.getMoveset();
                            boolean found = false;
                            for (Attack atk : moves.attacks) {
                                if(atk == null) continue;
                                if (atk.baseAttack.getLocalizedName().equalsIgnoreCase(args.<String>getOne(flag).get())) {
                                    found = true;
                                    break;
                                }
                            }
                            if(!found) addLot = false;
                            break;
                        case "eggGroup":
                            String eg = args.<String>getOne(flag).get();
                            if(!EnumEggGroup.hasEggGroup(eg))
                                throw new CommandException(Text.of("The specified egg group doesn't exist..."));

                            boolean egFound = false;
                            for (EnumEggGroup eggGroup : poke.baseStats.eggGroups) {
                                if (eggGroup.name().equalsIgnoreCase(eg)) {
                                    egFound = true;
                                    break;
                                }
                            }
                            if(!egFound) addLot = false;
                            break;
                        case "st":
                        case "specialTexture":
                            if (poke.getSpecialTexture() == 0)
                                addLot = false;
                            break;
                        case "halloween":
                        case "zombie":
                            if (poke.getSpecialTexture() != 2)
                                addLot = false;
                            break;
                        case "roasted":
                            if (poke.getSpecialTexture() != 1)
                                addLot = false;
                            break;
                        case "minPrice":
                            int minPrice = args.<Integer>getOne(flag).get();
                            if(minPrice < 0)
                                throw new CommandException(Text.of("Min Price must be a positive number!"));

                            if (lot.getPrice() < minPrice)
                                addLot = false;
                            break;
                        case "maxPrice":
                            int maxPrice = args.<Integer>getOne(flag).get();
                            if(maxPrice < 0)
                                throw new CommandException(Text.of("Max Price must be a positive number!"));

                            if (lot.getPrice() > maxPrice)
                                addLot = false;
                            break;
                        case "seller":
                            if (!lot.getItem().getOwner().equalsIgnoreCase(args.<String>getOne(flag).get()))
                                addLot = false;
                            break;
                        case "he":
                        case "heldItem":
                            String heldItem = args.<String>getOne(flag).get();
                            if(Arrays.stream(EnumHeldItems.values()).noneMatch(h ->
                                h.name().equalsIgnoreCase(heldItem)))
                                throw new CommandException(Text.of("The specified held item doesn't exist..."));

                            if (!poke.getItemHeld().getLocalizedName().equalsIgnoreCase(heldItem))
                                addLot = false;
                            break;
                        case "auc":
                        case "auction":
                            if(!lot.isAuction())
                                addLot = false;
                            break;
                        case "cash":
                            if(lot.isAuction() || lot.isPokemon())
                                addLot = false;
                            break;
                        case "pokemon":
                            if(lot.isAuction() || !lot.isPokemon())
                                addLot = false;
                            break;
                    }
                }
            }
            if(addLot) validListings.add(lot);
        }
        return validListings;
    }
}
