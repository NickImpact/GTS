package net.impactdev.gts.api.events.extension

import net.impactdev.impactor.api.event.ImpactorEvent

interface PlaceholderRegistryEvent<T> : ImpactorEvent.Generic<T> {
    @get:Param(0)
    val manager: T
}