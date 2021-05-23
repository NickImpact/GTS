package net.impactdev.gts.common.plugin.bootstrap

import net.impactdev.impactor.api.dependencies.classloader.PluginClassLoader
import net.impactdev.impactor.api.logging.Logger
import java.io.InputStream
import java.nio.file.Path
import java.util.*

/**
 * Represents the bootstrap plugin interface.
 *
 *
 * Instances of this interface are responsible for loading the GTS plugin on their respective platforms,
 * and provide the plugin with its essential information for it to perform as intended.
 */
interface GTSBootstrap {
    val pluginLogger: Logger?
    val dataDirectory: Path?
    val configDirectory: Path?
    fun getResourceStream(path: String?): InputStream?

    /**
     * Gets a [PluginClassLoader] for this instance
     *
     * @return a classloader
     */
    val pluginClassLoader: PluginClassLoader

    /**
     * States whether or not GTS encountered an error during startup that prevented the plugin from initializing
     * properly. This will be checked during the server started phase or some equivalent, and if an error was present
     * during the rest of startup, then this will announce such.
     *
     * @return Any error that occurred during startup, or empty if no error was encountered
     */
    val launchError: Optional<Throwable?>?

    /**
     * Disables the plugin entirely.
     */
    fun disable()
}