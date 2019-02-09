package me.nickimpact.gts.commands.basic;

import com.nickimpact.impactor.api.commands.SpongeCommand;
import com.nickimpact.impactor.api.commands.SpongeSubCommand;
import com.nickimpact.impactor.api.commands.annotations.Aliases;
import com.nickimpact.impactor.api.commands.elements.BaseCommandElement;
import com.nickimpact.impactor.api.plugins.SpongePlugin;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.holders.EntryClassification;
import me.nickimpact.gts.ui.SellUI;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Optional;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@Aliases({"sell", "add"})
public class SellCmd extends SpongeSubCommand {

	private static final Text TYPE = Text.of("type");
	private static final Text CONTEXT = Text.of("context");

	public SellCmd(SpongePlugin plugin) {
		super(plugin);
	}

	@Override
	public CommandElement[] getArgs() {
		return new CommandElement[] {
				GenericArguments.optional(GenericArguments.string(TYPE)),
				GenericArguments.optional(GenericArguments.remainingJoinedStrings(CONTEXT))
		};
	}

	@Override
	public Text getDescription() {
		return Text.of("Adds a listing to the market");
	}

	@Override
	public Text getUsage() {
		return Text.of("/gts sell");
	}

	@Override
	public SpongeCommand[] getSubCommands() {
		return new SpongeCommand[0];
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if(src instanceof Player) {
			Optional<String> typing = args.getOne(TYPE);
			Optional<String> context = args.getOne(CONTEXT);
			if(typing.isPresent()) {
				EntryClassification classification = GTS.getInstance().getService().getEntryRegistry().getForIdentifier(typing.get()).orElse(null);
				if(classification == null) {
					new SellUI((Player) src).open((Player) src, 1);
					return CommandResult.success();
				} else {
					try {
						if (classification.getCmdHandler().apply(src, context.orElse("").split(" ")).equals(CommandResult.empty())) {
							throw new CommandException(Text.of("Invalid syntax for that typing!"));
						}

						return CommandResult.success();
					} catch (Exception e) {
						throw new CommandException(Text.of("Invalid syntax for that typing!"));
					}
				}
			} else {
				new SellUI((Player) src).open((Player) src, 1);
				return CommandResult.success();
			}
		}

		throw new CommandException(Text.of("Only players can use this command..."));
	}
}
