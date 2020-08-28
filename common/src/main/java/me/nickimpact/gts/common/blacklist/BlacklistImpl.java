package me.nickimpact.gts.common.blacklist;

import com.google.common.collect.Multimap;
import me.nickimpact.gts.api.blacklist.Blacklist;
import me.nickimpact.gts.api.listings.entries.Entry;

public class BlacklistImpl implements Blacklist {
    @Override
    public Multimap<Class<? extends Entry<?, ?>>, String> getBlacklist() {
        return null;
    }

    @Override
    public void append(Class<? extends Entry<?, ?>> registrar, String key) {

    }

    @Override
    public boolean isBlacklisted(Class<? extends Entry<?, ?>> registrar, String query) {
        return false;
    }
}
