package me.nickimpact.gts.reforged.config;

import com.google.common.collect.Maps;
import com.nickimpact.impactor.api.configuration.ConfigKey;
import com.nickimpact.impactor.api.configuration.ConfigKeyHolder;

import java.util.Map;

public class ReforgedKeys implements ConfigKeyHolder {

	@Override
	public Map<String, ConfigKey<?>> getKeys() {
		return Maps.newHashMap();
	}

	@Override
	public int getSize() {
		return 0;
	}
}
