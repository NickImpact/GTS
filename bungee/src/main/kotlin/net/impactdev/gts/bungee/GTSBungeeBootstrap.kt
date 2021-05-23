package net.impactdev.gts.bungee

import net.impactdev.gts.common.plugin.bootstrap.GTSBootstrap
import net.impactdev.impactor.api.dependencies.classloader.PluginClassLoader
import net.impactdev.impactor.api.logging.Logger
import net.md_5.bungee.api.plugin.Plugin
import java.io.InputStream
import java.nio.file.Path
import java.util.*

class GTSBungeeBootstrap : Plugin(), GTSBootstrap {
    private val plugin: GTSBungeePlugin
    private var exception: Throwable? = null
    override fun onEnable() {
        try {
            plugin.enable()
        } catch (e: Exception) {
            exception = e
            e.printStackTrace()
        }
    }

    override fun getPluginLogger(): Logger {
        return plugin.pluginLogger
    }

    override fun getDataDirectory(): Path {
        return this.dataFolder.toPath()
    }

    override fun getConfigDirectory(): Path {
        return this.dataFolder.toPath()
    }

    override fun getResourceStream(path: String): InputStream {
        return null
    }

    override fun getPluginClassLoader(): PluginClassLoader {
        return null
    }

    override fun getLaunchError(): Optional<Throwable> {
        return Optional.empty()
    }

    override fun disable() {}

    init {
        plugin = GTSBungeePlugin(this)
    }
}