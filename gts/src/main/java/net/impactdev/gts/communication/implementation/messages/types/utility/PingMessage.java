package net.impactdev.gts.communication.implementation.messages.types.utility;

import net.impactdev.gts.communication.implementation.messages.Message;
import net.impactdev.gts.communication.implementation.messages.MessageDecoder;
import net.impactdev.gts.communication.implementation.messages.requests.AbstractRequestMessage;
import net.impactdev.gts.communication.implementation.messages.requests.RequestInfo;
import net.impactdev.gts.util.GTSKeys;
import net.impactdev.gts.util.future.Futures;
import net.impactdev.impactor.api.utility.printing.PrettyPrinter;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class PingMessage extends AbstractRequestMessage<PongMessage> implements Message {

    public static final Key KEY = GTSKeys.gts("ping");
    public static final MessageDecoder<PingMessage> DECODER = (id, content) -> new PingMessage(id);

    public PingMessage(UUID id) {
        super(id);
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    @Override
    public @NotNull String encoded() {
        return this.encode(KEY, null);
    }

    @Override
    public void print(PrettyPrinter printer) {
        printer.add("ID: %s", this.id());
    }

    @Override
    public CompletableFuture<PongMessage> respond() {
        return Futures.execute(() -> new PongMessage(
                UUID.randomUUID(),
                new RequestInfo(this.id(), this.timestamp()),
                true,
                null
        ));
    }
}
