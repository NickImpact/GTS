package net.impactdev.gts.api.events.metadata;

import net.impactdev.impactor.api.events.ImpactorEvent;
import net.kyori.event.Cancellable;
import org.spongepowered.api.util.annotation.eventgen.PropertySettings;

public interface CancellableEvent extends ImpactorEvent, Cancellable {

    @Override
    @PropertySettings(requiredParameter = false)
    boolean cancelled();

}
