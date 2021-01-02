package net.impactdev.gts.api.events;

import net.impactdev.gts.api.maintenance.MaintenanceMode;
import net.impactdev.impactor.api.event.annotations.Param;

import java.util.UUID;

public interface MaintenanceModeUpdateEvent {

    @Param(0)
    UUID getServer();

    @Param(1)
    MaintenanceMode getMode();

    @Param(2)
    boolean getNewState();

}
