package net.impactdev.gts.commands.admin;

import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import net.impactdev.gts.locale.TranslationManager;
import net.impactdev.gts.permissions.GTSPermissions;
import net.impactdev.gts.plugin.GTSPlugin;
import net.impactdev.impactor.api.commands.CommandSource;

import java.util.Locale;

@CommandPermission(GTSPermissions.GTS_ADMIN_TRANSLATIONS)
public final class TranslationCommands {

    @CommandMethod("gts admin translations")
    public void root(CommandSource source) {
        TranslationManager manager = GTSPlugin.instance().translations();
        for(Locale language : manager.getInstalledLocales()) {

        }
    }

    @CommandMethod("gts admin translations install")
    public void translations$install(CommandSource source) {

    }

    @CommandMethod("gts admin translations reload")
    public void translations$reload(CommandSource source) {
        GTSPlugin.instance().translations().reload();
    }

}
