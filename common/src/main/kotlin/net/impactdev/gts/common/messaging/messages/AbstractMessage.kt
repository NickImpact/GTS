package net.impactdev.gts.common.messaging.messages

import net.impactdev.gts.api.messaging.message.OutgoingMessage
import net.impactdev.gts.api.messaging.message.type.UpdateMessage
import java.util.*

abstract class AbstractMessage(override val iD: UUID) : UpdateMessage, OutgoingMessage