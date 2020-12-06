package net.impactdev.gts.api.messaging.message;

@FunctionalInterface
public interface MessageConsumer<V extends Message> {

	void consume(V message);

}
