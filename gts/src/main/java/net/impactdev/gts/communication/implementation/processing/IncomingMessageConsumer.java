package net.impactdev.gts.communication.implementation.processing;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import net.impactdev.gts.communication.implementation.messages.Message;
import net.impactdev.gts.plugin.GTSPlugin;
import net.impactdev.gts.util.JsonUtilities;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static net.impactdev.gts.util.JsonUtilities.require;

public final class IncomingMessageConsumer {

    private final Cache<UUID, UUID> received = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    public boolean consume(final @NotNull Message message) {
        Preconditions.checkNotNull(message, "Message was unexpectedly null");
        if(this.received.asMap().containsKey(message.id())) {
            return false;
        }

        this.received.put(message.id(), message.id());
        return this.process(message);
    }

    public boolean consume(final @NotNull String encoded) {
        Preconditions.checkNotNull(encoded, "Message data was unexpectedly null");
        JsonObject json = JsonUtilities.SIMPLE.fromJson(encoded, JsonObject.class);

        UUID id = require(json, "id", element -> UUID.fromString(element.getAsString()));
        if(this.received.asMap().containsKey(id)) {
            return false;
        }

        Key key = require(json, "key", element -> Key.key(element.getAsString()));
        @Nullable JsonObject content = json.getAsJsonObject("content");

        Message message = GTSPlugin.instance().communication().messages().decoder(key).decode(id, content);
        return this.process(message);
    }

    private boolean process(final @NotNull Message message) {
        Preconditions.checkNotNull(message, "Message was unexpectedly null");
        Key key = message.key();

        GTSPlugin.instance().logger().debug("Received message with ID: " + message.id() + " (" + key.asString() + ")");
        GTSPlugin.instance().communication()
                .messages()
                .subscription(key)
                .ifPresent(subscription -> subscription.consume(message));

        return true;
    }
}
