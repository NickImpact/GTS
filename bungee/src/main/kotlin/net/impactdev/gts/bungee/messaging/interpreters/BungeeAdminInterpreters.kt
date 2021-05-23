package net.impactdev.gts.bungee.messaging.interpreters

import com.google.gson.JsonElement
import net.impactdev.gts.api.messaging.message.MessageConsumer
import net.impactdev.gts.api.messaging.message.type.admin.ForceDeleteMessage
import net.impactdev.gts.common.messaging.interpreters.Interpreter
import net.impactdev.gts.common.messaging.messages.admin.ForceDeleteMessageImpl.ForceDeleteRequest
import net.impactdev.gts.common.plugin.GTSPlugin
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter
import java.util.*
import java.util.function.Consumer

class BungeeAdminInterpreters : Interpreter {
    override fun register(plugin: GTSPlugin) {
        getDecoders(plugin)
        getInterpreters(plugin)
    }

    override fun getDecoders(plugin: GTSPlugin) {
        plugin.messagingService.registerDecoder(
            ForceDeleteRequest.TYPE
        ) { content: JsonElement?, id: UUID? -> ForceDeleteRequest.decode(content, id) }
    }

    override fun getInterpreters(plugin: GTSPlugin) {
        val consumer = plugin.messagingService.messenger.messageConsumer
        consumer!!.registerInternalConsumer(
            ForceDeleteRequest::class.java, MessageConsumer<ForceDeleteRequest> { request: ForceDeleteRequest ->
                request.respond()!!
                    .thenAccept(
                        Consumer<ForceDeleteMessage.Response> { response: ForceDeleteMessage.Response? ->
                            GTSPlugin.instance.messagingService.messenger.sendOutgoingMessage(
                                response!!
                            )
                            GTSPlugin.instance.pluginLogger.info("Response sent")
                        })
                    .exceptionally { e: Throwable? ->
                        ExceptionWriter.write(e)
                        null
                    }
            }
        )
    }
}