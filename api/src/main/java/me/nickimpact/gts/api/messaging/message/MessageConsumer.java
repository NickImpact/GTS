package me.nickimpact.gts.api.messaging.message;

@FunctionalInterface
public interface MessageConsumer {

	void consume(Message message);

}
