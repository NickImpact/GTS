package net.impactdev.gts.bungee.messaging.interpreters

import com.google.gson.JsonElement
import net.impactdev.gts.api.messaging.message.MessageConsumer
import net.impactdev.gts.api.messaging.message.type.listings.BuyItNowMessage
import net.impactdev.gts.common.messaging.interpreters.Interpreter
import net.impactdev.gts.common.messaging.messages.listings.buyitnow.purchase.BINPurchaseMessage
import net.impactdev.gts.common.messaging.messages.listings.buyitnow.removal.BINRemoveMessage
import net.impactdev.gts.common.plugin.GTSPlugin
import java.util.*
import java.util.function.Consumer

class BungeeBINInterpreters : Interpreter {
    override fun register(plugin: GTSPlugin) {
        getDecoders(plugin)
        getInterpreters(plugin)
    }

    override fun getDecoders(plugin: GTSPlugin) {
        plugin.messagingService.registerDecoder(
            BINRemoveMessage.Request.TYPE
        ) { content: JsonElement?, id: UUID? -> BINRemoveMessage.Request.decode(content, id) }
        plugin.messagingService.registerDecoder(
            BINPurchaseMessage.Request.TYPE
        ) { content: JsonElement?, id: UUID? -> BINPurchaseMessage.Request.decode(content, id) }
    }

    override fun getInterpreters(plugin: GTSPlugin) {
        plugin.messagingService.messenger.messageConsumer!!.registerInternalConsumer(
            BINRemoveMessage.Request::class.java,
            MessageConsumer<BINRemoveMessage.Request> { request: BINRemoveMessage.Request? ->
                GTSPlugin.instance.storage
                    .processListingRemoveRequest(request)
                    .thenAccept(Consumer<BuyItNowMessage.Remove.Response> { response: BuyItNowMessage.Remove.Response? ->
                        plugin.messagingService.messenger.sendOutgoingMessage(
                            response!!
                        )
                    })
            }
        )
        plugin.messagingService.messenger.messageConsumer!!.registerInternalConsumer(
            BINPurchaseMessage.Request::class.java,
            MessageConsumer<BINPurchaseMessage.Request> { request: BINPurchaseMessage.Request? ->
                GTSPlugin.instance.storage
                    .processPurchase(request)
                    .thenAccept(Consumer<BuyItNowMessage.Purchase.Response> { response: BuyItNowMessage.Purchase.Response? ->
                        plugin.messagingService.messenger.sendOutgoingMessage(
                            response!!
                        )
                    })
            }
        )
    }
}