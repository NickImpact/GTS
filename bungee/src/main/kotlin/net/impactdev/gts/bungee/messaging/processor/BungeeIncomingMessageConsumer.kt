package net.impactdev.gts.bungee.messaging.processor

import com.google.common.collect.Maps
import com.google.gson.JsonObject
import net.impactdev.gts.api.messaging.IncomingMessageConsumer
import net.impactdev.gts.api.messaging.message.Message
import net.impactdev.gts.api.messaging.message.MessageConsumer
import net.impactdev.gts.api.messaging.message.type.MessageType
import net.impactdev.gts.api.messaging.message.type.UpdateMessage
import net.impactdev.gts.bungee.GTSBungeePlugin
import net.impactdev.gts.common.messaging.GTSMessagingService
import net.impactdev.gts.common.plugin.GTSPlugin
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter
import java.util.*
import java.util.function.Consumer

class BungeeIncomingMessageConsumer(private val plugin: GTSBungeePlugin?) : IncomingMessageConsumer {
    private val receivedMessages: MutableSet<UUID?>
    private val consumers: MutableMap<Class<*>?, MessageConsumer<*>?> = Maps.newHashMap()
    override fun <T : MessageType.Response?> registerRequest(request: UUID?, response: Consumer<T>?) {}
    override fun <T : MessageType.Response?> processRequest(request: UUID?, response: T) {}
    override fun cacheReceivedID(id: UUID?) {
        receivedMessages.add(id)
    }

    override fun consumeIncomingMessage(message: Message): Boolean {
        Objects.requireNonNull(message, "message")
        if (!receivedMessages.add(message.iD)) {
            return false
        }
        processIncomingMessage(message)
        return true
    }

    override fun consumeIncomingMessageAsString(encodedString: String): Boolean {
        Objects.requireNonNull(encodedString, "encodedString")
        val decodedObject = GTSMessagingService.NORMAL.fromJson(encodedString, JsonObject::class.java).asJsonObject

        // extract id
        val idElement = decodedObject["id"]
            ?: throw IllegalStateException("Incoming message has no id argument: $encodedString")
        val id = UUID.fromString(idElement.asString)

        // ensure the message hasn't been received already
        if (!receivedMessages.add(id)) {
            return false
        }

        // extract type
        val typeElement = decodedObject["type"]
            ?: throw IllegalStateException("Incoming message has no type argument: $encodedString")
        val type = typeElement.asString

        // extract content
        val content = decodedObject["content"]
        return try {
            // decode message
            val decoded = GTSPlugin.instance.messagingService.getDecoder(type).apply(content, id)
                ?: return false

            // consume the message
            processIncomingMessage(decoded)
            true
        } catch (e: Exception) {
            GTSPlugin.instance.pluginLogger.error("Failed to read message of type: $type")
            ExceptionWriter.write(e)
            false
        }
    }

    override fun <T : Message?, V : T?> registerInternalConsumer(parent: Class<T>?, consumer: MessageConsumer<V>?) {
        consumers[parent] = consumer
    }

    override fun getInternalConsumer(parent: Class<*>?): MessageConsumer<*>? {
        return consumers[parent]
    }

    private fun processIncomingMessage(message: Message) {
        if (message is UpdateMessage) {
            val msg = message
            plugin!!.pluginLogger.info("[Messaging] Received message with id: " + msg.iD)
            getInternalConsumer(msg.javaClass)!!.consume(message)
        } else {
            throw IllegalArgumentException("Unknown message type: " + message.javaClass.name)
        }
    }

    init {
        receivedMessages = Collections.synchronizedSet(HashSet())
    }
}