package net.impactdev.gts.api.events.placeholders;

import net.impactdev.impactor.api.event.ImpactorEvent;
import net.impactdev.impactor.api.event.annotations.Param;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

/**
 * Represents the event when a placeholder's contents becomes available when they previously
 * weren't. This is namely meant for asynchronous placeholder value replacements.
 */
public interface PlaceholderReadyEvent extends ImpactorEvent {

    @Param(0)
    @NonNull
    UUID getSource();

    @Param(1)
    @NonNull
    String getPlaceholderID();

}
