package net.impactdev.gts.api.messaging.message.type.utility

import net.impactdev.gts.api.messaging.message.OutgoingMessage
import net.impactdev.gts.api.messaging.message.type.MessageType
import net.impactdev.gts.api.messaging.message.type.UpdateMessage
import net.impactdev.gts.api.messaging.message.type.utility.PingMessage.Pong

/**
 * Represents a message that GTS can use as a means to ping the proxy and vice versa. These messages are namely
 * useful in determining the messaging service is properly connected.
 */
interface PingMessage : UpdateMessage, OutgoingMessage {
    /**
     * This message type simply asks the proxy server if it can see this ping. If it does, it'll respond
     * to that server with a [Pong] message.
     */
    interface Ping : PingMessage, MessageType.Request<Pong?>

    /**
     * Simply indicates that the proxy has heard the ping sent.
     */
    interface Pong : PingMessage, MessageType.Response {
        abstract override var responseTime: Long
            set(responseTime) {
                super.responseTime = responseTime
            }
    }
}