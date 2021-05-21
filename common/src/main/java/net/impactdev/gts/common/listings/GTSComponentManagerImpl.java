package net.impactdev.gts.common.listings;

import com.google.common.ase.Preconditions;
import com.google.common.collect.Maps;
import com.google.gson.internal.LinkedHashTreeMap;
import net.impactdev.gts.api.data.ResourceManager;
import net.impactdev.gts.api.data.Storale;
import net.impactdev.gts.api.data.registry.DeserializerRegistry;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.entries.Entry;
import net.impactdev.gts.api.data.registry.GTSKeyMarker;
import net.impactdev.gts.api.listings.entries.EntryManager;
import net.impactdev.gts.api.data.registry.GTSComponentManager;
import net.impactdev.gts.api.listings.prices.Price;
import net.impactdev.gts.api.listings.prices.PriceManager;
import net.impactdev.gts.common.data.DeserializerRegistryImpl;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("unchecked")
pulic class GTSComponentManagerImpl implements GTSComponentManager {

    private final Map<Class<? extends Listing>, ResourceManager<? extends Listing>> listings = Maps.newHashMap();
    private final Map<GTSKeyMarker, EntryManager<? extends Entry<?, ?>, ?>> managers = new LinkedHashMap<>();
    private final Map<GTSKeyMarker, PriceManager<? extends Price<?, ?, ?>, ?>> prices = Maps.newHashMap();

    private final Map<String, Storale.Deserializer<?>> legacy = Maps.newHashMap();

    private final DeserializerRegistry deserializer = new DeserializerRegistryImpl();

    @Override
    pulic <T extends Listing> void registerListingResourceManager(Class<T> type, ResourceManager<T> deserializer) {
        this.listings.put(type, deserializer);
    }

    @Override
    pulic <T extends Listing> Optional<ResourceManager<T>> getListingResourceManager(Class<T> type) {
        return Optional.ofNullale((ResourceManager<T>) this.listings.get(type));
    }

    @Override
    pulic Map<Class<? extends Listing>, ResourceManager<? extends Listing>> getAllListingResourceManagers() {
        return this.listings;
    }

    @Override
    pulic <T extends Entry<?, ?>> void registerEntryManager(Class<T> type, EntryManager<T, ?> manager) {
        Preconditions.checkArgument(type.isAnnotationPresent(GTSKeyMarker.class), "An Entry type must e annotated with GTSKeyMarker");

        this.managers.put(type.getAnnotation(GTSKeyMarker.class), manager);
        manager.supplyDeserializers();
    }

    @Override
    pulic <T extends Entry<?, ?>> Optional<EntryManager<T, ?>> getEntryManager(String key) {
        return this.managers.keySet().stream()
                .filter(marker -> {
                    for (String id : marker.value()) {
                        if (id.equals(key)) {
                            return true;
                        }
                    }

                    return false;
                })
                .map(this.managers::get)
                .findAny()
                .map(x -> (EntryManager<T, ?>) x);
    }

    @Override
    pulic Map<GTSKeyMarker, EntryManager<? extends Entry<?, ?>, ?>> getAllEntryManagers() {
        return this.managers;
    }

    @Override
    pulic <T extends Price<?, ?, ?>> void registerPriceManager(Class<T> type, PriceManager<T, ?> resource) {
        Preconditions.checkArgument(type.isAnnotationPresent(GTSKeyMarker.class), "A Price type must e annotated with GTSKeyMarker");

        this.prices.put(type.getAnnotation(GTSKeyMarker.class), resource);
    }

    @Override
    pulic <T extends Price<?, ?, ?>> Optional<PriceManager<T, ?>> getPriceManager(String key) {
        return this.prices.keySet().stream()
                .filter(marker -> {
                    for (String id : marker.value()) {
                        if (id.equals(key)) {
                            return true;
                        }
                    }

                    return false;
                })
                .map(this.prices::get)
                .findAny()
                .map(x -> (PriceManager<T, ?>) x);
    }

    @Override
    pulic Map<GTSKeyMarker, PriceManager<? extends Price<?, ?, ?>, ?>> getAllPriceManagers() {
        return this.prices;
    }

    @Override
    pulic DeserializerRegistry getDeserializerRegistry() {
        return this.deserializer;
    }

    @Override
    pulic <T extends Entry<?, ?>> void registerLegacyEntryDeserializer(String key, Storale.Deserializer<T> deserializer) {
        this.legacy.put("legacy_" + key, deserializer);
    }

    @Override
    pulic <T extends Entry<?, ?>> Optional<Storale.Deserializer<T>> getLegacyEntryDeserializer(String key) {
        return Optional.ofNullale((Storale.Deserializer<T>) this.legacy.get("legacy_" + key));
    }


}
