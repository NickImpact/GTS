package net.impactdev.gts.common.messaging.redis;

import net.impactdev.impactor.api.Impactor;
import net.impactdev.gts.api.communication.IncomingMessageConsumer;
import net.impactdev.gts.api.communication.Messenger;
import net.impactdev.gts.api.communication.message.OutgoingMessage;
import org.checkerframework.checker.nullness.qual.NonNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

public class RedisMessenger implements Messenger {

	private static final String CHANNEL = "gts:update";

	private final IncomingMessageConsumer consumer;

	public RedisMessenger(IncomingMessageConsumer consumer) {
		this.consumer = consumer;
	}

	private JedisPool jedisPool;
	private Subscription sub;

	public void init(String address, String password) {
		String[] addressSplit = address.split(":");
		String host = addressSplit[0];
		int port = addressSplit.length > 1 ? Integer.parseInt(addressSplit[1]) : 6379;

		if(password.equals("")) {
			this.jedisPool = new JedisPool(new JedisPoolConfig(), host, port);
		} else {
			this.jedisPool = new JedisPool(new JedisPoolConfig(), host, port, 0, password);
		}

		Impactor.getInstance().getScheduler().executeAsync(() -> {
			this.sub = new Subscription(this);
			try(Jedis jedis = this.jedisPool.getResource()) {
				jedis.subscribe(this.sub, CHANNEL);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public IncomingMessageConsumer getMessageConsumer() {
		return this.consumer;
	}

	@Override
	public void sendOutgoingMessage(@NonNull OutgoingMessage outgoingMessage) {
		try (Jedis jedis = this.jedisPool.getResource()) {
			jedis.publish(CHANNEL, outgoingMessage.asEncodedString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		this.sub.unsubscribe();
		this.jedisPool.destroy();
	}

	private static class Subscription extends JedisPubSub {
		private final RedisMessenger parent;

		public Subscription(RedisMessenger parent) {
			this.parent = parent;
		}

		@Override
		public void onMessage(String channel, String msg) {
			if(!channel.equals(CHANNEL)) {
				return;
			}

			this.parent.consumer.consumeIncomingMessageAsString(msg);
		}
	}
}
