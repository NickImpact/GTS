package net.impactdev.gts.api.players;

import com.google.gson.JsonObject;
import net.impactdev.gts.api.storage.DataWritable;
import net.impactdev.impactor.api.json.factory.JObject;

import java.util.Map;

public record PlayerPreferences(Map<NotificationType, Boolean> settings) implements DataWritable {

    public boolean notification(NotificationType type) {
        return this.settings.get(type);
    }

    public void set(NotificationType type, boolean state) {
        this.settings.put(type, state);
    }

    @Override
    public int version() {
        return 1;
    }

    @Override
    public JsonObject serialize() {
        return new JObject()
                .consume(o -> {
                    for(Map.Entry<NotificationType, Boolean> entry : this.settings.entrySet()) {
                        o.add(entry.getKey().name(), entry.getValue());
                    }
                })
                .toJson();
    }



}
