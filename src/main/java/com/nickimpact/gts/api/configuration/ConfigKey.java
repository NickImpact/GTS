package com.nickimpact.gts.api.configuration;

/**
 * Represents a key in the configuration.
 *
 * @param <T> the value type
 */
public interface ConfigKey<T> {

	/**
	 * Resolves and returns the value mapped to this key using the given config instance.
	 *
	 * <p>The {@link GTSConfiguration#get(ConfigKey)} method should be used to
	 * retrieve the value, as opposed to calling this directly.</p>
	 *
	 * @param adapter the config adapter instance
	 * @return the value mapped to this key
	 */
	T get(ConfigAdapter adapter);
}
