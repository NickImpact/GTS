package me.nickimpact.gts.common.listings;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import me.nickimpact.gts.api.data.Storable;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.data.registry.GTSKeyMarker;
import me.nickimpact.gts.api.listings.entries.EntryManager;
import me.nickimpact.gts.api.data.registry.GTSComponentManager;
import me.nickimpact.gts.api.listings.prices.Price;

import java.util.Map;
import java.util.Optional;

@SuppressWarnings("unchecked")
public class GTSComponentManagerImpl implements GTSComponentManager {

    private final Map<Class<?>, Storable.Deserializer<?>> listings = Maps.newHashMap();
    private final Map<String, EntryManager<?, ?>> managers = Maps.newHashMap();
    private final Map<String, Storable.Deserializer<?>> prices = Maps.newHashMap();

    private final Map<String, Storable.Deserializer<?>> legacy = Maps.newHashMap();

    @Override
    public <T extends Listing> void registerListingDeserializer(Class<T> type, Storable.Deserializer<T> deserializer) {
        this.listings.put(type, deserializer);
    }

    @Override
    public <T extends Listing> Optional<Storable.Deserializer<T>> getListingDeserializer(Class<T> type) {
        return Optional.ofNullable((Storable.Deserializer<T>) this.listings.get(type));
    }

    @Override
    public <T extends Entry<?, ?>> void registerEntryDeserializer(Class<T> clazz, EntryManager<T, ?> manager) {
        Preconditions.checkArgument(clazz.isAnnotationPresent(GTSKeyMarker.class), "An Entry type must be annotated with GTSKeyMarker");

        this.managers.put(clazz.getAnnotation(GTSKeyMarker.class).value(), manager);
    }

    @Override
    public <T extends Entry<?, ?>> Optional<EntryManager<T, ?>> getEntryDeserializer(String key) {
        return Optional.ofNullable((EntryManager<T, ?>) this.managers.get(key));
    }

    @Override
    public <T extends Price<?, ?>> void registerPriceDeserializer(Class<T> type, Storable.Deserializer<T> deserializer) {
        Preconditions.checkArgument(type.isAnnotationPresent(GTSKeyMarker.class), "A Price type must be annotated with GTSKeyMarker");

        this.prices.put(type.getAnnotation(GTSKeyMarker.class).value(), deserializer);
    }

    @Override
    public <T extends Price<?, ?>> Optional<Storable.Deserializer<T>> getPriceDeserializer(String key) {
        return Optional.ofNullable((Storable.Deserializer<T>) this.prices.get(key));
    }

    @Override
    public <T extends Entry<?, ?>> void registerLegacyEntryDeserializer(String key, Storable.Deserializer<T> deserializer) {
        this.legacy.put("legacy_" + key, deserializer);
    }

    @Override
    public <T extends Entry<?, ?>> Optional<Storable.Deserializer<T>> getLegacyEntryDeserializer(String key) {
        return Optional.ofNullable((Storable.Deserializer<T>) this.legacy.get("legacy_" + key));
    }


}
