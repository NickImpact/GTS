package me.nickimpact.gts.common.listings;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.entries.EntryKey;
import me.nickimpact.gts.api.listings.entries.EntryManager;
import me.nickimpact.gts.api.listings.entries.registry.EntryManagerRegistry;

import java.util.Map;
import java.util.Optional;

public class EntryManagerRegistryImpl implements EntryManagerRegistry {

    private final Map<String, EntryManager<?, ?>> managers = Maps.newHashMap();

    @Override
    public <T extends Entry<?, ?>> void register(Class<T> clazz, EntryManager<T, ?> manager) {
        Preconditions.checkArgument(clazz.isAnnotationPresent(EntryKey.class), "An Entry type must be annotated with EntryKey");

        this.managers.put(clazz.getAnnotation(EntryKey.class).value(), manager);
    }

    @Override
    public Optional<EntryManager<?, ?>> get(String key) {
        return Optional.ofNullable(this.managers.get(key));
    }

}
