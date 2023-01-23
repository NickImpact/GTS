package net.impactdev.gts.communication.implementation.messages;

public interface MessageSubscription<T extends Message> {

    void consume(T message);

}
