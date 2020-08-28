package me.nickimpact.gts.common.config.updated.types.time;

import com.nickimpact.impactor.api.configuration.ConfigurationAdapter;
import com.nickimpact.impactor.api.configuration.keys.BaseConfigKey;
import com.nickimpact.impactor.api.utilities.Time;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TimeKey extends BaseConfigKey<Time> {

    private final String key;
    private final String def;

    @Override
    public Time get(ConfigurationAdapter adapter) {
        return new Time(adapter.getString(key, def));
    }

}
