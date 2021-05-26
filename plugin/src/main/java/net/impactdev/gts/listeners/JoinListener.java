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
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;

import java.util.concurrent.CompletableFuture;

public class JoinListener {

    @Listener
    public void onAdminJoin(ClientConnectionEvent.Join event, @First Player player) {
        final MessageService<Text> parser = Impactor.getInstance().getRegistry().get(MessageService.class);

        if(player.hasPermission(GTSPermissions.ADMIN_BASE)) {
            PluginMetadata meta = GTSPlugin.getInstance().getMetadata();
            final Version current = new Version(meta.getVersion());
            CompletableFuture<Version> ore = OreVersionChecker.query();
            ore.thenAccept(response -> {
                if(current.compareTo(response) < 0) {
                    String before = Utilities.readMessageConfigOption(MsgConfigKeys.UPDATE_AVAILABLE);
                    String after = before.replace("{{new_version}}", response.toString());
                    player.sendMessage(parser.parse(after));
                } else if(current.isSnapshot()) {
                    player.sendMessage(parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UPDATE_SNAPSHOT)));
                } else {
                    player.sendMessage(parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UPDATE_LATEST)));
                }
            });
        }

    }
    
    @Listener
    public void onPlayerLogin(ClientConnectionEvent.Login event) {
        Sponge.getScheduler().createTaskBuilder().execute(() -> {
        final MessageService<Text> parser = Impactor.getInstance().getRegistry().get(MessageService.class);
            final Optional<Player> player = Sponge.getServer().getPlayer(event.getTargetUser().getUniqueId());
            GTSPlugin.getInstance().getStorage().getStash( player.get().getUniqueId() ).thenAccept(
                stash -> {
                    if (!stash.isEmpty()) {
                        player.get().sendMessage( parser.parse( Utilities.readMessageConfigOption( MsgConfigKeys.STASH_COLLECT_JOIN_MESSAGE ) ) );
                        player.get().playSound( SoundTypes.BLOCK_CHEST_OPEN, player.get().getPosition(), 1 );
                    }
                });
        }).delay( 4, TimeUnit.SECONDS).submit(GTSPlugin.getInstance().getBootstrap());
    }

}
