package me.nickimpact.gts.api.messaging.message.type;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface MessageType {

	/**
	 * Represents a message that is intended to be received only on the proxy server. This message will
	 * then attempt to process and construct a Response message.
	 */
	interface Request<T extends Response> extends MessageType {

		/**
		 * When this message is detected, this message will be processed by the proxy and then dispatched
		 * to the listening servers. This task is to be executed asynchronously as these tasks will likely
		 * involve polling the central database for information.
		 *
		 * @return A {@link CompletableFuture} that will process the message and craft a response that
		 * will be distributed across the servers.
		 */
		CompletableFuture<T> respond();

	}

	/**
	 * Represents a message built in regards to an incoming proxy message. These messages are intended
	 * to only be sent to the connected servers, and not the proxy itself. In the case of a multi-layered
	 * proxy, these messages should simply be relayed rather than processed.
	 */
	interface Response extends MessageType {

		/**
		 * This call specifies the ID of the message that sent out the request that is now being responded to.
		 * We can use this to ensure we received a message back from the system manager for the specific message,
		 * rather than attempt to guess on which message was received. Also helps determine timeout procedures as
		 * we can now directly map an incoming response to an outgoing request.
		 *
		 * @return The ID of the request message that generated this response
		 */
		UUID getRequestID();

	}

}
