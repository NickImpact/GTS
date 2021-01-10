package net.impactdev.gts.commands.executors;

import net.impactdev.gts.commands.annotations.Alias;
import net.impactdev.gts.commands.annotations.Permission;
import net.impactdev.gts.commands.executors.subs.AdminExecutor;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.ui.SpongeMainMenu;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Optional;

@Alias("gts")
@Permission("gts.base")
public class GlobalExecutor extends GTSCmdExecutor {

    public GlobalExecutor(GTSPlugin plugin) {
        super(plugin);
    }

    @Override
    public Optional<Text> getDescription() {
        return Optional.empty();
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[0];
    }

    @Override
    public GTSCmdExecutor[] getSubcommands() {
        return new GTSCmdExecutor[] {
                new AdminExecutor(this.plugin)
        };
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext arguments) throws CommandException {
        if(source instanceof Player) {
            new SpongeMainMenu((Player) source).open();
            return CommandResult.success();
        }

        throw new CommandException(Text.of("Only players can use the base command!"));
    }

}
