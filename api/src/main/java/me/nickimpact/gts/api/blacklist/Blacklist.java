package me.nickimpact.gts.api.blacklist;

import com.google.common.collect.Multimap;
import me.nickimpact.gts.api.listings.entries.Entry;

public interface Blacklist {

	Multimap<Class<?>, String> getBlacklist();

	void append(Class<?> registrar, String key);

	boolean isBlacklisted(Class<?> registrar, String query);

}
