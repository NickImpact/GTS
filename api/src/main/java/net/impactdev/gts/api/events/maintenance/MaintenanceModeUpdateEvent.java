package net.impactdev.gts.api.events.maintenance;

import net.impactdev.gts.api.maintenance.MaintenanceMode;
import net.impactdev.impactor.api.event.annotations.Param;

import java.util.UUID;

public interface MaintenanceModeUpdateEvent {

    @Param(0)
    MaintenanceMode getMode();

    @Param(1)
    boolean getNewState();

}
