package net.impactdev.gts.communication.implementation.messages.types;

import com.google.gson.JsonObject;
import net.impactdev.gts.communication.implementation.messages.Message;
import net.impactdev.gts.util.JsonUtilities;
import net.impactdev.json.JObject;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

public abstract class AbstractMessage implements Message {

    private final UUID id;
    private final Instant timestamp;

    public AbstractMessage(UUID id) {
        this.id = id;
        this.timestamp = Instant.now();
    }

    protected AbstractMessage(UUID id, Instant timestamp) {
        this.id = id;
        this.timestamp = timestamp;
    }

    @Override
    public @NotNull UUID id() {
        return this.id;
    }

    @Override
    public Instant timestamp() {
        return this.timestamp;
    }

    public String encode(Key key, @Nullable JObject content) {
        JsonObject json = new JObject()
                .add("id", this.id.toString())
                .add("key", key.asString())
                .consume(o -> {
                    if(content != null) {
                        o.add("content", content);
                    }
                })
                .toJson();

        return JsonUtilities.SIMPLE.toJson(json);
    }
}
