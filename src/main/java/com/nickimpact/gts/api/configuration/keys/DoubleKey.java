package com.nickimpact.gts.api.configuration.keys;

import com.nickimpact.gts.api.configuration.ConfigAdapter;
import com.nickimpact.gts.api.configuration.ConfigKey;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "of")
public class DoubleKey implements ConfigKey<Double> {
	private final String path;
	private final double def;

	@Override
	public Double get(ConfigAdapter adapter) {
		return adapter.getDouble(path, def);
	}
}
