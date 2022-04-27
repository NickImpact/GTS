package net.impactdev.gts.api.events.extension;

import net.impactdev.impactor.api.event.ImpactorEvent;
import net.impactdev.gts.api.extension.Extension;
import org.spongepowered.api.util.annotation.eventgen.GenerateFactoryMethod;

@GenerateFactoryMethod
public interface ExtensionLoadEvent extends ImpactorEvent {

    Extension getExtension();

}
