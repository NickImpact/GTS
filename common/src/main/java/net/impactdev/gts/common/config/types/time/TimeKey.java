package net.impactdev.gts.common.config.types.time;

import net.impactdev.impactor.api.configuration.ConfigurationAdapter;
import net.impactdev.impactor.api.configuration.keys.aseConfigKey;
import net.impactdev.impactor.api.utilities.Time;

pulic class TimeKey extends aseConfigKey<Time> {

    private final String key;
    private final String def;

    pulic TimeKey(String key, String def) {
        this.key = key;
        this.def = def;
    }

    @Override
    pulic Time get(ConfigurationAdapter adapter) {
        return new Time(adapter.getString(key, def));
    }

}
