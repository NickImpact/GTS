package net.impactdev.gts.api.communication.message.type.utility;

import net.impactdev.gts.api.communication.message.OutgoingMessage;
import net.impactdev.gts.api.communication.message.type.MessageType;
import net.impactdev.gts.api.communication.message.type.UpdateMessage;

/**
 * Represents a message that GTS can use as a means to ping the proxy and vice versa. These messages are namely
 * useful in determining the messaging service is properly connected.
 */
public interface PingMessage extends UpdateMessage, OutgoingMessage {

	/**
	 * This message type simply asks the proxy server if it can see this ping. If it does, it'll respond
	 * to that server with a {@link Pong Pong} message.
	 */
	interface Ping extends PingMessage, MessageType.Request<Pong> {}

	/**
	 * Simply indicates that the proxy has heard the ping sent.
	 */
	interface Pong extends PingMessage, MessageType.Response {

		long getResponseTime();

	}

}
