package net.impactdev.gts.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.impactdev.gts.permissions.GTSPermissions;
import net.impactdev.gts.ui.views.MainMenu;
import net.impactdev.impactor.api.commands.ImpactorCommand;
import net.impactdev.impactor.api.commands.annotations.Alias;
import net.impactdev.impactor.api.commands.annotations.RestrictedExecutor;
import net.impactdev.impactor.api.commands.annotations.permissions.Permission;
import net.impactdev.impactor.api.commands.annotations.permissions.Phase;
import net.impactdev.impactor.api.commands.executors.CommandContext;
import net.impactdev.impactor.api.commands.executors.CommandResult;
import net.impactdev.impactor.api.platform.players.PlatformPlayer;
import net.impactdev.impactor.api.utilities.context.Context;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

@Alias("gts")
@Permission(value = GTSPermissions.GTS_COMMAND_BASE, phase = Phase.EXECUTION)
@RestrictedExecutor(system = false)
public final class GTSCommand implements ImpactorCommand {

    @Override
    public @NotNull CommandResult execute(CommandContext context) throws CommandSyntaxException {
        PlatformPlayer platform = context.source().requirePlayer();
        new MainMenu(platform).open(platform);

        return CommandResult.successful();
    }

}
