package net.impactdev.gts.commands;

import net.impactdev.gts.locale.Messages;
import net.impactdev.gts.permissions.GTSPermissions;
import net.impactdev.impactor.api.commands.ImpactorCommand;
import net.impactdev.impactor.api.commands.annotations.Alias;
import net.impactdev.impactor.api.commands.annotations.permissions.Permission;
import net.impactdev.impactor.api.commands.annotations.permissions.Phase;
import net.impactdev.impactor.api.commands.executors.CommandContext;
import net.impactdev.impactor.api.commands.executors.CommandResult;
import net.impactdev.impactor.api.commands.executors.CommandSource;
import net.impactdev.impactor.api.utilities.context.Context;
import org.jetbrains.annotations.NotNull;

@Alias("gts")
@Permission(value = GTSPermissions.GTS_COMMAND_BASE, phase = Phase.EXECUTION)
public final class SafeModeExecutor implements ImpactorCommand {

    @Override
    public @NotNull CommandResult execute(CommandContext context) {
        CommandSource source = context.source();
        Messages.SAFE_MODE.send(source.asPlatform(), Context.empty());
        return CommandResult.successful();
    }

}
