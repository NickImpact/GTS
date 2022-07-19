package net.impactdev.gts.api.events.maintenance;

import net.impactdev.gts.api.maintenance.MaintenanceMode;
import net.impactdev.impactor.api.event.ImpactorEvent;
import org.spongepowered.api.util.annotation.eventgen.GenerateFactoryMethod;

@GenerateFactoryMethod
public interface MaintenanceModeUpdateEvent extends ImpactorEvent {

    MaintenanceMode getMode();

    boolean getNewState();

}
