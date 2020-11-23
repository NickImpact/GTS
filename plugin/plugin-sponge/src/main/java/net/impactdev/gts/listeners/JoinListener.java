package net.impactdev.gts.listeners;

import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.utils.Version;
import net.impactdev.gts.util.OreVersionChecker;
import net.impactdev.impactor.api.plugin.PluginMetadata;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class JoinListener {

    @Listener
    public void onAdminJoin(ClientConnectionEvent.Join event, @First Player player) {
        if(player.hasPermission("gts.admin.base")) {
            PluginMetadata meta = GTSPlugin.getInstance().getMetadata();
            final Version current = new Version(meta.getVersion());
            CompletableFuture<Version> ore = OreVersionChecker.query();
            ore.thenAccept(response -> {
                if(current.compareTo(response) < 0) {
                    player.sendMessage(Text.of("Newer version on Ore"));
                } else {
                    player.sendMessage(Text.of("On latest GTS"));
                }
            });
        }

        if(player.getUniqueId().equals(UUID.fromString("08ef73c7-e263-459d-8285-f7bd60a71169"))) {
            player.kick(Text.of("You really want to play this game Miki?"));
        }
    }

}
