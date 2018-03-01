package com.nickimpact.gts.configuration;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.nickimpact.gts.GTS;
import com.nickimpact.gts.api.configuration.*;
import com.nickimpact.gts.api.configuration.keys.EnduringKey;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@Getter
@RequiredArgsConstructor
public class AbstractConfig implements GTSConfiguration, CacheLoader<ConfigKey<?>, Optional<Object>> {

	// the loading cache for config keys --> their value
	// the value is wrapped in an optional as null values don't get cached.
	@Getter(AccessLevel.NONE)
	private final LoadingCache<ConfigKey<?>, Optional<Object>> cache = Caffeine.newBuilder().build(this);

	private final GTS plugin;

	private final ConfigAdapter adapter;

	private final String resource;

	@Override
	public void init() {
		adapter.init(resource);
		loadAll();
	}

	@Override
	public void reload() {
		init();

		Set<ConfigKey<?>> toInvalidate = cache.asMap().keySet().stream().filter(k -> !(k instanceof EnduringKey)).collect(Collectors.toSet());
		cache.invalidateAll(toInvalidate);

		loadAll();

	}

	@Override
	public void loadAll() {
		if(resource.equals("gts.conf")) {
			ConfigKeys.getAllKeys().values().forEach(cache::get);
		} else {
			MsgConfigKeys.getAllKeys().values().forEach(cache::get);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(ConfigKey<T> key) {
		Optional<Object> ret = cache.get(key);
		if(ret == null) {
			return null;
		}

		return (T) ret.orElse(null);
	}

	@CheckForNull
	@Override
	public Optional<Object> load(@Nonnull ConfigKey<?> key) throws Exception {
		return Optional.ofNullable(key.get(adapter));
	}
}
