package me.nickimpact.gts.api.data.registry;

import me.nickimpact.gts.api.data.ResourceManager;
import me.nickimpact.gts.api.data.Storable;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.entries.EntryManager;
import me.nickimpact.gts.api.listings.prices.Price;
import me.nickimpact.gts.api.listings.prices.PriceManager;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface GTSComponentManager {

    // Listings

    <T extends Listing> void registerListingResourceManager(Class<T> type, ResourceManager<T> resource);

    <T extends Listing> Optional<ResourceManager<T>> getListingResourceManager(Class<T> type);

    Map<Class<? extends Listing>, ResourceManager<? extends Listing>> getAllListingResourceManagers();

    // Entries

    <T extends Entry<?, ?>> void registerEntryManager(Class<T> type, EntryManager<T, ?> manager);

    <T extends Entry<?, ?>> Optional<EntryManager<T, ?>> getEntryManager(String key);

    Map<String, EntryManager<? extends Entry<?, ?>, ?>> getAllEntryManagers();

    // Prices

    <T extends Price<?, ?>> void registerPriceManager(Class<T> type, PriceManager<T, ?> resource);

    <T extends Price<?, ?>> Optional<PriceManager<T, ?>> getPriceManager(String key);

    Map<String, PriceManager<? extends Price<?, ?>, ?>> getAllPriceManagers();

    // Legacy

    @Deprecated
    <T extends Entry<?, ?>> void registerLegacyEntryDeserializer(String key, Storable.Deserializer<T> deserializer);

    @Deprecated
    <T extends Entry<?, ?>> Optional<Storable.Deserializer<T>> getLegacyEntryDeserializer(String key);

}
