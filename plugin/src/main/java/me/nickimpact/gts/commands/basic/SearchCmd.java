package me.nickimpact.gts.commands.basic;

import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.GtsService;
import com.nickimpact.impactor.api.commands.SpongeCommand;
import com.nickimpact.impactor.api.commands.SpongeSubCommand;
import com.nickimpact.impactor.api.commands.annotations.Aliases;
import com.nickimpact.impactor.api.plugins.SpongePlugin;
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
@Aliases({"search"})
public class SearchCmd extends SpongeSubCommand {

	public SearchCmd(SpongePlugin plugin) {
		super(plugin);
	}

	@SuppressWarnings("unchecked")
	@Override
	public CommandElement[] getArgs() {
		CommandFlags.Builder builder = GenericArguments.flags();
		Collection collection = Objects.requireNonNull(
				GTS.getInstance().getService().getRegistry(GtsService.RegistryType.ENTRY)).getTypings().values();
		collection.forEach(clazz -> builder.flag("-" + ((Class)clazz).getSimpleName().toLowerCase()));

		return new CommandElement[] {
				builder.buildWith(GenericArguments.none())
		};
	}

	@Override
	public Text getDescription() {
		return Text.of("Scan the listings for a set of specified criteria");
	}

	@Override
	public Text getUsage() {
		return Text.of("/gts search <parameters>");
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
