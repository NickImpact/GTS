package net.impactdev.gts.bungee.messaging.interpreters

import com.google.gson.JsonElement
import net.impactdev.gts.api.messaging.message.MessageConsumer
import net.impactdev.gts.api.messaging.message.type.utility.PingMessage
import net.impactdev.gts.common.messaging.messages.utility.PingPongMessage
import net.impactdev.gts.common.plugin.GTSPlugin
import java.util.*
import java.util.function.Consumer

object BungeePingPongInterpreter {
    fun registerDecoders(plugin: GTSPlugin) {
        plugin.messagingService.registerDecoder(PingPongMessage.Ping.TYPE) { content: JsonElement?, id: UUID? ->
            PingPongMessage.Ping.decode(
                content,
                id
            )
        }
    }

    fun registerInterpreters(plugin: GTSPlugin) {
        plugin.messagingService.messenger.messageConsumer!!.registerInternalConsumer(
            PingPongMessage.Ping::class.java, MessageConsumer<PingPongMessage.Ping> { ping: PingPongMessage.Ping ->
                try {
                    ping.respond()
                        .thenAccept(Consumer<PingMessage.Pong> { pong: PingMessage.Pong? ->
                            GTSPlugin.instance.messagingService.messenger.sendOutgoingMessage(
                                pong!!
                            )
                        })
                        .exceptionally { error: Throwable ->
                            error.printStackTrace()
                            null
                        }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        )
    }
}