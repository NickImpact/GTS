/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package me.nickimpact.gts.common.placeholders.builders.variables;

import com.google.common.reflect.TypeToken;
import me.nickimpact.gts.api.placeholders.PlaceholderVariables;
import me.nickimpact.gts.common.placeholders.variables.GTSPlaceholderVariableKey;
import org.checkerframework.checker.nullness.qual.NonNull;

public class GTSPlaceholderVariableKeyBuilder implements PlaceholderVariables.KeyBuilder {

	public static final GTSPlaceholderVariableKeyBuilder INSTANCE = new GTSPlaceholderVariableKeyBuilder();

	private GTSPlaceholderVariableKeyBuilder() {
		// nope
	}

	@Override
	public <T> PlaceholderVariables.@NonNull Key<T> build(String name, TypeToken<T> clazz) {
		return new GTSPlaceholderVariableKey<>(name, clazz);
	}

	@Override
	public PlaceholderVariables.@NonNull KeyBuilder from(PlaceholderVariables.@NonNull Key<?> value) {
		return this;
	}

	@Override
	public PlaceholderVariables.Key<?> build() {
		throw new IllegalStateException("Incorrect build option, use the alternative");
	}

}
