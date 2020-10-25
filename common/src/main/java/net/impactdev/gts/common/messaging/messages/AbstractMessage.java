package net.impactdev.gts.common.messaging.messages;

import net.impactdev.gts.api.messaging.message.OutgoingMessage;
import net.impactdev.gts.api.messaging.message.type.MessageType;
import net.impactdev.gts.api.messaging.message.type.UpdateMessage;
import net.impactdev.gts.api.util.PrettyPrinter;
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

}
