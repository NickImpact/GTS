package me.nickimpact.gts.api.events.extension;

import com.nickimpact.impactor.api.event.ImpactorEvent;
import com.nickimpact.impactor.api.event.annotations.Param;
import me.nickimpact.gts.api.extension.Extension;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface ExtensionLoadEvent extends ImpactorEvent {

    @NonNull
    @Param(0)
    Extension getExtension();

}
