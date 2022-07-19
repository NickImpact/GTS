package net.impactdev.gts.api.events;

import net.impactdev.impactor.api.event.ImpactorEvent;
import org.spongepowered.api.util.annotation.eventgen.GenerateFactoryMethod;

import java.time.Instant;
import java.util.UUID;

@GenerateFactoryMethod
public interface PingEvent extends ImpactorEvent {

    UUID getPingID();

    Instant getTimeSent();

}
