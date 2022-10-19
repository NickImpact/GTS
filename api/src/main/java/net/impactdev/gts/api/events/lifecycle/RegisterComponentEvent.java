package net.impactdev.gts.api.events.lifecycle;

import net.impactdev.impactor.api.events.ImpactorEvent;

public interface RegisterComponentEvent extends ImpactorEvent {

    interface Entries extends RegisterComponentEvent {

    }

    interface Prices extends RegisterComponentEvent {

    }

}
