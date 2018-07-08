package com.nickimpact.gts.commands.basic;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.gts.GTS;
import com.nickimpact.gts.GTSInfo;
import com.nickimpact.gts.api.commands.SpongeCommand;
import com.nickimpact.gts.api.commands.SpongeSubCommand;
import com.nickimpact.gts.api.commands.annotations.AdminCmd;
import com.nickimpact.gts.api.commands.annotations.CommandAliases;
import com.nickimpact.gts.commands.GTSBaseCmd;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
import java.util.Map;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@CommandAliases({"help"})
public class HelpCmd extends SpongeSubCommand {

	public static Map<String, Text> commands = Maps.newHashMap();

	@Override
	public CommandElement[] getArgs() {
		return new CommandElement[0];
	}

	@Override
	public Text getDescription() {
		return Text.of("Get help specifically for GTS");
	}

	@Override
	public Text getUsage() {
		return Text.of("/gts help");
	}

	@Override
	public SpongeCommand[] getSubCommands() {
		return new SpongeCommand[0];
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		List<Text> cmds = Lists.newArrayList();
		for(Map.Entry<String, Text> entry : commands.entrySet()) {
			if(src.hasPermission(entry.getKey())) {
				cmds.add(entry.getValue());
			}
		}

		PaginationList.builder()
				.title(Text.of(TextColors.YELLOW, "GTS Command Help"))
				.header(Text.of(
						TextColors.GRAY, "Friendly Command Helper: ", Text.NEW_LINE,
						TextColors.YELLOW, "<> ", TextColors.GRAY, "- Required", Text.NEW_LINE,
						TextColors.YELLOW, "[] ", TextColors.GRAY, "- Optional", Text.NEW_LINE
				))
				.contents(cmds)
				.sendTo(src);

		return CommandResult.success();
	}

	private List<Text> formatCommands(CommandSource src, SpongeCommand cmd) {
		List<Text> commands = Lists.newArrayList();

		if(!cmd.getClass().isAnnotationPresent(AdminCmd.class) || !src.hasPermission(cmd.getPermission())) {
			return commands;
		}

		commands.add(Text.of(
				TextColors.AQUA, cmd.getUsage(), TextColors.GRAY, " - ", TextColors.YELLOW,
				cmd.getDescription()
		));

		if(cmd.getSubCommands() != null && cmd.getSubCommands().length > 0) {
			for(SpongeCommand child : cmd.getSubCommands()) {
				if(child.getCommandSpec().testPermission(src))
					commands.addAll(this.formatCommands(src, child));
			}
		}

		return commands;
	}
}
