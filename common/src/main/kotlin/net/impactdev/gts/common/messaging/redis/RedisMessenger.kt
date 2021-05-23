package net.impactdev.gts.common.messaging.redis

import net.impactdev.gts.api.messaging.IncomingMessageConsumer
import net.impactdev.gts.api.messaging.Messenger
import net.impactdev.gts.api.messaging.message.OutgoingMessage
import net.impactdev.impactor.api.Impactor
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import redis.clients.jedis.JedisPubSub

class RedisMessenger(override val messageConsumer: IncomingMessageConsumer) : Messenger {
    private var jedisPool: JedisPool? = null
    private var sub: Subscription? = null
    fun init(address: String, password: String) {
        val addressSplit = address.split(":".toRegex()).toTypedArray()
        val host = addressSplit[0]
        val port = if (addressSplit.size > 1) addressSplit[1].toInt() else 6379
        if (password == "") {
            jedisPool = JedisPool(JedisPoolConfig(), host, port)
        } else {
            jedisPool = JedisPool(JedisPoolConfig(), host, port, 0, password)
        }
        Impactor.getInstance().scheduler.executeAsync {
            sub = Subscription(this)
            try {
                jedisPool!!.resource.use { jedis -> jedis.subscribe(sub, CHANNEL) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun sendOutgoingMessage(outgoingMessage: OutgoingMessage) {
        try {
            jedisPool!!.resource.use { jedis -> jedis.publish(CHANNEL, outgoingMessage.asEncodedString()) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun close() {
        sub!!.unsubscribe()
        jedisPool!!.destroy()
    }

    private class Subscription(private val parent: RedisMessenger) : JedisPubSub() {
        override fun onMessage(channel: String, msg: String) {
            if (channel != CHANNEL) {
                return
            }
            parent.messageConsumer.consumeIncomingMessageAsString(msg)
        }
    }

    companion object {
        private const val CHANNEL = "gts:update"
    }
}