package net.impactdev.gts.communication.implementation.messages.requests;

import net.impactdev.gts.communication.implementation.messages.types.AbstractMessage;
import net.impactdev.gts.communication.implementation.messages.types.MessageType;
import net.impactdev.impactor.api.utility.printing.PrettyPrinter;

import java.util.UUID;

public abstract class AbstractRequestMessage<T extends MessageType.Response>
        extends AbstractMessage
        implements MessageType.Request<T>
{
    public AbstractRequestMessage(UUID id) {
        super(id);
    }

    @Override
    public void print(PrettyPrinter printer) {
        printer.kv("Message ID", this.id());
        printer.kv("Timestamp", this.timestamp());
    }
}
