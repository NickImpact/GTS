package net.impactdev.gts.communication.api.message;

import net.impactdev.gts.communication.implementation.messages.Message;

@FunctionalInterface
public interface MessageConsumer<V extends Message> {

	void consume(V message);

}
