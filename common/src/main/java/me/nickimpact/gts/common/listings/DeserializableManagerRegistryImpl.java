package me.nickimpact.gts.common.listings;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import me.nickimpact.gts.api.data.Storable;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.data.registry.GTSKeyMarker;
import me.nickimpact.gts.api.listings.entries.EntryManager;
import me.nickimpact.gts.api.data.registry.DeserializableManagerRegistry;
import me.nickimpact.gts.api.listings.prices.Price;

import java.util.Map;
import java.util.Optional;

public class DeserializableManagerRegistryImpl implements DeserializableManagerRegistry {

    private final Map<Class<?>, Storable.Deserializer<?>> listings = Maps.newHashMap();
    private final Map<String, EntryManager<?, ?>> managers = Maps.newHashMap();
    private final Map<String, Storable.Deserializer<?>> prices = Maps.newHashMap();

    @Override
    public <T extends Listing> void registerListingDeserializer(Class<T> type, Storable.Deserializer<T> deserializer) {
        this.listings.put(type, deserializer);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Listing> Optional<Storable.Deserializer<T>> getListingDeserializer(Class<T> type) {
        return Optional.ofNullable((Storable.Deserializer<T>) this.listings.get(type));
    }

    @Override
    public <T extends Entry<?, ?>> void registerEntryDeserializer(Class<T> clazz, EntryManager<T, ?> manager) {
        Preconditions.checkArgument(clazz.isAnnotationPresent(GTSKeyMarker.class), "An Entry type must be annotated with GTSKeyMarker");

        this.managers.put(clazz.getAnnotation(GTSKeyMarker.class).value(), manager);
    }

    @Override
    public Optional<EntryManager<?, ?>> getEntryDeserializer(String key) {
        return Optional.ofNullable(this.managers.get(key));
    }

    @Override
    public <T extends Price<?, ?>> void registerPriceDeserializer(Class<T> type, Storable.Deserializer<T> deserializer) {
        Preconditions.checkArgument(type.isAnnotationPresent(GTSKeyMarker.class), "A Price type must be annotated with GTSKeyMarker");

        this.prices.put(type.getAnnotation(GTSKeyMarker.class).value(), deserializer);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Price<?, ?>> Optional<Storable.Deserializer<T>> getPriceDeserializer(String key) {
        return Optional.ofNullable((Storable.Deserializer<T>) this.prices.get(key));
    }


}
