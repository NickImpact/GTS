package net.impactdev.gts.communication.implementation.messages.requests;

import java.time.Instant;
import java.util.UUID;

public final class RequestInfo {

    private final UUID id;
    private final Instant timestamp;

    public RequestInfo(UUID uuid, Instant timestamp) {
        this.id = uuid;
        this.timestamp = timestamp;
    }

    public UUID id() {
        return this.id;
    }

    public Instant timestamp() {
        return this.timestamp;
    }

}
