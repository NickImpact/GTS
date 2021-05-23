package net.impactdev.gts.common.config.wrappers

import net.impactdev.gts.common.plugin.GTSPlugin
import net.impactdev.impactor.api.configuration.ConfigKey

class AtLeastOne(private val compare: ConfigKey<Boolean>, private val state: Boolean) {
    fun get(): Boolean {
        return if (state) {
            state
        } else !GTSPlugin.instance.configuration.get(compare)
    }
}