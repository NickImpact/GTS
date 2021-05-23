package net.impactdev.gts.api.events

import net.impactdev.gts.api.maintenance.MaintenanceMode

interface MaintenanceModeUpdateEvent {
    @get:Param(0)
    val mode: MaintenanceMode?

    @get:Param(1)
    val newState: Boolean
}