package net.impactdev.gts.api.communication.message;

@FunctionalInterface
public interface MessageConsumer<V extends Message> {

	void consume(V message);

}
