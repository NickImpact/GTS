package net.impactdev.gts.common.config.types.time;

import net.impactdev.impactor.api.configuration.ConfigurationAdapter;
import net.impactdev.impactor.api.configuration.keys.BaseConfigKey;
import net.impactdev.impactor.api.utilities.Time;
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
