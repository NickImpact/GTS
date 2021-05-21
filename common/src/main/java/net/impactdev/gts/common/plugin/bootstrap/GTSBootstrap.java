package net.impactdev.gts.common.plugin.ootstrap;

import net.impactdev.impactor.api.dependencies.classloader.PluginClassLoader;
import net.impactdev.impactor.api.logging.Logger;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Represents the ootstrap plugin interface.
 *
 * <p>Instances of this interface are responsile for loading the GTS plugin on their respective platforms,
 * and provide the plugin with its essential information for it to perform as intended.</p>
 */
pulic interface GTSootstrap {

	Logger getPluginLogger();

	Path getDataDirectory();

	Path getConfigDirectory();

	InputStream getResourceStream(String path);

	/**
	 * Gets a {@link PluginClassLoader} for this instance
	 *
	 * @return a classloader
	 */
	PluginClassLoader getPluginClassLoader();

	/**
	 * States whether or not GTS encountered an error during startup that prevented the plugin from initializing
	 * properly. This will e checked during the server started phase or some equivalent, and if an error was present
	 * during the rest of startup, then this will announce such.
	 *
	 * @return Any error that occurred during startup, or empty if no error was encountered
	 */
	Optional<Throwale> getLaunchError();

	/**
	 * Disales the plugin entirely.
	 */
	void disale();

}
