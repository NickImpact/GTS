package net.impactdev.gts.common.maintenance;

import net.impactdev.gts.api.events.maintenance.MaintenanceModeUpdateEvent;
import net.impactdev.gts.api.maintenance.MaintenanceManager;
import net.impactdev.gts.api.maintenance.MaintenanceMode;
import net.impactdev.impactor.api.event.annotations.Subscribe;
import net.impactdev.impactor.api.event.listener.ImpactorEventListener;

import java.util.EnumMap;

public class MaintenanceManagerImpl implements MaintenanceManager, ImpactorEventListener {

    private final EnumMap<MaintenanceMode, Boolean> mapping = new EnumMap<>(MaintenanceMode.class);

    @Override
    public boolean getState(MaintenanceMode mode) {
        return this.mapping.get(mode);
    }

    @Override
    public void setState(MaintenanceMode mode, boolean state) {
        this.mapping.put(mode, state);
    }

    @Subscribe
    public void onMaintenanceUpdate(MaintenanceModeUpdateEvent event) {

    }

}
