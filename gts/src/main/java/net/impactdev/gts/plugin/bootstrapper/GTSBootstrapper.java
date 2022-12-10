package net.impactdev.gts.plugin.bootstrapper;

import net.impactdev.impactor.api.logging.PluginLogger;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

public abstract class GTSBootstrapper {

    private final PluginLogger logger;
    private final Path configDir;
    private final Path dataDir;

    private Throwable exception;

    public GTSBootstrapper(PluginLogger logger, Path configDir, Path dataDir) {
        this.logger = logger;
        this.configDir = configDir;
        this.dataDir = dataDir;
    }

    public PluginLogger logger() {
        return this.logger;
    }

    public Path configDir() {
        return this.configDir;
    }

    public Path dataDir() {
        return this.dataDir;
    }

    /**
     * Attempts to locate a resource within the internal jar, and if it exists, creates an
     * InputStream for said resource.
     *
     * @param path The path to the resource
     * @return An optionally filled InputStream if the file was found and loaded, empty otherwise.
     */
    protected Optional<InputStream> resource(Path path) {
        return Optional.ofNullable(this.getClass().getResourceAsStream(path.toString()));
    }

    protected void logLaunchFailure(Throwable exception) {
        this.exception = exception;
    }

    /**
     * States whether GTS encountered an error during startup that prevented the plugin from initializing
     * properly. This will be checked during the server started phase or some equivalent, and if an error was present
     * during the rest of startup, then this will announce such.
     *
     * @return Any error that occurred during startup, or empty if no error was encountered
     */
    protected Optional<Throwable> launchError() {
        return Optional.ofNullable(this.exception);
    }

}
