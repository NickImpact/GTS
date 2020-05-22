/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package me.nickimpact.gts.api.placeholders;

import com.google.common.reflect.TypeToken;
import me.nickimpact.gts.api.GTSService;
import me.nickimpact.gts.api.util.Builder;

import java.util.Optional;

/**
 * A container class for providing context for placeholders.
 */
public interface PlaceholderVariables {

	/**
	 * The empty {@link PlaceholderVariables}
	 */
	static PlaceholderVariables empty() {
		return GTSService.getInstance().getRegistry().createBuilder(PVBuilder.class).build();
	}

	/**
	 * Creates a builder to create a {@link PlaceholderVariables} object
	 *
	 * @return The builder
	 */
	static PlaceholderVariables.PVBuilder builder() {
		return GTSService.getInstance().getRegistry().createBuilder(PVBuilder.class);
	}

	/**
	 * Creates a {@link PlaceholderVariables.Key} for use as a key
	 * for the variables.
	 *
	 * @param name The name of the key
	 * @param valueType The type of variable that will be stored against
	 *                  this key
	 * @param <T> The type of value that will be stored around this key
	 * @return The {@link Key}
	 */
	static <T> Key<T> key(String name, Class<T> valueType) {
		return GTSService.getInstance().getRegistry().createBuilder(KeyBuilder.class).build(name, TypeToken.of(valueType));
	}

	/**
	 * Creates a {@link PlaceholderVariables.Key} for use as a key
	 * for the variables.
	 *
	 * @param name The name of the key
	 * @param valueType The type of variable that will be stored against
	 *                  this key
	 * @param <T> The type of value that will be stored around this key
	 * @return The {@link Key}
	 */
	static <T> Key<T> key(String name, TypeToken<T> valueType) {
		return GTSService.getInstance().getRegistry().createBuilder(KeyBuilder.class).build(name, valueType);
	}

	/**
	 * Gets a value based on the associated {@link Key}
	 *
	 * @param key The key
	 * @param <T> The value type to return
	 * @return The value, if it exists
	 */
	<T> Optional<T> get(PlaceholderVariables.Key<T> key);

	/**
	 * Creates a {@link PlaceholderVariables}
	 */
	interface PVBuilder extends Builder<PlaceholderVariables, PVBuilder> {

		/**
		 * Adds a value to the variable map.
		 *
		 * @param key The {@link Key} to associate the value with.
		 * @param value The value to put
		 * @param <T> The type of value
		 * @return This, for chaining
		 */
		<T> PVBuilder put(Key<T> key, T value);

		/**
		 * Creates a {@link PlaceholderVariables}
		 *
		 * @return The {@link PlaceholderVariables}
		 */
		PlaceholderVariables build();

	}

	/**
	 * A key that is to be associated with a value.
	 *
	 * @param <T> The type of value this key will be associated with
	 */
	interface Key<T> {

		/**
		 * The key name
		 *
		 * @return The name
		 */
		String key();

		/**
		 * The type of value
		 *
		 * @return The value class
		 */
		TypeToken<T> getValueClass();

	}

	/**
	 * Creates keys
	 */
	interface KeyBuilder extends Builder<Key<?>, KeyBuilder> {

		/**
		 * The key creator
		 *
		 * @param name The name
		 * @param clazz The value type
		 * @param <T> The value type
		 * @return The key
		 */
		<T> Key<T> build(String name, TypeToken<T> clazz);

	}

}
