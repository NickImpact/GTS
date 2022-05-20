package net.impactdev.gts.messaging.types;

import net.impactdev.gts.api.communication.IncomingMessageConsumer;
import net.impactdev.gts.api.communication.Messenger;
import net.impactdev.gts.api.communication.message.OutgoingMessage;
import org.checkerframework.checker.nullness.qual.NonNull;

public class SpongeSingleServerModeMessenger implements Messenger {

    private final IncomingMessageConsumer consumer;

    public SpongeSingleServerModeMessenger(IncomingMessageConsumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public IncomingMessageConsumer getMessageConsumer() {
        return this.consumer;
    }

    @Override
    public void sendOutgoingMessage(@NonNull OutgoingMessage outgoingMessage) {
        this.consumer.consumeIncomingMessage(outgoingMessage);
    }

}
