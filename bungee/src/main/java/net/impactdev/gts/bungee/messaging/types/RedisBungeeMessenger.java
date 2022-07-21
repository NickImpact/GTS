package net.impactdev.gts.bungee.messaging.types;

import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import com.imaginarycode.minecraft.redisbungee.events.PubSubMessageEvent;
import net.impactdev.gts.api.messaging.IncomingMessageConsumer;
import net.impactdev.gts.api.messaging.Messenger;
import net.impactdev.gts.api.messaging.message.OutgoingMessage;
import net.impactdev.gts.bungee.GTSBungeePlugin;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.checkerframework.checker.nullness.qual.NonNull;

public class RedisBungeeMessenger implements Messenger, Listener {

	private static final String CHANNEL = "gts:update";

	private final GTSBungeePlugin plugin;
	private final IncomingMessageConsumer consumer;
	private RedisBungeeAPI redisBungee;

	public RedisBungeeMessenger(GTSBungeePlugin plugin, IncomingMessageConsumer consumer) {
		this.plugin = plugin;
		this.consumer = consumer;
	}

	public void init() {
		this.redisBungee = RedisBungee.getApi();
		this.redisBungee.registerPubSubChannels(CHANNEL);

		this.plugin.bootstrap().proxy().getProxy().getPluginManager().registerListener(this.plugin.bootstrap().proxy(), this);
	}

	@Override
	public void close() {
		this.redisBungee.unregisterPubSubChannels(CHANNEL);
		this.redisBungee = null;

		this.plugin.bootstrap().proxy().getProxy().getPluginManager().unregisterListener(this);
	}

	@Override
	public IncomingMessageConsumer getMessageConsumer() {
		return this.consumer;
	}

	@Override
	public void sendOutgoingMessage(@NonNull OutgoingMessage outgoingMessage) {
		this.redisBungee.sendChannelMessage(CHANNEL, outgoingMessage.asEncodedString());
	}

	@EventHandler
	public void onMessage(PubSubMessageEvent event) {
		if(!event.getChannel().equals(CHANNEL)) {
			return;
		}

		this.consumer.consumeIncomingMessageAsString(event.getMessage());
	}
}
