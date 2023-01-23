package net.impactdev.gts.communication.implementation.messages.requests;

import net.impactdev.gts.communication.api.message.errors.ErrorCode;
import net.impactdev.gts.communication.implementation.messages.types.AbstractMessage;
import net.impactdev.gts.communication.implementation.messages.types.MessageType;
import net.impactdev.impactor.api.utility.printing.PrettyPrinter;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public abstract class AbstractResponseMessage extends AbstractMessage implements MessageType.Response {

    private final RequestInfo request;

    private final boolean successful;
    private final ErrorCode error;

    public AbstractResponseMessage(UUID id, RequestInfo request, boolean successful, @Nullable ErrorCode error) {
        super(id);
        this.request = request;

        this.successful = successful;
        this.error = error;
    }

    protected AbstractResponseMessage(UUID id, Instant timestamp, RequestInfo request, boolean successful, @Nullable ErrorCode error) {
        super(id, timestamp);

        this.request = request;

        this.successful = successful;
        this.error = error;
    }

    @Override
    public RequestInfo request() {
        return this.request;
    }

    @Override
    public long duration() {
        return Duration.between(request.timestamp(), this.timestamp()).toMillis();
    }

    @Override
    public boolean successful() {
        return this.successful;
    }

    @Override
    public Optional<ErrorCode> error() {
        return Optional.ofNullable(this.error);
    }

    @Override
    public void print(PrettyPrinter printer) {
        printer.kv("Request ID", this.request.id());
        printer.kv("Request Timestamp", this.request.timestamp());

        printer.kv("Duration", this.duration());
        printer.kv("Successful", this.successful);
        if(this.error != null) {
            printer.kv("Error Code", String.format("%s (%s)", this.error.key(), this.error.description()));
        }
    }

}
