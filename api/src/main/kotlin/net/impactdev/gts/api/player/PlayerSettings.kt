package net.impactdev.gts.api.player

import net.impactdev.gts.api.data.Storable
import net.impactdev.impactor.api.Impactor
import net.impactdev.impactor.api.utilities.Builder

interface PlayerSettings : Storable {
    fun getListeningState(setting: NotificationSetting?): Boolean
    val listeningStates: Map<NotificationSetting?, Boolean?>?
    operator fun set(setting: NotificationSetting?, mode: Boolean): PlayerSettings? {
        return builder().from(this)!!.set(setting, mode).build()
    }

    val publishListenState: Boolean
        get() = getListeningState(NotificationSetting.Publish)
    val soldListenState: Boolean
        get() = getListeningState(NotificationSetting.Sold)
    val bidListenState: Boolean
        get() = getListeningState(NotificationSetting.Bid)
    val outbidListenState: Boolean
        get() = getListeningState(NotificationSetting.Outbid)

    fun matches(other: PlayerSettings): Boolean {
        return publishListenState == other.publishListenState && soldListenState == other.soldListenState && bidListenState == other.bidListenState && outbidListenState == other.outbidListenState
    }

    interface PlayerSettingsBuilder : Builder<PlayerSettings?, PlayerSettingsBuilder?> {
        /**
         * Applies the setting to the player's settings. All settings are considered on by default.
         *
         * @param setting The setting to set
         * @param mode The mode we wish to set this setting to
         * @return The builder after being modified by this request
         */
        operator fun set(setting: NotificationSetting?, mode: Boolean): PlayerSettingsBuilder
    }

    companion object {
        @kotlin.jvm.JvmStatic
        fun create(): PlayerSettings? {
            return builder().build()
        }

        @kotlin.jvm.JvmStatic
        fun builder(): PlayerSettingsBuilder {
            return Impactor.getInstance().registry.createBuilder(PlayerSettingsBuilder::class.java)
        }
    }
}