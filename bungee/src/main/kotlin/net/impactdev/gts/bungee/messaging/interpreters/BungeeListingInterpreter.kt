package net.impactdev.gts.bungee.messaging.interpreters

import com.google.gson.JsonElement
import net.impactdev.gts.api.messaging.message.MessageConsumer
import net.impactdev.gts.api.messaging.message.type.listings.ClaimMessage
import net.impactdev.gts.common.messaging.interpreters.Interpreter
import net.impactdev.gts.common.messaging.messages.listings.ClaimMessageImpl.ClaimRequestImpl
import net.impactdev.gts.common.messaging.messages.listings.PublishListingMessageImpl
import net.impactdev.gts.common.plugin.GTSPlugin
import java.util.*
import java.util.function.Consumer

class BungeeListingInterpreter : Interpreter {
    override fun register(plugin: GTSPlugin) {
        getDecoders(plugin)
        getInterpreters(plugin)
    }

    override fun getDecoders(plugin: GTSPlugin) {
        plugin.messagingService.registerDecoder(
            PublishListingMessageImpl.TYPE
        ) { content: JsonElement?, id: UUID? -> PublishListingMessageImpl.decode(content, id) }
        plugin.messagingService.registerDecoder(
            ClaimRequestImpl.TYPE
        ) { content: JsonElement?, id: UUID? -> ClaimRequestImpl.decode(content, id) }
    }

    override fun getInterpreters(plugin: GTSPlugin) {
        val consumer = plugin.messagingService.messenger.messageConsumer
        consumer!!.registerInternalConsumer(
            PublishListingMessageImpl::class.java,
            MessageConsumer<PublishListingMessageImpl> { message: PublishListingMessageImpl? ->
                plugin.messagingService.messenger.sendOutgoingMessage(
                    message!!
                )
            }
        )
        consumer.registerInternalConsumer(
            ClaimRequestImpl::class.java, MessageConsumer<ClaimRequestImpl> { request: ClaimRequestImpl ->
                request.respond()
                    .thenAccept(Consumer<ClaimMessage.Response> { response: ClaimMessage.Response? ->
                        plugin.messagingService.messenger.sendOutgoingMessage(
                            response!!
                        )
                    })
            }
        )
    }
}