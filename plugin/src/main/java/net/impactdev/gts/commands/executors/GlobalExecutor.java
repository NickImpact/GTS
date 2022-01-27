package net.impactdev.gts.commands.executors;

import net.impactdev.gts.api.commands.annotations.Alias;
import net.impactdev.gts.api.commands.annotations.Permission;
import net.impactdev.gts.commands.executors.subs.AdminExecutor;
import net.impactdev.gts.commands.executors.subs.AuctionCommand;
import net.impactdev.gts.commands.executors.subs.SellCommand;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.plugin.permissions.GTSPermissions;
import net.impactdev.gts.sponge.commands.SpongeGTSCmdExecutor;
import net.impactdev.gts.ui.SpongeMainMenu;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

@Alias("gts")
@Permission(GTSPermissions.DEFAULT)
public class GlobalExecutor extends SpongeGTSCmdExecutor {

    public GlobalExecutor(GTSPlugin plugin) {
        super(plugin);
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[0];
    }

    @Override
    public SpongeGTSCmdExecutor[] getSubCommands() {
        return new SpongeGTSCmdExecutor[] {
                new SellCommand(),
                new AuctionCommand(),
                new AdminExecutor()
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
