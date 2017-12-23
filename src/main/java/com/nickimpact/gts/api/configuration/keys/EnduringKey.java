package com.nickimpact.gts.api.configuration.keys;

import com.nickimpact.gts.api.configuration.ConfigKey;
import lombok.AllArgsConstructor;
import lombok.experimental.Delegate;

/**
 * Wrapper class to mark a config key as enduring (doesn't change in the event of a reload)
 * @param <T>
 */
@AllArgsConstructor(staticName = "wrap")
public class EnduringKey<T> implements ConfigKey<T> {

	@Delegate
	private final ConfigKey<T> delegate;

}
