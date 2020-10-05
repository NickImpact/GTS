package net.impactdev.gts.api.events.extension;

import net.impactdev.impactor.api.event.ImpactorEvent;
import net.impactdev.impactor.api.event.annotations.Param;

public interface PlaceholderRegistryEvent<T> extends ImpactorEvent.Generic<T> {

    @Param(0)
    T getManager();

}
