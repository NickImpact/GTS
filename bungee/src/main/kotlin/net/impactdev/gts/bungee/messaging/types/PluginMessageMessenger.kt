package net.impactdev.gts.bungee.messaging.types

import com.google.common.io.ByteStreams
import net.impactdev.gts.api.messaging.IncomingMessageConsumer
import net.impactdev.gts.api.messaging.Messenger
import net.impactdev.gts.api.messaging.message.OutgoingMessage
import net.impactdev.gts.bungee.GTSBungeePlugin
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.PluginMessageEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import java.util.function.Consumer

class PluginMessageMessenger(
    private val plugin: GTSBungeePlugin?,
    override val messageConsumer: IncomingMessageConsumer
) : Messenger, Listener {
    fun init() {
        val proxy = plugin!!.bootstrap.proxy
        proxy.pluginManager.registerListener(plugin.bootstrap, this)
        proxy.registerChannel(CHANNEL)
    }

    override fun close() {
        val proxy = plugin!!.bootstrap.proxy
        proxy.unregisterChannel(CHANNEL)
        proxy.pluginManager.unregisterListener(this)
    }

    private fun dispatch(message: ByteArray) {
        plugin!!.bootstrap.proxy.servers.values.forEach(Consumer { server: ServerInfo ->
            server.sendData(
                CHANNEL,
                message,
                false
            )
        })
    }

    override fun sendOutgoingMessage(outgoingMessage: OutgoingMessage) {
        val out = ByteStreams.newDataOutput()
        out.writeUTF(outgoingMessage.asEncodedString())
        val message = out.toByteArray()
        dispatch(message)
    }

    @EventHandler
    fun onPluginMessage(event: PluginMessageEvent) {
        if (event.tag != CHANNEL) {
            return
        }
        event.isCancelled = true
        if (event.sender is ProxiedPlayer) {
            return
        }
        val data = event.data
        val `in` = ByteStreams.newDataInput(data)
        val msg = `in`.readUTF()
        try {
            messageConsumer.consumeIncomingMessageAsString(msg)
        } catch (e: Exception) {
        }
    }

    companion object {
        private const val CHANNEL = "gts:update"
    }
}