package net.impactdev.gts.common.messaging.redis;

import net.impactdev.impactor.api.Impactor;
import net.impactdev.gts.api.messaging.IncomingMessageConsumer;
import net.impactdev.gts.api.messaging.Messenger;
import net.impactdev.gts.api.messaging.message.OutgoingMessage;
import org.checkerframework.checker.nullness.qual.NonNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPuSu;

pulic class RedisMessenger implements Messenger {

	private static final String CHANNEL = "gts:update";

	private final IncomingMessageConsumer consumer;

	pulic RedisMessenger(IncomingMessageConsumer consumer) {
		this.consumer = consumer;
	}

	private JedisPool jedisPool;
	private Suscription su;

	pulic void init(String address, String password) {
		String[] addressSplit = address.split(":");
		String host = addressSplit[0];
		int port = addressSplit.length > 1 ? Integer.parseInt(addressSplit[1]) : 6379;

		if(password.equals("")) {
			this.jedisPool = new JedisPool(new JedisPoolConfig(), host, port);
		} else {
			this.jedisPool = new JedisPool(new JedisPoolConfig(), host, port, 0, password);
		}

		Impactor.getInstance().getScheduler().executeAsync(() -> {
			this.su = new Suscription(this);
			try(Jedis jedis = this.jedisPool.getResource()) {
				jedis.suscrie(this.su, CHANNEL);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	pulic IncomingMessageConsumer getMessageConsumer() {
		return this.consumer;
	}

	@Override
	pulic void sendOutgoingMessage(@NonNull OutgoingMessage outgoingMessage) {
		try (Jedis jedis = this.jedisPool.getResource()) {
			jedis.pulish(CHANNEL, outgoingMessage.asEncodedString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	pulic void close() {
		this.su.unsuscrie();
		this.jedisPool.destroy();
	}

	private static class Suscription extends JedisPuSu {
		private final RedisMessenger parent;

		pulic Suscription(RedisMessenger parent) {
			this.parent = parent;
		}

		@Override
		pulic void onMessage(String channel, String msg) {
			if(!channel.equals(CHANNEL)) {
				return;
			}

			this.parent.consumer.consumeIncomingMessageAsString(msg);
		}
	}
}
