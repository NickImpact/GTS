package me.nickimpact.gts.bungee.messaging.types;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.RequiredArgsConstructor;
import me.nickimpact.gts.api.messaging.IncomingMessageConsumer;
import me.nickimpact.gts.api.messaging.Messenger;
import me.nickimpact.gts.api.messaging.message.OutgoingMessage;
import me.nickimpact.gts.bungee.GTSBungeePlugin;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
public class PluginMessageMessenger implements Messenger, Listener {

	private static final String CHANNEL = "gts:update";

	private final GTSBungeePlugin plugin;
	private final IncomingMessageConsumer consumer;

	public void init() {
		ProxyServer proxy = this.plugin.getBootstrap().getProxy();
		proxy.getPluginManager().registerListener(this.plugin.getBootstrap(), this);
		proxy.registerChannel(CHANNEL);
	}

	@Override
	public void close() {
		ProxyServer proxy = this.plugin.getBootstrap().getProxy();
		proxy.unregisterChannel(CHANNEL);
		proxy.getPluginManager().unregisterListener(this);
	}

	private void dispatch(byte[] message) {
		this.plugin.getBootstrap().getProxy().getServers().values().stream()
				.filter(server -> {
					AtomicBoolean hasPlayers = new AtomicBoolean(false);
					Callback<ServerPing> callback = (ping, exception) -> {
						if(exception == null) {
							hasPlayers.set(ping.getPlayers().getOnline() > 0);
						}
					};
					server.ping(callback);
					return hasPlayers.get();
				})
				.forEach(server -> server.sendData(CHANNEL, message, true));
	}

	@Override
	public IncomingMessageConsumer getMessageConsumer() {
		return this.consumer;
	}

	@Override
	public void sendOutgoingMessage(@NonNull OutgoingMessage outgoingMessage) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF(outgoingMessage.asEncodedString());

		byte[] message = out.toByteArray();
		this.dispatch(message);
	}

	@EventHandler
	public void onPluginMessage(PluginMessageEvent event) {
		if(!event.getTag().equals(CHANNEL)) {
			return;
		}

		event.setCancelled(true);
		if(event.getSender() instanceof ProxiedPlayer) {
			return;
		}

		byte[] data = event.getData();
		ByteArrayDataInput in = ByteStreams.newDataInput(data);
		String msg = in.readUTF();

		this.consumer.consumeIncomingMessageAsString(msg);
	}

}
