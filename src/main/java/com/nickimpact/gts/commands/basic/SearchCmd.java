package com.nickimpact.gts.commands.basic;

import com.nickimpact.gts.GTS;
import com.nickimpact.gts.api.annotations.CommandAliases;
import com.nickimpact.gts.api.commands.SpongeCommand;
import com.nickimpact.gts.api.commands.SpongeSubCommand;
import com.nickimpact.gts.api.listings.entries.Entry;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.CommandFlags;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;

import java.util.Collection;
import java.util.Objects;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@CommandAliases({"search"})
public class SearchCmd extends SpongeSubCommand {
	@Override
	public CommandElement[] getArgs() {
		CommandFlags.Builder builder = GenericArguments.flags();
		Collection<Class<? extends Entry>> collection = Objects.requireNonNull(
				GTS.getInstance().getApi().getRegistry(Entry.class)).getTypings().values();
		for(Class<? extends Entry> clazz : collection) {
			builder.flag("-" + clazz.getSimpleName().toLowerCase());
		}

		return new CommandElement[] {
				builder.buildWith(GenericArguments.none())
		};
	}

	@Override
	public Text getDescription() {
		return Text.of("Scan the listings for a set of specified criteria");
	}

	@Override
	public SpongeCommand[] getSubCommands() {
		return null;
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		return CommandResult.success();
	}
}
