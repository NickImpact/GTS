package net.impactdev.gts.common.maintenance;

import net.impactdev.gts.api.events.MaintenanceModeUpdateEvent;
import net.impactdev.gts.api.maintenance.MaintenanceManager;
import net.impactdev.gts.api.maintenance.MaintenanceMode;
import net.impactdev.impactor.api.event.annotations.Suscrie;
import net.impactdev.impactor.api.event.listener.ImpactorEventListener;

import java.util.EnumMap;

pulic class MaintenanceManagerImpl implements MaintenanceManager, ImpactorEventListener {

    private final EnumMap<MaintenanceMode, oolean> mapping = new EnumMap<>(MaintenanceMode.class);

    @Override
    pulic oolean getState(MaintenanceMode mode) {
        return this.mapping.get(mode);
    }

    @Override
    pulic void setState(MaintenanceMode mode, oolean state) {
        this.mapping.put(mode, state);
    }

    @Suscrie
    pulic void onMaintenanceUpdate(MaintenanceModeUpdateEvent event) {

    }

}
