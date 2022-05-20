package net.impactdev.gts.messaging.processor;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.impactdev.gts.api.communication.IncomingMessageConsumer;
import net.impactdev.gts.api.communication.message.Message;
import net.impactdev.gts.api.communication.message.MessageConsumer;
import net.impactdev.gts.api.communication.message.type.MessageType;
import net.impactdev.gts.api.communication.message.type.UpdateMessage;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.messaging.GTSMessagingService;
import net.impactdev.gts.common.plugin.GTSPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class SpongeIncomingMessageConsumer implements IncomingMessageConsumer {

	private final GTSPlugin plugin;
	private final Set<UUID> receivedMessages;

	private final Map<Class<?>, MessageConsumer<?>> consumers = Maps.newHashMap();
	private final Map<UUID, Consumer<?>> requests = Maps.newHashMap();

	public SpongeIncomingMessageConsumer(GTSPlugin plugin) {
		this.plugin = plugin;
		this.receivedMessages = Collections.synchronizedSet(new HashSet<>());
	}

	@Override
	public <T extends MessageType.Response> void registerRequest(UUID request, Consumer<T> response) {
		this.requests.put(request, response);
	}

	@Override
	public <T extends MessageType.Response> void processRequest(UUID request, T response) {
		if(this.requests.get(request) == null) {
			return;
		}

		((Consumer<T>) this.requests.get(request)).accept(response);
		this.requests.remove(request);
	}

	@Override
	public void cacheReceivedID(UUID id) {
		this.receivedMessages.add(id);
	}

	@Override
	public boolean consumeIncomingMessage(@NonNull Message message) {
		Objects.requireNonNull(message, "message");

		if (!this.receivedMessages.add(message.getID())) {
			if(this.plugin.configuration().main().get(ConfigKeys.USE_MULTI_SERVER)) {
				return false;
			}
		}

		this.processIncomingMessage(message);
		return true;
	}

	@Override
	public boolean consumeIncomingMessageAsString(@NonNull String encodedString) {
		Objects.requireNonNull(encodedString, "encodedString");
		JsonObject decodedObject = GTSMessagingService.NORMAL.fromJson(encodedString, JsonObject.class).getAsJsonObject();

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
		Message decoded = GTSPlugin.instance().messagingService().getDecoder(type).apply(content, id);
		if(decoded == null) {
			GTSPlugin.instance().logger().info("No decoder found for incoming message");
			return false;
		}

		// consume the message
		this.processIncomingMessage(decoded);
		return true;
	}

	@Override
	public <T extends Message, V extends T> void registerInternalConsumer(Class<T> parent, MessageConsumer<V> consumer) {
		this.consumers.put(parent, consumer);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public MessageConsumer getInternalConsumer(Class<?> parent) {
		return this.consumers.get(parent);
	}

	private void processIncomingMessage(Message message) {
		if (message instanceof UpdateMessage) {
			UpdateMessage msg = (UpdateMessage) message;

			GTSPlugin.instance().logger().info("Received message with ID: " + message.getID());
			Optional.ofNullable(this.getInternalConsumer(msg.getClass()))
					.orElseThrow(() -> new IllegalStateException("No consumer available for " + msg.getClass().getName()))
					.consume(message);
		} else {
			throw new IllegalArgumentException("Unknown message type: " + message.getClass().getName());
		}
	}

}
