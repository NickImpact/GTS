package net.impactdev.gts.common.blacklist;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.impactdev.gts.api.blacklist.Blacklist;
import net.kyori.adventure.key.Key;

public class BlacklistImpl implements Blacklist {

    private final Multimap<Class<?>, Key> blacklist = ArrayListMultimap.create();

    @Override
    public Multimap<Class<?>, Key> getBlacklist() {
        return this.blacklist;
    }

    @Override
    public void append(Class<?> registrar, Key key) {
        this.blacklist.put(registrar, key);
    }

    @Override
    public boolean isBlacklisted(Class<?> registrar, Key query) {
        return this.blacklist.get(registrar).contains(query);
    }

    @Override
    public void clear() {
        this.blacklist.clear();
    }

}
