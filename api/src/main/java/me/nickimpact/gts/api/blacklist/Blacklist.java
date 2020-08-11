package me.nickimpact.gts.api.blacklist;

import com.google.common.collect.Multimap;
import me.nickimpact.gts.api.listings.entries.Entry;

public interface Blacklist {

	Multimap<Class<? extends Entry<?, ?>>, String> getBlacklist();

	void append(Class<? extends Entry<?, ?>> registrar, String key);

	boolean isBlacklisted(Class<? extends Entry<?, ?>> registrar, String query);
}
