package net.impactdev.gts.commands.admin;

import net.impactdev.gts.permissions.GTSPermissions;
import net.impactdev.gts.plugin.GTSPlugin;
import net.impactdev.impactor.api.commands.ImpactorCommand;
import net.impactdev.impactor.api.commands.annotations.Alias;
import net.impactdev.impactor.api.commands.annotations.CommandPath;
import net.impactdev.impactor.api.commands.annotations.permissions.Permission;
import net.impactdev.impactor.api.commands.executors.CommandContext;
import net.impactdev.impactor.api.commands.executors.CommandResult;
import org.jetbrains.annotations.NotNull;

@CommandPath("gts admin translations")
@Alias("reload")
@Permission(GTSPermissions.GTS_ADMIN_TRANSLATIONS)
public final class ReloadTranslationsCommand implements ImpactorCommand {

    @Override
    public @NotNull CommandResult execute(CommandContext context) {
        GTSPlugin.instance().translations().reload();
        return CommandResult.successful();
    }

}
