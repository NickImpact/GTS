package net.impactdev.gts.common.api

import com.google.common.collect.ImmutableList
import com.google.common.collect.Lists
import net.impactdev.gts.api.GTSService
import net.impactdev.gts.api.data.registry.GTSComponentManager
import net.impactdev.gts.api.data.translators.DataTranslatorManager
import net.impactdev.gts.api.extension.Extension
import net.impactdev.gts.api.maintenance.MaintenanceManager
import net.impactdev.gts.api.messaging.message.errors.ErrorCode
import net.impactdev.gts.api.player.PlayerSettingsManager
import net.impactdev.gts.api.searching.Searcher
import net.impactdev.gts.common.data.DataTranslatorManagerImpl
import net.impactdev.gts.common.listings.GTSComponentManagerImpl
import net.impactdev.gts.common.player.PlayerSettingsManagerImpl
import net.impactdev.gts.common.plugin.GTSPlugin

class GTSAPIProvider : GTSService {
    private val entryManagerRegistry: GTSComponentManager = GTSComponentManagerImpl()
    private val playerSettingsManager: PlayerSettingsManager = PlayerSettingsManagerImpl()
    private val searchers: MutableList<Searcher> = Lists.newArrayList()
    private val dataTranslatorManager: DataTranslatorManager = DataTranslatorManagerImpl()
    private var safe = false
    private var reason: ErrorCode? = null
    override fun getAllExtensions(): ImmutableList<Extension> {
        return ImmutableList.copyOf(GTSPlugin.instance.extensionManager.loadedExtensions)
    }

    override fun getGTSComponentManager(): GTSComponentManager {
        return entryManagerRegistry
    }

    override fun getPlayerSettingsManager(): PlayerSettingsManager {
        return playerSettingsManager
    }

    override fun getMaintenanceManager(): MaintenanceManager {
        return null
    }

    override fun getDataTranslatorManager(): DataTranslatorManager {
        return dataTranslatorManager
    }

    override fun addSearcher(searcher: Searcher) {
        searchers.add(searcher)
    }

    override fun getSearchers(): List<Searcher> {
        return searchers
    }

    override fun isInSafeMode(): Boolean {
        return safe
    }

    override fun getSafeModeReason(): ErrorCode {
        return reason!!
    }

    fun setSafeMode(reason: ErrorCode?) {
        safe = true
        this.reason = reason
    }
}