/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package me.nickimpact.gts.common.placeholders.variables;

import com.google.common.collect.ImmutableMap;
import me.nickimpact.gts.api.placeholders.PlaceholderVariables;

import java.util.Optional;

public class GTSPlaceholderVariables implements PlaceholderVariables {

	private final ImmutableMap<Key<?>, Object> map;

	public GTSPlaceholderVariables(ImmutableMap<Key<?>, Object> map) {
		this.map = map;
	}

	@Override
	public <T> Optional<T> get(Key<T> key) {
		return Optional.ofNullable((T) this.map.get(key));
	}

	public ImmutableMap<Key<?>, Object> getMap() {
		return this.map;
	}

}
