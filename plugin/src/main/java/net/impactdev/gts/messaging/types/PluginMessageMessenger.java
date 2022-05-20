package net.impactdev.gts.messaging.types;

import com.google.common.collect.Iterables;
import net.impactdev.gts.api.communication.IncomingMessageConsumer;
import net.impactdev.gts.api.communication.Messenger;
import net.impactdev.gts.api.communication.message.OutgoingMessage;
import net.impactdev.gts.common.plugin.GTSPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.network.EngineConnectionSide;
import org.spongepowered.api.network.ServerSideConnection;
import org.spongepowered.api.network.channel.ChannelBuf;
import org.spongepowered.api.network.channel.raw.RawDataChannel;
import org.spongepowered.api.network.channel.raw.play.RawPlayDataHandler;

import java.util.Collection;

/**
 * An implementation of {@link Messenger} using the plugin messaging channels.
 */
public class PluginMessageMessenger implements Messenger, RawPlayDataHandler<ServerSideConnection> {

	private static final ResourceKey CHANNEL = ResourceKey.builder()
			.namespace("gts")
			.value("update")
			.build();

	private final GTSPlugin plugin;
	private final IncomingMessageConsumer consumer;

	private RawDataChannel channel = null;

	public PluginMessageMessenger(GTSPlugin plugin, IncomingMessageConsumer consumer) {
		this.plugin = plugin;
		this.consumer = consumer;
	}

	public void init() {
		this.channel = Sponge.channelManager().ofType(CHANNEL, RawDataChannel.class);
		this.channel.play().addHandler(EngineConnectionSide.SERVER, this);
	}

	@Override
	public void close() {
		if (this.channel != null) {
			this.channel.play().removeHandler(this);
		}
	}

	@Override
	public IncomingMessageConsumer getMessageConsumer() {
		return this.consumer;
	}

	@Override
	public void sendOutgoingMessage(@NonNull OutgoingMessage outgoingMessage) {
		if (!Sponge.isServerAvailable()) {
			return;
		}

		Collection<ServerPlayer> players = Sponge.server().onlinePlayers();
		ServerPlayer p = Iterables.getFirst(players, null);
		if (p == null) {
			return;
		}

		this.channel.play().sendTo(p, buf -> buf.writeUTF(outgoingMessage.asEncodedString()));
	}

	@Override
	public void handlePayload(@NonNull ChannelBuf buf, ServerSideConnection connection) {
		String msg = buf.readUTF();
		this.consumer.consumeIncomingMessageAsString(msg);
	}

}
