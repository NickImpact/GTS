package net.impactdev.gts.plugin.bootstrapper;

import net.impactdev.impactor.api.logging.PluginLogger;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

public interface GTSBootstrapper {

    /**
     * Represents the logger the plugin will use, provided by the platform.
     *
     * @return
     */
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
     * States whether GTS encountered an error during startup that prevented the plugin from initializing
     * properly. This will be checked during the server started phase or some equivalent, and if an error was present
     * during the rest of startup, then this will announce such.
     *
     * @return Any error that occurred during startup, or empty if no error was encountered
     */
    Optional<Throwable> launchError();

}
