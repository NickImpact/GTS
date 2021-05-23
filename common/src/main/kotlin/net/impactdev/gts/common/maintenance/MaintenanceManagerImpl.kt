package net.impactdev.gts.common.maintenance

import net.impactdev.gts.api.events.MaintenanceModeUpdateEvent
import net.impactdev.gts.api.maintenance.MaintenanceManager
import net.impactdev.gts.api.maintenance.MaintenanceMode
import net.impactdev.impactor.api.event.annotations.Subscribe
import net.impactdev.impactor.api.event.listener.ImpactorEventListener
import java.util.*
import kotlin.collections.set

class MaintenanceManagerImpl : MaintenanceManager, ImpactorEventListener {
    private val mapping: EnumMap<MaintenanceMode?, Boolean> = EnumMap<MaintenanceMode, Boolean>(
        MaintenanceMode::class.java
    )

    override fun getState(mode: MaintenanceMode?): Boolean {
        return mapping[mode]!!
    }

    override fun setState(mode: MaintenanceMode?, state: Boolean) {
        mapping[mode] = state
    }

    @Subscribe
    fun onMaintenanceUpdate(event: MaintenanceModeUpdateEvent?) {
    }
}