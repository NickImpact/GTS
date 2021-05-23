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
package net.impactdev.gts.common.storage.implementation.file

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.google.common.collect.ForwardingSet
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator
import kotlin.collections.set

class FileWatcher(private val base: Path, autoRegisterNewSubDirectories: Boolean) : AutoCloseable {
    /** The watch service  */
    private val service: WatchService

    /** A map of all registered watch keys  */
    private val keys = Collections.synchronizedMap(HashMap<WatchKey, Path>())

    /** If this file watcher should discover directories  */
    private val autoRegisterNewSubDirectories: Boolean

    /** The thread currently being used to wait for & process watch events  */
    private val processingThread = AtomicReference<Thread?>()
    private val watchedLocations: MutableMap<Path?, WatchedLocation>

    /**
     * Register a watch key in the given directory.
     *
     * @param directory the directory
     * @throws IOException if unable to register a key
     */
    @kotlin.Throws(IOException::class)
    fun register(directory: Path) {
        val key = register(service, directory)
        keys[key] = directory
    }

    /**
     * Register a watch key recursively in the given directory.
     *
     * @param root the root directory
     * @throws IOException if unable to register a key
     */
    @kotlin.Throws(IOException::class)
    fun registerRecursively(root: Path?) {
        Files.walkFileTree(root, object : SimpleFileVisitor<Path>() {
            @kotlin.Throws(IOException::class)
            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                this@FileWatcher.register(dir)
                return super.preVisitDirectory(dir, attrs)
            }
        })
    }

    /**
     * Gets a [WatchedLocation] instance for a given path.
     *
     * @param path the path to get a watcher for
     * @return the watched location
     */
    fun getWatcher(path: Path?): WatchedLocation {
        var path = path
        if (path!!.isAbsolute) {
            path = base.relativize(path)
        }
        return watchedLocations.computeIfAbsent(path) { path: Path? -> WatchedLocation(path) }
    }

    /**
     * Process an observed watch event.
     *
     * @param event the event
     * @param path the resolved event context
     */
    protected fun processEvent(event: WatchEvent<Path?>?, path: Path?) {
        val relative = base.relativize(path)
        if (relative.nameCount == 0) {
            return
        }

        // pass the event onto all watched locations that match
        for ((key, value) in watchedLocations) {
            if (relative.startsWith(key)) {
                value.onEvent(event, relative)
            }
        }
    }

    /**
     * Processes [WatchEvent]s from the watch service until it is closed, or until
     * the thread is interrupted.
     */
    fun runEventProcessingLoop() {
        check(
            processingThread.compareAndSet(
                null,
                Thread.currentThread()
            )
        ) { "A thread is already processing events for this watcher." }
        while (true) {
            // poll for a key from the watch service
            var key: WatchKey
            key = try {
                service.take()
            } catch (e: InterruptedException) {
                break
            } catch (e: ClosedWatchServiceException) {
                break
            }

            // find the directory the key is watching
            val directory = keys[key]
            if (directory == null) {
                key.cancel()
                continue
            }

            // process each watch event the key has
            for (ev in key.pollEvents()) {
                val event = ev as WatchEvent<Path?>
                val context = event.context()

                // ignore contexts with a name count of zero
                if (context == null || context.nameCount == 0) {
                    continue
                }

                // resolve the context of the event against the directory being watched
                val file = directory.resolve(context)

                // if the file is a regular file, send the event on to be processed
                if (Files.isRegularFile(file)) {
                    processEvent(event, file)
                }

                // handle recursive directory creation
                if (autoRegisterNewSubDirectories && event.kind() === StandardWatchEventKinds.ENTRY_CREATE) {
                    try {
                        if (Files.isDirectory(file, LinkOption.NOFOLLOW_LINKS)) {
                            registerRecursively(file)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }

            // reset the key
            val valid = key.reset()
            if (!valid) {
                keys.remove(key)
            }
        }
        processingThread.compareAndSet(Thread.currentThread(), null)
    }

    override fun close() {
        try {
            service.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    class WatchedLocation internal constructor(private val path: Path?) {
        private val recentlyModifiedFiles: MutableSet<String> = ExpiringSet(5, TimeUnit.SECONDS)
        private val callbacks: MutableList<Consumer<Path>> = CopyOnWriteArrayList()
        fun onEvent(event: WatchEvent<Path?>?, path: Path?) {
            val relative = this.path!!.relativize(path)
            val name = relative.toString()
            if (!recentlyModifiedFiles.add(name)) {
                return
            }
            for (callback in callbacks) {
                callback.accept(relative)
            }
        }

        fun record(filename: String) {
            recentlyModifiedFiles.add(filename)
        }

        fun addListener(listener: Consumer<Path>) {
            callbacks.add(listener)
        }
    }

    class ExpiringSet<E>(duration: Long, unit: TimeUnit?) : ForwardingSet<E>() {
        private val view: Set<E>
        override fun delegate(): Set<E> {
            return view
        }

        init {
            val cache: Cache<E, Boolean> = Caffeine.newBuilder().expireAfterAccess(duration, unit!!).build()
            view = Collections.newSetFromMap(cache.asMap())
        }
    }

    companion object {
        /**
         * Get a [WatchKey] from the given [WatchService] in the given [directory][Path].
         *
         * @param watchService the watch service
         * @param directory the directory
         * @return the watch key
         * @throws IOException if unable to register
         */
        @kotlin.Throws(IOException::class)
        private fun register(watchService: WatchService, directory: Path): WatchKey {
            return directory.register(
                watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY
            )
        }
    }

    init {
        service = base.fileSystem.newWatchService()
        this.autoRegisterNewSubDirectories = autoRegisterNewSubDirectories
        watchedLocations = Collections.synchronizedMap(HashMap<Path, WatchedLocation>())
    }
}