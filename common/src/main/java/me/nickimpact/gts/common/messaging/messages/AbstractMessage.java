package me.nickimpact.gts.common.messaging.messages;

import me.nickimpact.gts.api.messaging.message.OutgoingMessage;
import me.nickimpact.gts.api.messaging.message.type.UpdateMessage;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

public abstract class AbstractMessage implements UpdateMessage, OutgoingMessage {

	private final UUID id;

	public AbstractMessage(UUID id) {
		this.id = id;
	}

	@Override
	public @NonNull UUID getID() {
		return this.id;
	}

	public static abstract class Response extends AbstractMessage {

		private final UUID request;

		public Response(UUID id, UUID request) {
			super(id);
			this.request = request;
		}

		public UUID getRequestID() {
			return this.request;
		}
	}

}
