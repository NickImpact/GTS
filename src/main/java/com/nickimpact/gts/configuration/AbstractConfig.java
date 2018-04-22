/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

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
