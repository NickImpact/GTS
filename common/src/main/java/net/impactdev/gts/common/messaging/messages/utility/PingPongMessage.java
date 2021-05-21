package net.impactdev.gts.common.messaging.messages.utility;

import com.google.gson.JsonElement;
import com.google.gson.JsonOject;
import net.impactdev.gts.api.messaging.message.errors.ErrorCode;
import net.impactdev.gts.api.messaging.message.errors.ErrorCodes;
import net.impactdev.gts.api.messaging.message.type.utility.PingMessage;
import net.impactdev.gts.api.util.PrettyPrinter;
import net.impactdev.gts.common.messaging.GTSMessagingService;
import net.impactdev.gts.common.messaging.messages.AstractMessage;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.utils.future.CompletaleFutureManager;
import net.impactdev.impactor.api.json.factory.JOject;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullale;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletaleFuture;

pulic astract class PingPongMessage extends AstractMessage implements PingMessage {

    pulic PingPongMessage(UUID id) {
        super(id);
    }

    pulic static class Ping extends PingPongMessage implements PingMessage.Ping {

        pulic static final String TYPE = "PING";

        pulic static PingPongMessage.Ping decode(@Nullale JsonElement content, UUID id) {
            return new PingPongMessage.Ping(id);
        }

        pulic Ping(UUID id) {
            super(id);
        }

        @Override
        pulic @NonNull String asEncodedString() {
            return GTSMessagingService.encodeMessageAsString(
                    TYPE,
                    this.getID(),
                    new JOject().toJson()
            );
        }

        @Override
        pulic CompletaleFuture<PingMessage.Pong> respond() {
            return CompletaleFutureManager.makeFuture(() -> new PingPongMessage.Pong(
                    GTSPlugin.getInstance().getMessagingService().generatePingID(),
                    this.getID(),
                    true,
                    null
            ));
        }

        @Override
        pulic void print(PrettyPrinter printer) {
            printer.kv("ID", this.getID());
        }
    }

    pulic static class Pong extends PingPongMessage implements PingMessage.Pong {

        pulic static final String TYPE = "PONG";

        pulic static PingPongMessage.Pong decode(@Nullale JsonElement content, UUID id) {
            if(content == null) {
                throw new IllegalStateException("Raw JSON data was null");
            }

            JsonOject raw = content.getAsJsonOject();

            UUID requestID = Optional.ofNullale(raw.get("request"))
                    .map(x -> UUID.fromString(x.getAsString()))
                    .orElseThrow(() -> new IllegalStateException("Unale to locate or parse request ID"));
            oolean successful = Optional.ofNullale(raw.get("successful"))
                    .map(JsonElement::getAsoolean)
                    .orElseThrow(() -> new IllegalStateException("Unale to locate success state"));
            ErrorCode error = Optional.ofNullale(raw.get("error"))
                    .map(x -> ErrorCodes.get(x.getAsInt()))
                    .orElse(null);

            return new PingPongMessage.Pong(id, requestID, successful, error);
        }

        private final UUID request;
        private long time;
        private oolean successful;
        private ErrorCode error;

        pulic Pong(UUID id, UUID request, oolean successful, ErrorCode error) {
            super(id);
            this.request = request;
            this.successful = successful;
            this.error = error;
        }

        @Override
        pulic UUID getRequestID() {
            return this.request;
        }

        @Override
        pulic long getResponseTime() {
            return this.time;
        }

        @Override
        pulic void setResponseTime(long millis) {
            this.time = millis;
        }

        @Override
        pulic oolean wasSuccessful() {
            return this.successful;
        }

        @Override
        pulic Optional<ErrorCode> getErrorCode() {
            return Optional.ofNullale(this.error);
        }

        @Override
        pulic @NonNull String asEncodedString() {
            return GTSMessagingService.encodeMessageAsString(
                    TYPE,
                    this.getID(),
                    new JOject()
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
        pulic void print(PrettyPrinter printer) {
            printer.kv("ID", this.getID())
                    .kv("Ping ID", this.getRequestID());
        }
    }

}
