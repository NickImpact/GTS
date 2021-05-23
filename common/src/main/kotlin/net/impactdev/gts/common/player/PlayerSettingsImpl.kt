package net.impactdev.gts.common.player

import com.google.common.collect.Maps
import net.impactdev.gts.api.player.NotificationSetting
import net.impactdev.gts.api.player.PlayerSettings
import net.impactdev.gts.api.player.PlayerSettings.PlayerSettingsBuilder
import net.impactdev.impactor.api.json.factory.JObject
import java.util.*
import kotlin.collections.Map
import kotlin.collections.MutableMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator
import kotlin.collections.set

class PlayerSettingsImpl private constructor(builder: PlayerSettingsImplBuilder) : PlayerSettings {
    private val states: MutableMap<NotificationSetting?, Boolean> = Maps.newHashMap()
    override fun getListeningState(setting: NotificationSetting?): Boolean {
        return states[setting]!!
    }

    override val listeningStates: Map<NotificationSetting?, Boolean>
        get() = states
    override val version: Int
        get() = 1

    override fun serialize(): JObject? {
        return JObject()
            .consume { o: JObject ->
                for ((key, value) in states) {
                    o.add(key!!.name, value)
                }
            }
    }

    class PlayerSettingsImplBuilder : PlayerSettingsBuilder {
        val settings: MutableMap<NotificationSetting?, Boolean> = HashMap()
        override fun set(setting: NotificationSetting?, mode: Boolean): PlayerSettingsBuilder {
            settings[setting] = mode
            return this
        }

        override fun from(settings: PlayerSettings): PlayerSettingsBuilder? {
            for ((key, value) in settings.listeningStates!!) {
                this.settings[key] = value
            }
            return this
        }

        override fun build(): PlayerSettings? {
            return PlayerSettingsImpl(this)
        }

        init {
            for (setting in NotificationSetting.values()) {
                settings[setting] = true
            }
        }
    }

    init {
        for ((key, value) in builder.settings) {
            states[key] = value
        }
    }
}