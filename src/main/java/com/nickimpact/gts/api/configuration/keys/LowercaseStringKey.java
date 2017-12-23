package com.nickimpact.gts.api.configuration.keys;

import com.nickimpact.gts.api.configuration.ConfigAdapter;
import com.nickimpact.gts.api.configuration.ConfigKey;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "of")
public class LowercaseStringKey implements ConfigKey<String> {
	private final String path;
	private final String def;

	@Override
	public String get(ConfigAdapter adapter) {
		return adapter.getString(path, def).toLowerCase();
	}
}
