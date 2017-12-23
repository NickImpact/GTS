package com.nickimpact.gts.api.configuration;

public interface GTSConfiguration {

	/**
	 * Initialises the configuration.
	 */
	void init();

	/**
	 * Reloads the configuration.
	 */
	void reload();

	/**
	 * Pre-loads all configuration keys into the cache.
	 */
	void loadAll();

	/**
	 * Gets the value of a given context key.
	 *
	 * @param key the key
	 * @param <T> the key return type
	 * @return the value mapped to the given key. May be null.
	 */
	<T> T get(ConfigKey<T> key);
}
