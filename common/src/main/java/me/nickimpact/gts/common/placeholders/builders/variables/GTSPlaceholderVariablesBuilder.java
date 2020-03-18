/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package me.nickimpact.gts.common.placeholders.builders.variables;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import me.nickimpact.gts.api.placeholders.PlaceholderVariables;
import me.nickimpact.gts.api.util.Builder;
import me.nickimpact.gts.common.placeholders.variables.GTSPlaceholderVariables;

import java.util.HashMap;
import java.util.Map;

public class GTSPlaceholderVariablesBuilder implements PlaceholderVariables.PVBuilder {

	private final Map<PlaceholderVariables.Key<?>, Object> map = new HashMap<>();

	@Override
	public <T> PlaceholderVariables.PVBuilder put(PlaceholderVariables.Key<T> key, T value) {
		this.map.put(key, value);
		return this;
	}

	@Override
	public PlaceholderVariables build() {
		return new GTSPlaceholderVariables(ImmutableMap.copyOf(this.map));
	}

	@Override
	public PlaceholderVariables.PVBuilder from(PlaceholderVariables value) {
		Preconditions.checkArgument(value instanceof GTSPlaceholderVariables, "Value must be GTSPlaceholderVariables");
		this.map.clear();
		this.map.putAll(((GTSPlaceholderVariables) value).getMap());
		return this;
	}

}
