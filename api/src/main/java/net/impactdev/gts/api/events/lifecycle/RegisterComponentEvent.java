package net.impactdev.gts.api.events.lifecycle;

import net.impactdev.impactor.api.event.ImpactorEvent;

public interface RegisterComponentEvent extends ImpactorEvent {

    interface Entries extends RegisterComponentEvent {

    }

    interface Prices extends RegisterComponentEvent {

    }

}
