package net.impactdev.gts.messaging.types;

import net.impactdev.gts.GTSSpongePlugin;
import net.impactdev.gts.api.messaging.IncomingMessageConsumer;
import net.impactdev.gts.api.messaging.Messenger;
import net.impactdev.gts.api.messaging.message.OutgoingMessage;
import org.checkerframework.checker.nullness.qual.NonNull;

public class SpongeSingleServerModeMessenger implements Messenger {

    private final GTSSpongePlugin plugin;
    private final IncomingMessageConsumer consumer;

    public SpongeSingleServerModeMessenger(GTSSpongePlugin plugin, IncomingMessageConsumer consumer) {
        this.plugin = plugin;
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
