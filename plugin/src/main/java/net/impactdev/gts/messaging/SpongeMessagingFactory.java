package net.impactdev.gts.messaging;

import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.messaging.types.PluginMessageMessenger;
import net.impactdev.gts.messaging.types.SpongeSingleServerModeMessenger;
import net.impactdev.gts.api.messaging.IncomingMessageConsumer;
import net.impactdev.gts.api.messaging.Messenger;
import net.impactdev.gts.api.messaging.MessengerProvider;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.messaging.GTSMessagingService;
import net.impactdev.gts.common.messaging.InternalMessagingService;
import net.impactdev.gts.common.messaging.MessagingFactory;
import net.impactdev.gts.messaging.processor.SpongeIncomingMessageConsumer;
import org.checkerframework.checker.nullness.qual.NonNull;

public class SpongeMessagingFactory extends MessagingFactory<GTSPlugin> {

	public SpongeMessagingFactory(GTSPlugin plugin) {
		super(plugin);
	}

	@Override
	protected InternalMessagingService getServiceFor(String messageType) {
		if(this.getPlugin().configuration().main().get(ConfigKeys.USE_MULTI_SERVER)) {
			if (messageType.equals("pluginmsg") || messageType.equals("bungee") || messageType.equals("velocity")) {
				return new GTSMessagingService(this.getPlugin(), new PluginMessageMessengerProvider(), new SpongeIncomingMessageConsumer(this.getPlugin()));
			}

			if (messageType.equalsIgnoreCase("redis")) {
				if (this.getPlugin().configuration().main().get(ConfigKeys.REDIS_ENABLED)) {
					return new GTSMessagingService(this.getPlugin(), new RedisMessengerProvider(), new SpongeIncomingMessageConsumer(this.getPlugin()));
				} else {
					this.getPlugin().logger().warn("Messaging Service was set to redis, but redis is not enabled!");
				}
			}
		}

		return new GTSMessagingService(this.getPlugin(), new SingleServerMessengerProvider(), new SpongeIncomingMessageConsumer(this.getPlugin()));
	}

	private class PluginMessageMessengerProvider implements MessengerProvider {

		@Override
		public @NonNull String getName() {
			return "Plugin Messenger";
		}

		@Override
		public @NonNull Messenger obtain(@NonNull IncomingMessageConsumer incomingMessageConsumer) {
			PluginMessageMessenger messenger = new PluginMessageMessenger(SpongeMessagingFactory.this.getPlugin(), incomingMessageConsumer);
			messenger.init();
			return messenger;
		}
	}

	public static class SingleServerMessengerProvider implements MessengerProvider {

		@Override
		public @NonNull String getName() {
			return "Sponge Single Server Mode";
		}

		@Override
		public @NonNull Messenger obtain(@NonNull IncomingMessageConsumer incomingMessageConsumer) {
			return new SpongeSingleServerModeMessenger(incomingMessageConsumer);
		}
	}
}
