/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package net.impactdev.gts.common.extension

import com.google.common.collect.Sets
import com.google.gson.JsonParser
import net.impactdev.gts.api.GTSService.Companion.instance
import net.impactdev.gts.api.events.extension.ExtensionLoadEvent
import net.impactdev.gts.api.extension.Extension
import net.impactdev.gts.api.extension.ExtensionManager
import net.impactdev.gts.common.plugin.GTSPlugin
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter
import net.impactdev.impactor.api.Impactor
import net.impactdev.impactor.api.dependencies.Dependency
import net.impactdev.impactor.api.dependencies.DependencyManager
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.reflect.InvocationTargetException
import java.nio.charset.StandardCharsets
import java.nio.file.*
import java.util.*
import java.util.jar.JarFile
import java.util.stream.Collectors

class SimpleExtensionManager(private val plugin: GTSPlugin) : ExtensionManager, AutoCloseable {
    private val extensions: MutableSet<LoadedExtension> = HashSet()
    override fun close() {
        for (extension in extensions) {
            try {
                extension.instance!!.unload()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        extensions.clear()
    }

    override fun loadExtension(extension: Extension?) {
        if (extensions.stream().anyMatch { e: LoadedExtension -> e.instance == extension }) {
            return
        }
        plugin.pluginLogger.info("Loading extension: " + extension!!.metadata.name)
        extensions.add(LoadedExtension(extension, null))
        extension.load(instance, GTSPlugin.Companion.getInstance().getConfigDir())
        Impactor.getInstance().eventBus.post(ExtensionLoadEvent::class.java, extension)
    }

    fun loadExtensions(directory: Path?) {
        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
        if (!Files.isDirectory(directory)) {
            return
        }
        val dependencies: MutableSet<Dependency> = Sets.newHashSet()
        try {
            Files.list(directory).use { stream ->
                stream.forEach { path: Path ->
                    if (path.fileName.toString().endsWith(".jar")) {
                        try {
                            val extension = this.loadExtension(path)
                            dependencies.addAll(extension.requiredDependencies)
                        } catch (e: Exception) {
                            val exception = RuntimeException("Exception loading extension from $path", e)
                            ExceptionWriter.write(exception)
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        plugin.pluginLogger.info("Initializing any additionally required dependencies...")
        Impactor.getInstance().registry.get(DependencyManager::class.java).loadDependencies(dependencies)
        for (extension in loadedExtensions) {
            try {
                plugin.pluginLogger.info("Loading extension: &a" + extension.metadata.name)
                extension.load(instance, GTSPlugin.Companion.getInstance().getConfigDir())
            } catch (e: Exception) {
                plugin.pluginLogger.error("Failed to load extension '" + extension.metadata.name + "', check error below...")
                ExceptionWriter.write(e)
            }
        }
    }

    @kotlin.Throws(IOException::class)
    override fun loadExtension(path: Path?): Extension {
        check(
            !extensions.stream()
                .anyMatch { e: LoadedExtension -> path!!.endsWith(e.path) }) { "Extension at path $path is already loaded" }
        if (!Files.exists(path)) {
            throw NoSuchFileException("No file at $path")
        }
        var className: String? = null
        JarFile(path!!.toFile()).use { jar ->
            val extensionJarEntry = jar.getJarEntry("extension.json")
                ?: throw IllegalStateException("Likely extension at path $path missing extension.json")
            jar.getInputStream(extensionJarEntry).use { `in` ->
                checkNotNull(`in`) { "Unable to read extension.json for extension at path $path" }
                BufferedReader(InputStreamReader(`in`, StandardCharsets.UTF_8)).use { reader ->
                    val parsed = JsonParser().parse(reader)
                    if (parsed.asJsonObject.has("main")) {
                        className = parsed.asJsonObject["main"].asString
                    }
                }
            }
        }
        requireNotNull(className) { "Failed to locate main class from extension.json for path: $path" }
        plugin.bootstrap.pluginClassLoader.addJarToClasspath(path)
        val extensionClass: Class<out Extension>
        extensionClass = try {
            Class.forName(className).asSubclass(Extension::class.java)
        } catch (e: ClassNotFoundException) {
            throw RuntimeException(e)
        }
        val extension: Extension
        extension = try {
            val constructor = extensionClass.getConstructor()
            constructor.newInstance()
        } catch (e: NoSuchMethodException) {
            throw RuntimeException("Unable to find valid constructor in " + extensionClass.name)
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        } catch (e: InstantiationException) {
            throw RuntimeException(e)
        } catch (e: InvocationTargetException) {
            throw RuntimeException(e)
        }
        extensions.add(LoadedExtension(extension, path))
        Impactor.getInstance().eventBus.post(ExtensionLoadEvent::class.java, extension)
        return extension
    }

    override fun enableExtensions() {
        for (extension in loadedExtensions) {
            extension.enable(instance)
            for (executor in extension.executors!!) {
                executor!!.register()
            }
        }
    }

    override val loadedExtensions: Collection<Extension>
        get() = extensions.stream().map { e: LoadedExtension -> e.instance }.collect(Collectors.toSet())

    private class LoadedExtension(val instance: Extension?, val path: Path?)
}