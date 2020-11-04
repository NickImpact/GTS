package net.impactdev.gts.common.messaging.messages.utility;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.impactdev.gts.api.messaging.message.errors.ErrorCode;
import net.impactdev.gts.common.messaging.errors.ErrorCodes;
import net.impactdev.gts.api.messaging.message.type.utility.PingMessage;
import net.impactdev.gts.api.util.PrettyPrinter;
import net.impactdev.gts.common.messaging.GTSMessagingService;
import net.impactdev.gts.common.messaging.messages.AbstractMessage;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.utils.future.CompletableFutureManager;
import net.impactdev.impactor.api.json.factory.JObject;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class PingPongMessage extends AbstractMessage implements PingMessage {

    public PingPongMessage(UUID id) {
        super(id);
    }

    public static class Ping extends PingPongMessage implements PingMessage.Ping {

        public static final String TYPE = "PING";

        public static PingPongMessage.Ping decode(@Nullable JsonElement content, UUID id) {
            return new PingPongMessage.Ping(id);
        }

        public Ping(UUID id) {
            super(id);
        }

        @Override
        public @NonNull String asEncodedString() {
            return GTSMessagingService.encodeMessageAsString(
                    TYPE,
                    this.getID(),
                    new JObject().toJson()
            );
        }

        @Override
        public CompletableFuture<PingMessage.Pong> respond() {
            return CompletableFutureManager.makeFuture(() -> new PingPongMessage.Pong(
                    GTSPlugin.getInstance().getMessagingService().generatePingID(),
                    this.getID(),
                    true,
                    null
            ));
        }

        @Override
        public void print(PrettyPrinter printer) {
            printer.kv("ID", this.getID());
        }
    }

    public static class Pong extends PingPongMessage implements PingMessage.Pong {

        public static final String TYPE = "PONG";

        public static PingPongMessage.Pong decode(@Nullable JsonElement content, UUID id) {
            if(content == null) {
                throw new IllegalStateException("Raw JSON data was null");
            }

            JsonObject raw = content.getAsJsonObject();

            UUID requestID = Optional.ofNullable(raw.get("request"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElseThrow(() -> new IllegalStateException("Unable to locate or parse request ID"));
            boolean successful = Optional.ofNullable(raw.get("successful"))
                    .map(JsonElement::getAsBoolean)
                    .orElseThrow(() -> new IllegalStateException("Unable to locate success state"));
            ErrorCode error = Optional.ofNullable(raw.get("error"))
                    .map(x -> ErrorCodes.get(x.getAsInt()))
                    .orElse(null);

            return new PingPongMessage.Pong(id, requestID, successful, error);
        }

        private final UUID request;
        private long time;
        private boolean successful;
        private ErrorCode error;

        public Pong(UUID id, UUID request, boolean successful, ErrorCode error) {
            super(id);
            this.request = request;
            this.successful = successful;
            this.error = error;
        }

        @Override
        public UUID getRequestID() {
            return this.request;
        }

        @Override
        public long getResponseTime() {
            return this.time;
        }

        @Override
        public void setResponseTime(long millis) {
            this.time = millis;
        }

        @Override
        public boolean wasSuccessful() {
            return this.successful;
        }

        @Override
        public Optional<ErrorCode> getErrorCode() {
            return Optional.ofNullable(this.error);
        }

        @Override
        public @NonNull String asEncodedString() {
            return GTSMessagingService.encodeMessageAsString(
                    TYPE,
                    this.getID(),
                    new JObject()
                            .add("request", this.getRequestID().toString())
                            .add("successful", this.successful)
                            .consume(o -> {
                                if(this.getErrorCode().isPresent()) {
                                    o.add("error", this.error.ordinal());
                                }
                            })
                            .toJson()
            );
        }

        @Override
        public void print(PrettyPrinter printer) {
            printer.kv("ID", this.getID())
                    .kv("Ping ID", this.getRequestID());
        }
    }

}
