package me.nickimpact.gts.api.data.registry;

import me.nickimpact.gts.api.data.Storable;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.entries.EntryManager;
import me.nickimpact.gts.api.listings.prices.Price;

import java.util.Optional;

public interface GTSComponentManager {

    <T extends Listing> void registerListingDeserializer(Class<T> type, Storable.Deserializer<T> deserializer);

    <T extends Listing> Optional<Storable.Deserializer<T>> getListingDeserializer(Class<T> type);

    <T extends Entry<?, ?>> void registerEntryDeserializer(Class<T> type, EntryManager<T, ?> manager);

    <T extends Entry<?, ?>> Optional<EntryManager<T, ?>> getEntryDeserializer(String key);

    <T extends Price<?, ?>> void registerPriceDeserializer(Class<T> type, Storable.Deserializer<T> deserializer);

    <T extends Price<?, ?>> Optional<Storable.Deserializer<T>> getPriceDeserializer(String key);

    @Deprecated
    <T extends Entry<?, ?>> void registerLegacyEntryDeserializer(String key, Storable.Deserializer<T> deserializer);

    @Deprecated
    <T extends Entry<?, ?>> Optional<Storable.Deserializer<T>> getLegacyEntryDeserializer(String key);


}
