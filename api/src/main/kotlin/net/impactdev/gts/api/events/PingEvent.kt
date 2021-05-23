package net.impactdev.gts.api.events

import net.impactdev.impactor.api.event.ImpactorEvent
import java.time.Instant
import java.util.*

interface PingEvent : ImpactorEvent {
    @get:Param(0)
    val pingID: UUID?

    @get:Param(1)
    val timeSent: Instant?
}