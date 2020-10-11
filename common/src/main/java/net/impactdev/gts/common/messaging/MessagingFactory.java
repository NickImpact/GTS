package net.impactdev.gts.common.messaging;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.impactdev.gts.api.messaging.IncomingMessageConsumer;
import net.impactdev.gts.api.messaging.Messenger;
import net.impactdev.gts.api.messaging.MessengerProvider;
import net.impactdev.gts.common.config.updated.ConfigKeys;
import net.impactdev.gts.common.messaging.redis.RedisMessenger;
import net.impactdev.gts.common.plugin.GTSPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

@RequiredArgsConstructor
public abstract class MessagingFactory<P extends GTSPlugin> {

	@Getter(value = AccessLevel.PROTECTED)
	private final P plugin;

	public final InternalMessagingService getInstance() {
		String messageType = this.plugin.getConfiguration().get(ConfigKeys.MESSAGE_SERVICE);
		if(messageType.equalsIgnoreCase("none")) {
			return null;
		}

		if(this.plugin.getConfiguration().get(ConfigKeys.USE_MULTI_SERVER)) {
			this.plugin.getPluginLogger().info("Loading messaging service... [" + messageType.toUpperCase() + "]");
		} else {
			this.plugin.getPluginLogger().info("Loading messaging service... [Single Server Mode]");
		}
		InternalMessagingService service = this.getServiceFor(messageType);
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
			RedisMessenger redis = new RedisMessenger(incomingMessageConsumer);
			redis.init(MessagingFactory.this.getPlugin().getConfiguration().get(ConfigKeys.REDIS_ADDRESS), MessagingFactory.this.getPlugin().getConfiguration().get(ConfigKeys.REDIS_PASSWORD));
			return redis;
		}

	}
}
