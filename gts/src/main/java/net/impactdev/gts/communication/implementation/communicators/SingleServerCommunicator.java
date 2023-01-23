package net.impactdev.gts.communication.implementation.communicators;

import net.impactdev.gts.communication.implementation.messages.Message;
import net.impactdev.gts.communication.implementation.processing.IncomingMessageConsumer;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class SingleServerCommunicator extends Communicator {

    public SingleServerCommunicator(IncomingMessageConsumer consumer) {
        super(consumer);
    }

    @Override
    public void publish(@NonNull Message message) {
        this.consumer().consume(message);
    }
}
