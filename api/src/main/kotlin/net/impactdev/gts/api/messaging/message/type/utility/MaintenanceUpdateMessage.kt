package net.impactdev.gts.api.messaging.message.type.utility

import net.impactdev.gts.api.maintenance.MaintenanceMode
import net.impactdev.gts.api.messaging.message.OutgoingMessage
import net.impactdev.gts.api.messaging.message.type.UpdateMessage

interface MaintenanceUpdateMessage : UpdateMessage, OutgoingMessage {
    val mode: MaintenanceMode?
    val state: Boolean
}