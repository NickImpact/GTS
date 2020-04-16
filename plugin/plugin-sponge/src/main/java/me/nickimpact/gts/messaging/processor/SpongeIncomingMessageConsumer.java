package me.nickimpact.gts.messaging.processor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.nickimpact.gts.GTSSpongePlugin;
import me.nickimpact.gts.api.messaging.IncomingMessageConsumer;
import me.nickimpact.gts.api.messaging.message.Message;
import me.nickimpact.gts.api.messaging.message.type.utility.PingMessage;
import me.nickimpact.gts.api.messaging.message.type.UpdateMessage;
import me.nickimpact.gts.common.messaging.messages.listings.auctions.impl.BidMessage;
import me.nickimpact.gts.common.messaging.messages.listings.auctions.impl.BidResponseMessage;
import me.nickimpact.gts.common.messaging.messages.testing.TestMessage;
import me.nickimpact.gts.common.messaging.messages.utility.GTSPongMessage;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static me.nickimpact.gts.common.messaging.GTSMessagingService.NORMAL;

public class SpongeIncomingMessageConsumer implements IncomingMessageConsumer {

	private final GTSSpongePlugin plugin;
	private final Set<UUID> receivedMessages;

	public SpongeIncomingMessageConsumer(GTSSpongePlugin plugin) {
		this.plugin = plugin;
		this.receivedMessages = Collections.synchronizedSet(new HashSet<>());
	}

	@Override
	public void cacheReceivedID(UUID id) {
		this.receivedMessages.add(id);
	}

	@Override
	public boolean consumeIncomingMessage(@NonNull Message message) {
		Objects.requireNonNull(message, "message");

		if (!this.receivedMessages.add(message.getID())) {
			return false;
		}

		this.processIncomingMessage(message);
		return true;
	}

	@Override
	public boolean consumeIncomingMessageAsString(@NonNull String encodedString) {
		Objects.requireNonNull(encodedString, "encodedString");
		JsonObject decodedObject = NORMAL.fromJson(encodedString, JsonObject.class).getAsJsonObject();

		// extract id
		JsonElement idElement = decodedObject.get("id");
		if (idElement == null) {
			throw new IllegalStateException("Incoming message has no id argument: " + encodedString);
		}
		UUID id = UUID.fromString(idElement.getAsString());

		// ensure the message hasn't been received already
		if (!this.receivedMessages.add(id)) {
			return false;
		}

		// extract type
		JsonElement typeElement = decodedObject.get("type");
		if (typeElement == null) {
			throw new IllegalStateException("Incoming message has no type argument: " + encodedString);
		}
		String type = typeElement.getAsString();

		// extract content
		@Nullable JsonElement content = decodedObject.get("content");

		// decode message
		Message decoded;
		if(type.equals(BidMessage.TYPE)) {
			decoded = BidMessage.decode(content, id);
		} else if(type.equals(BidResponseMessage.TYPE)) {
			decoded = BidResponseMessage.decode(content, id);
		} else if(type.equals(GTSPongMessage.TYPE)) {
			decoded = GTSPongMessage.decode(content, id);
		} else {// gracefully return if we just don't recognise the type
			return false;
		}

		// consume the message
		processIncomingMessage(decoded);
		return true;
	}

	private void processIncomingMessage(Message message) {
		if (message instanceof UpdateMessage) {
			UpdateMessage msg = (UpdateMessage) message;
			this.plugin.getPluginLogger().info("[Messaging] Received message with id: " + msg.getID());
			if(msg instanceof PingMessage.Pong) {
				PingMessage.Pong pong = (PingMessage.Pong) msg;
				InetSocketAddress address = Sponge.getServer().getBoundAddress().get();
				if(address.getHostName().equals(pong.getServerAddress()) && address.getPort() == pong.getServerPort()) {
					this.plugin.getPluginLogger().info("[Messaging] Request has returned a pong message successfully");
				}
			}
		} else {
			throw new IllegalArgumentException("Unknown message type: " + message.getClass().getName());
		}
	}
}
