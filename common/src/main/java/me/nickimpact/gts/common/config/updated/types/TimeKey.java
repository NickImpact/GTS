package me.nickimpact.gts.common.config.updated.types;

import com.nickimpact.impactor.api.configuration.ConfigurationAdapter;
import com.nickimpact.impactor.api.configuration.keys.BaseConfigKey;
import com.nickimpact.impactor.api.utilities.Time;

public class TimeKey extends BaseConfigKey<Time> {

    private String key;
    private long def;

    @Override
    public Time get(ConfigurationAdapter adapter) {
        return new Time(adapter.getLong(key, def));
    }

}
