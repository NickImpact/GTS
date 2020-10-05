package net.impactdev.gts.api.events.extension;

import net.impactdev.impactor.api.event.ImpactorEvent;
import net.impactdev.impactor.api.event.annotations.Param;
import net.impactdev.gts.api.extension.Extension;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface ExtensionLoadEvent extends ImpactorEvent {

    @NonNull
    @Param(0)
    Extension getExtension();

}
