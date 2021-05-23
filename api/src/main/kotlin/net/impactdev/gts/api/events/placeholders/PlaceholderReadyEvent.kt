package net.impactdev.gts.api.events.placeholders

import net.impactdev.impactor.api.event.ImpactorEvent
import java.util.*

/**
 * Represents the event when a placeholder's contents becomes available when they previously
 * weren't. This is namely meant for asynchronous placeholder value replacements.
 */
interface PlaceholderReadyEvent : ImpactorEvent {
    @get:Param(0)
    val source: UUID

    @get:Param(1)
    val placeholderID: String

    @get:Param(2)
    val value: Any
}