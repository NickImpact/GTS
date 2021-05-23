package net.impactdev.gts.bungee.messaging

import net.impactdev.gts.api.messaging.IncomingMessageConsumer
import net.impactdev.gts.api.messaging.Messenger
import net.impactdev.gts.api.messaging.MessengerProvider
import net.impactdev.gts.bungee.GTSBungeePlugin
import net.impactdev.gts.bungee.messaging.processor.BungeeIncomingMessageConsumer
import net.impactdev.gts.bungee.messaging.types.PluginMessageMessenger
import net.impactdev.gts.bungee.messaging.types.RedisBungeeMessenger
import net.impactdev.gts.common.config.ConfigKeys
import net.impactdev.gts.common.messaging.GTSMessagingService
import net.impactdev.gts.common.messaging.InternalMessagingService
import net.impactdev.gts.common.messaging.MessagingFactory

class BungeeMessagingFactory(plugin: GTSBungeePlugin?) : MessagingFactory<GTSBungeePlugin?>(plugin) {
    override fun getServiceFor(messageType: String): InternalMessagingService {
        if (messageType.equals("pluginmsg", ignoreCase = true) || messageType.equals("bungee", ignoreCase = true)) {
            try {
                return GTSMessagingService(
                    plugin, PluginMessageMessengerProvider(), BungeeIncomingMessageConsumer(
                        plugin
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (messageType.equals("redisbungee", ignoreCase = true)) {
            if (plugin!!.bootstrap.proxy.pluginManager.getPlugin("RedisBungee") == null) {
                plugin!!.pluginLogger.warn("RedisBungee plugin is not present")
            } else {
                try {
                    return GTSMessagingService(
                        plugin, RedisBungeeMessengerProvider(), BungeeIncomingMessageConsumer(
                            plugin
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        if (messageType.equals("redis", ignoreCase = true)) {
            if (plugin!!.configuration.get(ConfigKeys.REDIS_ENABLED)) {
                try {
                    return GTSMessagingService(plugin, RedisMessengerProvider(), BungeeIncomingMessageConsumer(plugin))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                plugin!!.pluginLogger.warn("Messaging Service was set to redis, but redis is not enabled!")
            }
        }
        return null
    }

    private inner class PluginMessageMessengerProvider : MessengerProvider {
        override val name: String
            get() = "PluginMessage"

        override fun obtain(incomingMessageConsumer: IncomingMessageConsumer): Messenger {
            val messenger = PluginMessageMessenger(plugin, incomingMessageConsumer)
            messenger.init()
            return messenger
        }
    }

    private inner class RedisBungeeMessengerProvider : MessengerProvider {
        override val name: String
            get() = "RedisBungee"

        override fun obtain(incomingMessageConsumer: IncomingMessageConsumer): Messenger {
            val messenger = RedisBungeeMessenger(plugin, incomingMessageConsumer)
            messenger.init()
            return messenger
        }
    }
}