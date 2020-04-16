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
public abstract class MessagingFactory<P extends GTSPlugin> {

	@Getter(value = AccessLevel.PROTECTED)
	private final P plugin;

	public final InternalMessagingService getInstance() {
		String messageType = "pluginmsg";//this.plugin.getConfiguration().get(ConfigKeys.MESSAGE_SERVICE);
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

	protected abstract InternalMessagingService getServiceFor(String messageType);

	public class RedisMessengerProvider implements MessengerProvider {

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
