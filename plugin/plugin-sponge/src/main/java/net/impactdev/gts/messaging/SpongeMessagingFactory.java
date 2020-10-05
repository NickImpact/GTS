package net.impactdev.gts.messaging;

import net.impactdev.gts.GTSSpongePlugin;
import net.impactdev.gts.api.messaging.IncomingMessageConsumer;
import net.impactdev.gts.api.messaging.Messenger;
import net.impactdev.gts.api.messaging.MessengerProvider;
import net.impactdev.gts.common.config.updated.ConfigKeys;
import net.impactdev.gts.common.messaging.GTSMessagingService;
import net.impactdev.gts.common.messaging.InternalMessagingService;
import net.impactdev.gts.common.messaging.MessagingFactory;
import net.impactdev.gts.messaging.processor.SpongeIncomingMessageConsumer;
import net.impactdev.gts.messaging.types.PluginMessageMessenger;
import org.checkerframework.checker.nullness.qual.NonNull;

public class SpongeMessagingFactory extends MessagingFactory<GTSSpongePlugin> {

	public SpongeMessagingFactory(GTSSpongePlugin plugin) {
		super(plugin);
	}

	@Override
	protected InternalMessagingService getServiceFor(String messageType) {
		if (messageType.equals("pluginmsg") || messageType.equals("bungee") || messageType.equals("velocity")) {
			try {
				return new GTSMessagingService(this.getPlugin(), new PluginMessageMessengerProvider(), new SpongeIncomingMessageConsumer(this.getPlugin()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if(messageType.equalsIgnoreCase("redis")) {
			if(this.getPlugin().getConfiguration().get(ConfigKeys.REDIS_ENABLED)) {
				try {
					return new GTSMessagingService(this.getPlugin(), new RedisMessengerProvider(), new SpongeIncomingMessageConsumer(this.getPlugin()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				this.getPlugin().getPluginLogger().warn("Messaging Service was set to redis, but redis is not enabled!");
			}
		}

		return null;
	}

	private class PluginMessageMessengerProvider implements MessengerProvider {

		@Override
		public @NonNull String getName() {
			return "PluginMessage";
		}

		@Override
		public @NonNull Messenger obtain(@NonNull IncomingMessageConsumer incomingMessageConsumer) {
			PluginMessageMessenger messenger = new PluginMessageMessenger(SpongeMessagingFactory.this.getPlugin(), incomingMessageConsumer);
			messenger.init();
			return messenger;
		}
	}

}
