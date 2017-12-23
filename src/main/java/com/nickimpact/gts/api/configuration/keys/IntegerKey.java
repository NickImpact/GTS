package com.nickimpact.gts.api.configuration.keys;

import com.nickimpact.gts.api.configuration.ConfigAdapter;
import com.nickimpact.gts.api.configuration.ConfigKey;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "of")
public class IntegerKey implements ConfigKey<Integer> {
	private final String path;
	private final int def;

	@Override
	public Integer get(ConfigAdapter adapter) {
		return adapter.getInt(path, def);
	}
}
