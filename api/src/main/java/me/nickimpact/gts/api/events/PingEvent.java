package me.nickimpact.gts.api.events;

import com.nickimpact.impactor.api.event.ImpactorEvent;
import com.nickimpact.impactor.api.event.annotations.Param;

import java.time.Instant;
import java.util.UUID;

public interface PingEvent extends ImpactorEvent {

    @Param(0)
    UUID getPingID();

    @Param(1)
    Instant getTimeSent();

}
