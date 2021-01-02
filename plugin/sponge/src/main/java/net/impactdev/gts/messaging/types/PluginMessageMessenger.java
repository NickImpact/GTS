package net.impactdev.gts.messaging.types;

import com.google.common.collect.Iterables;
import net.impactdev.gts.GTSSpongePlugin;
import net.impactdev.gts.api.messaging.IncomingMessageConsumer;
import net.impactdev.gts.api.messaging.Messenger;
import net.impactdev.gts.api.messaging.message.OutgoingMessage;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.RawDataListener;
import org.spongepowered.api.network.RemoteConnection;

import java.util.Collection;

/**
 * An implementation of {@link Messenger} using the plugin messaging channels.
 */
public class PluginMessageMessenger implements Messenger, RawDataListener {
	private static final String CHANNEL = "gts:update";

	private final GTSSpongePlugin plugin;
	private final IncomingMessageConsumer consumer;

	private ChannelBinding.RawDataChannel channel = null;

	public PluginMessageMessenger(GTSSpongePlugin plugin, IncomingMessageConsumer consumer) {
		this.plugin = plugin;
		this.consumer = consumer;
	}

	public void init() {
		this.channel = Sponge.getChannelRegistrar().createRawChannel(this.plugin.getBootstrap(), CHANNEL);
		this.channel.addListener(Platform.Type.SERVER, this);
	}

	@Override
	public void close() {
		if (this.channel != null) {
			Sponge.getChannelRegistrar().unbindChannel(this.channel);
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

		Collection<Player> players = Sponge.getServer().getOnlinePlayers();
		Player p = Iterables.getFirst(players, null);
		if (p == null) {
			return;
		}

		this.channel.sendTo(p, buf -> buf.writeUTF(outgoingMessage.asEncodedString()));
	}

	@Override
	public void handlePayload(@NonNull ChannelBuf buf, @NonNull RemoteConnection connection, Platform.@NonNull Type type) {
		String msg = buf.readUTF();
		this.consumer.consumeIncomingMessageAsString(msg);
	}

}
