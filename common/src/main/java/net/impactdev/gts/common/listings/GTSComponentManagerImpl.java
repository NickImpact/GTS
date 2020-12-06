package net.impactdev.gts.common.listings;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.gson.internal.LinkedHashTreeMap;
import net.impactdev.gts.api.data.ResourceManager;
import net.impactdev.gts.api.data.Storable;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.entries.Entry;
import net.impactdev.gts.api.data.registry.GTSKeyMarker;
import net.impactdev.gts.api.listings.entries.EntryManager;
import net.impactdev.gts.api.data.registry.GTSComponentManager;
import net.impactdev.gts.api.listings.prices.Price;
import net.impactdev.gts.api.listings.prices.PriceManager;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("unchecked")
public class GTSComponentManagerImpl implements GTSComponentManager {

    private final Map<Class<? extends Listing>, ResourceManager<? extends Listing>> listings = Maps.newHashMap();
    private final Map<String, EntryManager<? extends Entry<?, ?>, ?>> managers = new LinkedHashMap<>();
    private final Map<String, PriceManager<? extends Price<?, ?, ?>, ?>> prices = Maps.newHashMap();

    private final Map<String, Storable.Deserializer<?>> legacy = Maps.newHashMap();

    @Override
    public <T extends Listing> void registerListingResourceManager(Class<T> type, ResourceManager<T> deserializer) {
        this.listings.put(type, deserializer);
    }

    @Override
    public <T extends Listing> Optional<ResourceManager<T>> getListingResourceManager(Class<T> type) {
        return Optional.ofNullable((ResourceManager<T>) this.listings.get(type));
    }

    @Override
    public Map<Class<? extends Listing>, ResourceManager<? extends Listing>> getAllListingResourceManagers() {
        return this.listings;
    }

    @Override
    public <T extends Entry<?, ?>> void registerEntryManager(Class<T> type, EntryManager<T, ?> manager) {
        Preconditions.checkArgument(type.isAnnotationPresent(GTSKeyMarker.class), "An Entry type must be annotated with GTSKeyMarker");

        this.managers.put(type.getAnnotation(GTSKeyMarker.class).value().toLowerCase(), manager);
    }

    @Override
    public <T extends Entry<?, ?>> Optional<EntryManager<T, ?>> getEntryManager(String key) {
        return Optional.ofNullable((EntryManager<T, ?>) this.managers.get(key.toLowerCase()));
    }

    @Override
    public Map<String, EntryManager<? extends Entry<?, ?>, ?>> getAllEntryManagers() {
        return this.managers;
    }

    @Override
    public <T extends Price<?, ?, ?>> void registerPriceManager(Class<T> type, PriceManager<T, ?> resource) {
        Preconditions.checkArgument(type.isAnnotationPresent(GTSKeyMarker.class), "A Price type must be annotated with GTSKeyMarker");

        this.prices.put(type.getAnnotation(GTSKeyMarker.class).value(), resource);
    }

    @Override
    public <T extends Price<?, ?, ?>> Optional<PriceManager<T, ?>> getPriceManager(String key) {
        return Optional.ofNullable((PriceManager<T, ?>) this.prices.get(key));
    }

    @Override
    public Map<String, PriceManager<? extends Price<?, ?, ?>, ?>> getAllPriceManagers() {
        return this.prices;
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
