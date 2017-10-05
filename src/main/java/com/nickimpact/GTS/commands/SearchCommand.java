package com.nickimpact.GTS.commands;

import java.util.HashMap;
import java.util.List;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.GTS.guis.MainUI;

/**
 * Created by Nick on 12/15/2016.
 */
public class SearchCommand implements CommandExecutor {
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if(src instanceof Player){
			List<String> pokemon = Lists.newArrayList(args.getOne("pokemons").get().toString().split(" "));
			HashMap<String, Object> parameters = gatherParameters(args);
			if(args.hasAny("pokemons")) {
				if(parameters.size() > 0){
					((Player) src).openInventory(new MainUI((Player)src, 1, true, pokemon, parameters).getInventory());
					return CommandResult.success();
				} else {
					((Player) src).openInventory(new MainUI((Player) src, 1, true, pokemon, Maps.newHashMap()).getInventory());
					return CommandResult.success();
				}
			} else {
				if(parameters.size() == 0)
					throw new CommandException(Text.of("No arguments passed to the search command.."));
				else {
					((Player) src).openInventory(new MainUI((Player)src, 1, true, Lists.newArrayList(), parameters).getInventory());
					return CommandResult.success();
				}
			}
		} else {
			throw new CommandException(Text.of("Only players may use this command.."));
		}
	}

	private HashMap<String, Object> gatherParameters(CommandContext args) {
		HashMap<String, Object> parameters = Maps.newHashMap();
		for(String flag : getFlags(args)){
			parameters.put(flag, args.getOne(flag).get());
		}
		return parameters;
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
				.permission("gts.command.search")
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
						.flag("s", "-shiny")
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
}
