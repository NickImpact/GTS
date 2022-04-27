package net.impactdev.gts.api.events.extension;

import net.impactdev.impactor.api.event.ImpactorEvent;
import org.spongepowered.api.util.annotation.eventgen.GenerateFactoryMethod;

@GenerateFactoryMethod
public interface PlaceholderRegistryEvent<T> extends ImpactorEvent.Generic<T> {

    T getManager();

}
