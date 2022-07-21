package net.impactdev.gts.velocity.messaging;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.impactdev.gts.api.messaging.IncomingMessageConsumer;
import net.impactdev.gts.api.messaging.Messenger;
import net.impactdev.gts.api.messaging.message.OutgoingMessage;
import net.impactdev.gts.velocity.GTSVelocityPlugin;
import net.impactdev.impactor.api.Impactor;
import org.checkerframework.checker.nullness.qual.NonNull;

public class PluginMessageMessenger implements Messenger {

    private static final ChannelIdentifier CHANNEL = MinecraftChannelIdentifier.create("gts", "update");

    private final GTSVelocityPlugin plugin;
    private final IncomingMessageConsumer consumer;

    public PluginMessageMessenger(GTSVelocityPlugin plugin, IncomingMessageConsumer consumer) {
        this.plugin = plugin;
        this.consumer = consumer;
    }

    @Override
    public IncomingMessageConsumer getMessageConsumer() {
        return this.consumer;
    }

    public void init() {
        ProxyServer proxy = this.plugin.bootstrap().getProxy();
        proxy.getChannelRegistrar().register(CHANNEL);
        proxy.getEventManager().register(this.plugin.bootstrap(), this);
    }

    @Override
    public void close() {
        ProxyServer proxy = this.plugin.bootstrap().getProxy();
        proxy.getChannelRegistrar().unregister(CHANNEL);
        proxy.getEventManager().unregisterListener(this.plugin.bootstrap(), this);
    }

    private void dispatch(byte[] message) {
        for(RegisteredServer server : this.plugin.bootstrap().getProxy().getAllServers()) {
            server.sendPluginMessage(CHANNEL, message);
        }
    }

    @Override
    public void sendOutgoingMessage(@NonNull OutgoingMessage outgoingMessage) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(outgoingMessage.asEncodedString());

        byte[] message = out.toByteArray();
        this.dispatch(message);
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if(!event.getIdentifier().getId().equals(CHANNEL.getId())) {
            return;
        }

        event.setResult(PluginMessageEvent.ForwardResult.handled());

        if(event.getSource() instanceof Player) {
            return;
        }

        ByteArrayDataInput in = event.dataAsDataStream();
        String msg = in.readUTF();

        if(this.consumer.consumeIncomingMessageAsString(msg)) {
            Impactor.getInstance().getScheduler().executeAsync(() -> this.dispatch(event.getData()));
        }
    }
}
