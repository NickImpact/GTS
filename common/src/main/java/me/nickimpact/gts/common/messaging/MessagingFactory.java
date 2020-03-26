package me.nickimpact.gts.common.messaging;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.nickimpact.gts.api.messaging.IncomingMessageConsumer;
import me.nickimpact.gts.api.messaging.Messenger;
import me.nickimpact.gts.api.messaging.MessengerProvider;
import me.nickimpact.gts.common.config.updated.ConfigKeys;
import me.nickimpact.gts.common.messaging.redis.RedisMessenger;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

@RequiredArgsConstructor
public class MessagingFactory<P extends GTSPlugin> {

	@Getter(value = AccessLevel.PROTECTED)
	private final P plugin;

	public final InternalMessagingService getInstance() {
		String messageType = this.plugin.getConfiguration().get(ConfigKeys.MESSAGE_SERVICE);
		if(messageType.equalsIgnoreCase("none")) {
			return null;
		}

		this.plugin.getPluginLogger().info("Loading messaging service... [" + messageType.toUpperCase() + "]");
		InternalMessagingService service = getServiceFor(messageType);
		if(service != null) {
			return service;
		}

		this.plugin.getPluginLogger().error("Messaging service '" + messageType + "' not recognised");
		this.plugin.getPluginLogger().error("The messaging service will be disabled");
		return null;
	}

	protected InternalMessagingService getServiceFor(String messageType) {
		if(messageType.equalsIgnoreCase("redis")) {
			if(this.plugin.getConfiguration().get(ConfigKeys.REDIS_ENABLED)) {
				try {
					return new GTSMessagingService(this.plugin, new RedisMessengerProvider());
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				this.plugin.getPluginLogger().warn("Messaging Service was set to redis, but redis is not enabled!");
			}
		}

		return null;
	}

	private class RedisMessengerProvider implements MessengerProvider {

		@Override
		public @NonNull String getName() {
			return "Redis";
		}

		@Override
		public @NonNull Messenger obtain(@NonNull IncomingMessageConsumer incomingMessageConsumer) {
			RedisMessenger redis = new RedisMessenger(getPlugin(), incomingMessageConsumer);
			redis.init(getPlugin().getConfiguration().get(ConfigKeys.REDIS_ADDRESS), getPlugin().getConfiguration().get(ConfigKeys.REDIS_PASSWORD));
			return redis;
		}

	}
}
