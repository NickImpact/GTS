package net.impactdev.gts.api.players;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import net.impactdev.gts.api.storage.serialization.StorableContent;
import net.impactdev.json.JObject;

import java.util.Map;

public class PlayerPreferences implements StorableContent {

    private final Map<NotificationType, Boolean> settings = Maps.newHashMap();

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
