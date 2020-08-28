package me.nickimpact.gts.common.plugin.bootstrap;

import com.nickimpact.impactor.api.logging.Logger;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Represents the bootstrap plugin interface.
 *
 * <p>Instances of this interface are responsible for loading the GTS plugin on their respective platforms,
 * and provide the plugin with its essential information for it to perform as intended.</p>
 */
public interface GTSBootstrap {

	Logger getPluginLogger();

	Path getDataDirectory();

	Path getConfigDirectory();

	InputStream getResourceStream(String path);

	/**
	 * States whether or not GTS encountered an error during startup that prevented the plugin from initializing
	 * properly. This will be checked during the server started phase or some equivalent, and if an error was present
	 * during the rest of startup, then this will announce such.
	 *
	 * @return Any error that occurred during startup, or empty if no error was encountered
	 */
	Optional<Throwable> getLaunchError();

	/**
	 * Disables the plugin entirely.
	 */
	void disable();

}
