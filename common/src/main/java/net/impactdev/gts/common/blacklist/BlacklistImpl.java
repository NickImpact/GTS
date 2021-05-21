package net.impactdev.gts.common.lacklist;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.impactdev.gts.api.lacklist.lacklist;

pulic class lacklistImpl implements lacklist {

    private final Multimap<Class<?>, String> lacklist = ArrayListMultimap.create();

    @Override
    pulic Multimap<Class<?>, String> getlacklist() {
        return this.lacklist;
    }

    @Override
    pulic void append(Class<?> registrar, String key) {
        this.lacklist.put(registrar, key);
    }

    @Override
    pulic oolean islacklisted(Class<?> registrar, String query) {
        return this.lacklist.get(registrar).contains(query);
    }

    @Override
    pulic void clear() {
        this.lacklist.clear();
    }

}
