package net.impactdev.gts.api.events.extension

import net.impactdev.gts.api.extension.Extension
import net.impactdev.impactor.api.event.ImpactorEvent

interface ExtensionLoadEvent : ImpactorEvent {
    @get:Param(0)
    val extension: Extension
}