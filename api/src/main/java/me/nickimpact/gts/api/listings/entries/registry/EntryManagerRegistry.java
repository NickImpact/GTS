package me.nickimpact.gts.api.listings.entries.registry;

import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.entries.EntryManager;

import java.util.Optional;

public interface EntryManagerRegistry {

    <T extends Entry<?, ?>> void register(Class<T> clazz, EntryManager<T, ?> manager);

    Optional<EntryManager<?, ?>> get(String key);

}
