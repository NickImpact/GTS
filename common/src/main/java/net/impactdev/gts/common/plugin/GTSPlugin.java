package net.impactdev.gts.common.plugin;

import com.google.gson.Gson;
import net.impactdev.gts.api.environment.Environment;
import net.impactdev.gts.common.config.ConfigProvider;
import net.impactdev.gts.common.messaging.InternalMessagingService;
import net.impactdev.gts.common.plugin.bootstrap.GTSBootstrap;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.plugin.ImpactorPlugin;
import net.impactdev.gts.api.extensions.ExtensionManager;
import net.impactdev.gts.api.storage.GTSStorage;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface GTSPlugin extends ImpactorPlugin {

	static GTSPlugin instance() {
		return Impactor.getInstance().getRegistry().get(GTSPlugin.class);
	}

	<T extends GTSPlugin> T as(Class<T> type);

	/**
	 * Gets the bootstrapper that created this plugin instance.
	 *
	 * @return The bootstrapper responsible for loading the plugin.
	 */
	GTSBootstrap bootstrap();

	/**
	 * A helpful utility for providing configuration links used by the plugin.
	 * Outside providing a path to the language configuration, the provider
	 * additionally provides a cached version of the main configuration without
	 * the need of processing through {@link #config()}, where the config is
	 * optionally present.
	 *
	 * @return A utility class meant to help provide cached instances of the
	 * configuration used by GTS.
	 */
	ConfigProvider configuration();

	/**
	 * Fetches information regarding the environment the plugin is presently running on. This
	 * should contain information regarding the server type and branding, as well as version information
	 * GTS will care about.
	 *
	 * @return The environment respective to the current platform
	 */
	Environment environment();

	/**
	 *
	 * @return
	 */
	Gson gson();

	/**
	 *
	 * @return
	 */
	GTSStorage storage();

	ExtensionManager extensionManager();

	InternalMessagingService messagingService();

	CompletableFuture<String> playerDisplayName(UUID id);

	default InputStream resource(String path) {
		return this.getClass().getClassLoader().getResourceAsStream(path);
	}
}
