package net.impactdev.gts.common.config.types.time

import net.impactdev.impactor.api.configuration.ConfigurationAdapter
import net.impactdev.impactor.api.configuration.keys.BaseConfigKey
import net.impactdev.impactor.api.utilities.Time

class TimeKey(private val key: String, private val def: String) : BaseConfigKey<Time>() {
    override fun get(adapter: ConfigurationAdapter): Time {
        return Time(adapter.getString(key, def))
    }
}