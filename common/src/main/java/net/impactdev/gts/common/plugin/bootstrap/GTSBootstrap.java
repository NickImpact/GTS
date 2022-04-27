package net.impactdev.gts.common.plugin.bootstrap;

import net.impactdev.impactor.api.logging.PluginLogger;
import net.impactdev.impactor.launcher.LauncherBootstrap;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Represents the bootstrap plugin interface.
 *
 * <p>Instances of this interface are responsible for loading the GTS plugin on their respective platforms,
 * and provide the plugin with its essential information for it to perform as intended.</p>
 */
public interface GTSBootstrap extends LauncherBootstrap {

	PluginLogger logger();

	Path configDirectory();

	Path dataDirectory();

	/**
	 * Attempts to locate a resource within the internal jar, and if it exists, creates an
	 * InputStream for said resource.
	 *
	 * @param path The path to the resource
	 * @return An optionally filled InputStream if the file was found and loaded, empty otherwise.
	 */
	Optional<InputStream> resource(Path path);

	/**
	 * States whether or not GTS encountered an error during startup that prevented the plugin from initializing
	 * properly. This will be checked during the server started phase or some equivalent, and if an error was present
	 * during the rest of startup, then this will announce such.
	 *
	 * @return Any error that occurred during startup, or empty if no error was encountered
	 */
	Optional<Throwable> launchError();

}
