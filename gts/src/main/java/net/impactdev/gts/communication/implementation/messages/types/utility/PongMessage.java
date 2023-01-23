package net.impactdev.gts.communication.implementation.messages.types.utility;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.impactdev.gts.communication.api.message.errors.ErrorCode;
import net.impactdev.gts.communication.api.message.errors.ErrorCodes;
import net.impactdev.gts.communication.implementation.messages.MessageDecoder;
import net.impactdev.gts.communication.implementation.messages.requests.AbstractResponseMessage;
import net.impactdev.gts.communication.implementation.messages.requests.RequestInfo;
import net.impactdev.gts.util.GTSKeys;
import net.impactdev.json.JObject;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

import static net.impactdev.gts.util.JsonUtilities.optional;
import static net.impactdev.gts.util.JsonUtilities.require;

public final class PongMessage extends AbstractResponseMessage {

    public static final Key KEY = GTSKeys.gts("pong");
    public static final MessageDecoder<PongMessage> DECODER = (id, content) -> {
        Instant timestamp = Instant.parse(require(content, "timestamp", JsonElement::getAsString));
        JsonObject request = content.getAsJsonObject("request");
        boolean successful = require(content, "successful", JsonElement::getAsBoolean);
        ErrorCode error = optional(content, "error", element -> ErrorCodes.get(element.getAsInt()));

        return new PongMessage(
                id,
                timestamp,
                new RequestInfo(
                        require(request, "id", element -> UUID.fromString(element.getAsString())),
                        require(request, "timestamp", element -> Instant.parse(element.getAsString()))
                ),
                successful,
                error
        );
    };

    public PongMessage(UUID id, RequestInfo request, boolean successful, @Nullable ErrorCode error) {
        super(id, request, successful, error);
    }

    private PongMessage(UUID id, Instant timestamp, RequestInfo request, boolean successful, @Nullable ErrorCode error) {
        super(id, timestamp, request, successful, error);
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    @Override
    public @NotNull String encoded() {
        return this.encode(
                KEY,
                new JObject()
                        .add("id", this.id().toString())
                        .add("timestamp", this.timestamp().toString())
                        .add("request", new JObject()
                                .add("id", this.request().id().toString())
                                .add("timestamp", this.request().timestamp().toString())
                        )
                        .add("successful", this.successful())
                        .consume(o -> this.error().ifPresent(error -> o.add("error", error.ordinal())))
        );
    }

}
