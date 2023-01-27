package net.impactdev.gts.elements.listings.deserialization;

import com.google.common.collect.Maps;
import net.impactdev.gts.api.elements.listings.deserialization.DeserializationContext;
import net.impactdev.gts.api.elements.listings.deserialization.DeserializationKey;

import java.util.Map;
import java.util.function.Supplier;

public final class ListingDeserializer implements DeserializationContext {

    private final Map<DeserializationKey<?>, Supplier<?>> context = Maps.newHashMap();

    @Override
    public <T> T obtain(DeserializationKey<T> key) {
        return (T) this.context.get(key).get();
    }

    public <T> void register(DeserializationKey<T> key, T instance) {
        this.context.put(key, () -> instance);
    }
}
