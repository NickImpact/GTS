package net.impactdev.gts.commands;

import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import net.impactdev.gts.permissions.GTSPermissions;

import net.impactdev.gts.plugin.GTSPlugin;
import net.impactdev.gts.ui.views.SimpleViews;
import net.impactdev.impactor.api.commands.CommandSource;
import net.impactdev.impactor.api.platform.sources.PlatformPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class GTSCommands {

    @CommandMethod("gts")
    @CommandPermission(GTSPermissions.GTS_COMMAND_BASE)
    public void root(CommandSource source) {
        if(GTSPlugin.instance().inSafeMode()) {
//            Texts.SAFE_MODE.send(source.source(), Context.empty());
        } else {
            if(!(source.source() instanceof PlatformPlayer)) {
                source.sendMessage(Component.text("You must be a player to use this command!").color(NamedTextColor.RED));
                return;
            }

            PlatformPlayer player = source.player();
            SimpleViews.MAIN_MENU.provide(player).open(player);
        }
    }

}
