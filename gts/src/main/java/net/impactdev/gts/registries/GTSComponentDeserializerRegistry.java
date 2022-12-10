package net.impactdev.gts.registries;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import net.impactdev.gts.api.registries.components.ComponentDeserializerRegistry;
import net.impactdev.gts.api.storage.serialization.StorableContent;
import net.impactdev.gts.events.GTSDeserializerRegistrationEvent;
import net.impactdev.gts.plugin.GTSPlugin;
import net.impactdev.impactor.api.Impactor;

import java.util.Map;
import java.util.Optional;

public class GTSComponentDeserializerRegistry
        extends LockableRegistry<Class<?>, StorableContent.Deserializer<?>>
        implements ComponentDeserializerRegistry {

    private final Map<Class<?>, StorableContent.Deserializer<?>> registrations = Maps.newHashMap();

    @Override
    public void init() throws Exception {
        Impactor.instance().events().post(new GTSDeserializerRegistrationEvent(this));
        this.lock();
    }

    @Override
    public boolean register$child(Class<?> key, StorableContent.Deserializer<?> value) {
        if(this.registrations.containsKey(key)) {
            GTSPlugin.instance().logger().warn("Attempted to register a deserializer for a key which has already been registered, this registration has been ignored!");
            return false;
        }

        this.registrations.put(key, value);
        return true;
    }

    @Override
    public <T> T deserialize(Class<T> key, JsonObject json) {
        return Optional.ofNullable(this.registrations.get(key))
                .map(deserializer -> (T) deserializer.deserialize(json))
                .orElseThrow(() -> new IllegalArgumentException("Could not locate a deserializer for the following key: " + key.getSimpleName()));
    }
}
