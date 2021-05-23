package net.impactdev.gts.bungee.messaging.interpreters

import com.google.gson.JsonElement
import net.impactdev.gts.api.messaging.message.MessageConsumer
import net.impactdev.gts.api.messaging.message.type.auctions.AuctionMessage
import net.impactdev.gts.common.messaging.interpreters.Interpreter
import net.impactdev.gts.common.messaging.messages.listings.auctions.impl.AuctionBidMessage
import net.impactdev.gts.common.messaging.messages.listings.auctions.impl.AuctionCancelMessage
import net.impactdev.gts.common.plugin.GTSPlugin
import java.util.*
import java.util.function.Consumer

class BungeeAuctionInterpreter : Interpreter {
    override fun register(plugin: GTSPlugin) {
        getDecoders(plugin)
        getInterpreters(plugin)
    }

    override fun getDecoders(plugin: GTSPlugin) {
        plugin.messagingService.registerDecoder(
            AuctionBidMessage.Request.TYPE
        ) { element: JsonElement?, id: UUID? -> AuctionBidMessage.Request.decode(element, id) }
        plugin.messagingService.registerDecoder(
            AuctionCancelMessage.Request.TYPE
        ) { element: JsonElement?, id: UUID? -> AuctionCancelMessage.Request.decode(element, id) }
    }

    override fun getInterpreters(plugin: GTSPlugin) {
        val consumer = plugin.messagingService.messenger.messageConsumer
        consumer!!.registerInternalConsumer(
            AuctionBidMessage.Request::class.java,
            MessageConsumer<AuctionBidMessage.Request> { request: AuctionBidMessage.Request ->
                request.respond()
                    .thenAccept(Consumer<AuctionMessage.Bid.Response> { response: AuctionMessage.Bid.Response? ->
                        plugin.messagingService.messenger.sendOutgoingMessage(
                            response!!
                        )
                    })
            }
        )
        consumer.registerInternalConsumer(
            AuctionCancelMessage.Request::class.java,
            MessageConsumer<AuctionCancelMessage.Request> { request: AuctionCancelMessage.Request ->
                request.respond()
                    .thenAccept(Consumer<AuctionMessage.Cancel.Response> { response: AuctionMessage.Cancel.Response? ->
                        plugin.messagingService.messenger.sendOutgoingMessage(
                            response!!
                        )
                    })
            }
        )
    }
}