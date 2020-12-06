package net.impactdev.gts.api.blacklist;

import com.google.common.collect.Multimap;

public interface Blacklist {

	Multimap<Class<?>, String> getBlacklist();

	void append(Class<?> registrar, String key);

	boolean isBlacklisted(Class<?> registrar, String query);

    void clear();
}
