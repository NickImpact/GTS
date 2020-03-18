/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package me.nickimpact.gts.common.placeholders.variables;

import com.google.common.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import me.nickimpact.gts.api.placeholders.PlaceholderVariables;

import java.util.Objects;

@RequiredArgsConstructor
public class GTSPlaceholderVariableKey<T> implements PlaceholderVariables.Key<T> {

	private final String key;
	private final TypeToken<T> clazz;

	@Override
	public String key() {
		return this.key;
	}

	@Override
	public TypeToken<T> getValueClass() {
		return this.clazz;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GTSPlaceholderVariableKey<?> that = (GTSPlaceholderVariableKey<?>) o;
		return Objects.equals(key, that.key) &&
				Objects.equals(clazz, that.clazz);
	}

	@Override
	public int hashCode() {
		return Objects.hash(key, clazz);
	}

}
