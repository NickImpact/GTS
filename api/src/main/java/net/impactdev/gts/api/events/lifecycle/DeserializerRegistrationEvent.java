package net.impactdev.gts.api.events.lifecycle;

import net.impactdev.gts.api.storage.StorableContent;
import net.impactdev.impactor.api.events.ImpactorEvent;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.util.annotation.eventgen.NoFactoryMethod;

/**
 * Represents the event fired during the GTS launch life-cycle phase where deserializers
 * for components are registered. This would be the case where a listing, such as a
 * {@link net.impactdev.gts.api.elements.listings.models.BuyItNow} listing, would
 * initialize its means of how to transform JSON data into the native object.
 *
 * @since 7.0.0
 */
@NoFactoryMethod
public interface DeserializerRegistrationEvent extends ImpactorEvent {

    /**
     * Associates the given deserializer with the given class. This will be used
     * whenever the given type is requested for deserialization.
     *
     * @param key The key used for deserialization
     * @param deserializer
     * @return
     * @param <T>
     */
    <T extends StorableContent> DeserializerRegistrationEvent register(
            @NotNull final Key key,
            @NotNull final StorableContent.Deserializer<T> deserializer
    );

}
