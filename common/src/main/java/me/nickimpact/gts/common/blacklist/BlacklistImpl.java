package me.nickimpact.gts.common.blacklist;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import me.nickimpact.gts.api.blacklist.Blacklist;
import me.nickimpact.gts.api.listings.entries.Entry;

public class BlacklistImpl implements Blacklist {

    private final Multimap<Class<?>, String> blacklist = ArrayListMultimap.create();

    @Override
    public Multimap<Class<?>, String> getBlacklist() {
        return this.blacklist;
    }

    @Override
    public void append(Class<?> registrar, String key) {
        this.blacklist.put(registrar, key);
    }

    @Override
    public boolean isBlacklisted(Class<?> registrar, String query) {
        return this.blacklist.get(registrar).contains(query);
    }

}
