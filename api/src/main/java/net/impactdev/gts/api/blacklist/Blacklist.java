package net.impactdev.gts.api.blacklist;

import com.google.common.collect.Multimap;
import net.kyori.adventure.key.Key;

public interface Blacklist {

	Multimap<Class<?>, Key> getBlacklist();

	void append(Class<?> registrar, Key key);

	boolean isBlacklisted(Class<?> registrar, Key query);

    void clear();
}
