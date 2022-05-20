package net.impactdev.gts.common.messaging;

import net.impactdev.gts.api.communication.IncomingMessageConsumer;
import net.impactdev.gts.api.communication.Messenger;
import net.impactdev.gts.api.communication.MessengerProvider;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.messaging.redis.RedisMessenger;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.impactor.api.storage.StorageType;
import net.impactdev.impactor.api.utilities.printing.PrettyPrinter;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.LinkedHashSet;
import java.util.Set;

public abstract class MessagingFactory<P extends GTSPlugin> {

	private final P plugin;
	private static final Set<StorageType> multiServerCompatible = new LinkedHashSet<>();
	static {
		multiServerCompatible.add(StorageType.MYSQL);
		multiServerCompatible.add(StorageType.MARIADB);
		multiServerCompatible.add(StorageType.POSTGRESQL);
		multiServerCompatible.add(StorageType.MONGODB);
	}

	public MessagingFactory(P plugin) {
		this.plugin = plugin;
	}

	protected final P getPlugin() {
		return this.plugin;
	}

	public final InternalMessagingService getInstance() {
		String messageType = this.plugin.configuration().main().get(ConfigKeys.MESSAGE_SERVICE);

		boolean fallback = false;
		if(messageType.equalsIgnoreCase("none")) {
			if(this.plugin.configuration().main().get(ConfigKeys.USE_MULTI_SERVER)) {
				this.plugin.logger().warn("Multi Server Mode requires a messaging service other than none!");
				this.plugin.logger().warn("Defaulting to Single Server Mode...");
			}
			fallback = true;
		}

		if(!fallback && this.plugin.configuration().main().get(ConfigKeys.USE_MULTI_SERVER)) {
			this.plugin.logger().info("Loading messaging service... [" + messageType.toUpperCase() + "]");

			if(!multiServerCompatible.contains(this.plugin.configuration().main().get(ConfigKeys.STORAGE_METHOD))) {
				new PrettyPrinter(80)
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
						.log(GTSPlugin.instance().logger(), PrettyPrinter.Level.ERROR);
				throw new IllegalStateException("Invalid messaging/storage configuration");
			}
		} else {
			this.plugin.logger().info("Loading messaging service... [Single Server Mode]");
		}

		InternalMessagingService service = this.getServiceFor(messageType);
		if(service != null) {
			return service;
		}

		this.plugin.logger().error("Messaging service '" + messageType + "' not recognised");
		this.plugin.logger().error("The messaging service will be disabled");
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
			redis.init(MessagingFactory.this.getPlugin().configuration().main().get(ConfigKeys.REDIS_ADDRESS), MessagingFactory.this.getPlugin().configuration().main().get(ConfigKeys.REDIS_PASSWORD));
			return redis;
		}

	}
}
