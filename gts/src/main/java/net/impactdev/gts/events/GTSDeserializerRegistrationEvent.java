package net.impactdev.gts.events;

import net.impactdev.gts.api.events.lifecycle.DeserializerRegistrationEvent;
import net.impactdev.gts.api.registries.components.ComponentDeserializerRegistry;
import net.impactdev.gts.api.storage.StorableContent;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public class GTSDeserializerRegistrationEvent implements DeserializerRegistrationEvent {

    private final ComponentDeserializerRegistry registry;

    public GTSDeserializerRegistrationEvent(ComponentDeserializerRegistry registry) {
        this.registry = registry;
    }

    @Override
    public <T extends StorableContent> DeserializerRegistrationEvent register(@NotNull Key key, StorableContent.@NotNull Deserializer<T> deserializer) {
        this.registry.register(key, deserializer);
        return this;
    }
}
