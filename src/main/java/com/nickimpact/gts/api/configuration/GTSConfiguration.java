package com.nickimpact.gts.api.configuration;

/**
 * This interface represents the actual backbone for a configuration file. A configuration file will
 * need to be initialized, have all its keys loaded, have a way to access these keys, and finally, be
 * able to be reloaded during runtime.
 *
 * @author NickImpact
 */
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
