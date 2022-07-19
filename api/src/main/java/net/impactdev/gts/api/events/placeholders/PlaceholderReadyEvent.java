package net.impactdev.gts.api.events.placeholders;

import net.impactdev.impactor.api.event.ImpactorEvent;
import org.spongepowered.api.util.annotation.eventgen.GenerateFactoryMethod;

import java.util.UUID;

/**
 * Represents the event when a placeholder's contents becomes available when they previously
 * weren't. This is namely meant for asynchronous placeholder value replacements.
 */
@GenerateFactoryMethod
public interface PlaceholderReadyEvent extends ImpactorEvent {

    UUID getSource();

    String getPlaceholderID();

    Object getValue();

}
