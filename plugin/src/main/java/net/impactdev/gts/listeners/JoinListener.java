package net.impactdev.gts.listeners;

import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.plugin.permissions.GTSPermissions;
import net.impactdev.gts.common.utils.Version;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.gts.util.OreVersionChecker;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.plugin.PluginMetadata;
import net.impactdev.impactor.api.services.text.MessageService;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.plugin.PluginContainer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class JoinListener {

    private final PluginContainer contaienr;

    public JoinListener(PluginContainer container) {
        this.contaienr = container;
    }

    @Listener
    public void onAdminJoin(ServerSideConnectionEvent.Join event, @First ServerPlayer player) {
        final MessageService parser = Impactor.getInstance().getRegistry().get(MessageService.class);

        if (player.hasPermission(GTSPermissions.ADMIN_BASE)) {
            PluginMetadata meta = GTSPlugin.instance().metadata();
            final Version current = new Version(meta.version());
            CompletableFuture<Version> ore = OreVersionChecker.query();
            ore.thenAccept(response -> {
                if (current.compareTo(response) < 0) {
                    String before = Utilities.readMessageConfigOption(MsgConfigKeys.UPDATE_AVAILABLE);
                    String after = before.replace("{{new_version}}", response.toString());
                    player.sendMessage(parser.parse(after));
                } else if (current.isSnapshot()) {
                    player.sendMessage(parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UPDATE_SNAPSHOT)));
                } else {
                    player.sendMessage(parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UPDATE_LATEST)));
                }
            });
        }

    }

    @Listener
    public void onPlayerLogin(ServerSideConnectionEvent.Login event, @First ServerPlayer player) {
        Task task = Task.builder()
                .delay(4, TimeUnit.SECONDS)
                .plugin(this.contaienr)
                .execute(() -> {
                    final MessageService parser = Impactor.getInstance().getRegistry().get(MessageService.class);
                    GTSPlugin.instance().storage().getStash(player.uniqueId()).thenAccept(
                            stash -> {
                                if (!stash.isEmpty()) {
                                    player.sendMessage(parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.STASH_COLLECT_JOIN_MESSAGE)));
                                    player.playSound(Sound.sound(
                                            SoundTypes.BLOCK_CHEST_OPEN.get().key(),
                                            Sound.Source.MASTER,
                                            1,
                                            1
                                    ), player.position());
                                }
                            });
                })
                .build();

        Sponge.server().scheduler().submit(task);
    }

}
