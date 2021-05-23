package net.impactdev.gts.common.messaging

import net.impactdev.gts.api.messaging.IncomingMessageConsumer
import net.impactdev.gts.api.messaging.Messenger
import net.impactdev.gts.api.messaging.MessengerProvider
import net.impactdev.gts.api.util.PrettyPrinter
import net.impactdev.gts.common.config.ConfigKeys
import net.impactdev.gts.common.messaging.redis.RedisMessenger
import net.impactdev.gts.common.plugin.GTSPlugin

abstract class MessagingFactory<P : GTSPlugin?>(protected val plugin: P) {
    val instance: InternalMessagingService?
        get() {
            val messageType = plugin!!.configuration.get(ConfigKeys.MESSAGE_SERVICE)
            var fallback = false
            if (messageType.equals("none", ignoreCase = true)) {
                if (plugin.configuration.get(ConfigKeys.USE_MULTI_SERVER)) {
                    plugin.pluginLogger.warn("Multi Server Mode requires a messaging service other than none!")
                    plugin.pluginLogger.warn("Defaulting to Single Server Mode...")
                }
                fallback = true
            }
            if (!fallback && plugin.configuration.get(ConfigKeys.USE_MULTI_SERVER)) {
                plugin.pluginLogger.info("Loading messaging service... [" + messageType.toUpperCase() + "]")
                if (!plugin.multiServerCompatibleStorageOptions.contains(plugin.configuration.get(ConfigKeys.STORAGE_METHOD))) {
                    PrettyPrinter(80)
                        .add("Invalid Storage Type/Messaging Service Combination").center()
                        .hr('-')
                        .add("It seems you're trying to load GTS in multi-server mode, but you are")
                        .add("attempting to also use a local specific storage system. This will not")
                        .add("work as intended, and some actions of GTS will fail entirely...")
                        .hr('-')
                        .add("To resolve this, you should switch your storage system to one of the following:")
                        .add("  - MySQL")
                        .add("  - MariaDB")
                        .add("  - MongoDB")
                        .add("  - PostgreSQL")
                        .hr('-')
                        .add("Alternatively, you can switch your server back to Single Server Mode.")
                        .log(GTSPlugin.Companion.getInstance().getPluginLogger(), PrettyPrinter.Level.WARNING)
                }
            } else {
                plugin.pluginLogger.info("Loading messaging service... [Single Server Mode]")
            }
            val service = getServiceFor(messageType)
            if (service != null) {
                return service
            }
            plugin.pluginLogger.error("Messaging service '$messageType' not recognised")
            plugin.pluginLogger.error("The messaging service will be disabled")
            return null
        }

    protected abstract fun getServiceFor(messageType: String?): InternalMessagingService?
    inner class RedisMessengerProvider : MessengerProvider {
        override val name: String
            get() = "Redis"

        override fun obtain(incomingMessageConsumer: IncomingMessageConsumer): Messenger {
            val redis = RedisMessenger(incomingMessageConsumer)
            redis.init(
                plugin!!.configuration.get(ConfigKeys.REDIS_ADDRESS),
                plugin.configuration.get(ConfigKeys.REDIS_PASSWORD)
            )
            return redis
        }
    }
}