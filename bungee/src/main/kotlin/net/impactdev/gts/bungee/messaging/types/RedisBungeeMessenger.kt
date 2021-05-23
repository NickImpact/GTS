package net.impactdev.gts.bungee.messaging.types

import com.imaginarycode.minecraft.redisbungee.RedisBungee
import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI
import com.imaginarycode.minecraft.redisbungee.events.PubSubMessageEvent
import net.impactdev.gts.api.messaging.IncomingMessageConsumer
import net.impactdev.gts.api.messaging.Messenger
import net.impactdev.gts.api.messaging.message.OutgoingMessage
import net.impactdev.gts.bungee.GTSBungeePlugin
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

class RedisBungeeMessenger(
    private val plugin: GTSBungeePlugin?,
    override val messageConsumer: IncomingMessageConsumer
) : Messenger, Listener {
    private var redisBungee: RedisBungeeAPI? = null
    fun init() {
        redisBungee = RedisBungee.getApi()
        redisBungee.registerPubSubChannels(CHANNEL)
        plugin!!.bootstrap.proxy.pluginManager.registerListener(plugin.bootstrap, this)
    }

    override fun close() {
        redisBungee!!.unregisterPubSubChannels(CHANNEL)
        redisBungee = null
        plugin!!.bootstrap.proxy.pluginManager.unregisterListener(this)
    }

    override fun sendOutgoingMessage(outgoingMessage: OutgoingMessage) {
        redisBungee!!.sendChannelMessage(CHANNEL, outgoingMessage.asEncodedString())
    }

    @EventHandler
    fun onMessage(event: PubSubMessageEvent) {
        if (event.channel != CHANNEL) {
            return
        }
        messageConsumer.consumeIncomingMessageAsString(event.message)
    }

    companion object {
        private const val CHANNEL = "gts:update"
    }
}