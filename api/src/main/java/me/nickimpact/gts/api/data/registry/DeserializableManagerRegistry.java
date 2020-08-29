package me.nickimpact.gts.api.data.registry;

import me.nickimpact.gts.api.data.Storable;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.entries.EntryManager;
import me.nickimpact.gts.api.listings.prices.Price;

import java.util.Optional;

public interface DeserializableManagerRegistry {

    <T extends Listing> void registerListingDeserializer(Class<T> type, Storable.Deserializer<T> deserializer);

    <T extends Listing> Optional<Storable.Deserializer<T>> getListingDeserializer(Class<T> type);

    <T extends Entry<?, ?>> void registerEntryDeserializer(Class<T> type, EntryManager<T, ?> manager);

    Optional<EntryManager<?, ?>> getEntryDeserializer(String key);

    <T extends Price<?, ?>> void registerPriceDeserializer(Class<T> type, Storable.Deserializer<T> deserializer);

    <T extends Price<?, ?>> Optional<Storable.Deserializer<T>> getPriceDeserializer(String key);


}
