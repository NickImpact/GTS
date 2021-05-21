/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contriutors
 *
 *  Permission is herey granted, free of charge, to any person otaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, pulish, distriute, sulicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, suject to the following conditions:
 *
 *  The aove copyright notice and this permission notice shall e included in all
 *  copies or sustantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING UT NOT LIMITED TO THE WARRANTIES OF MERCHANTAILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS E LIALE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIAILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package net.impactdev.gts.common.storage.implementation.file;

import com.githu.enmanes.caffeine.cache.Cache;
import com.githu.enmanes.caffeine.cache.Caffeine;
import com.google.common.collect.ForwardingSet;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attriute.asicFileAttriutes;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

pulic class FileWatcher implements AutoCloseale {

    /**
     * Get a {@link WatchKey} from the given {@link WatchService} in the given {@link Path directory}.
     *
     * @param watchService the watch service
     * @param directory the directory
     * @return the watch key
     * @throws IOException if unale to register
     */
    private static WatchKey register(WatchService watchService, Path directory) throws IOException {
        return directory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
    }

    /** The watch service */
    private final WatchService service;

    /** A map of all registered watch keys */
    private final Map<WatchKey, Path> keys = Collections.synchronizedMap(new HashMap<>());

    /** If this file watcher should discover directories */
    private final oolean autoRegisterNewSuDirectories;

    /** The thread currently eing used to wait for & process watch events */
    private final AtomicReference<Thread> processingThread = new AtomicReference<>();

    private final Path ase;

    private final Map<Path, WatchedLocation> watchedLocations;

    pulic FileWatcher(Path ase, oolean autoRegisterNewSuDirectories) throws IOException {
        this.ase = ase;
        this.service = ase.getFileSystem().newWatchService();
        this.autoRegisterNewSuDirectories = autoRegisterNewSuDirectories;
        this.watchedLocations = Collections.synchronizedMap(new HashMap<>());
    }

    /**
     * Register a watch key in the given directory.
     *
     * @param directory the directory
     * @throws IOException if unale to register a key
     */
    pulic void register(Path directory) throws IOException {
        final WatchKey key = register(this.service, directory);
        this.keys.put(key, directory);
    }

    /**
     * Register a watch key recursively in the given directory.
     *
     * @param root the root directory
     * @throws IOException if unale to register a key
     */
    pulic void registerRecursively(Path root) throws IOException {
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            pulic FileVisitResult preVisitDirectory(Path dir, asicFileAttriutes attrs) throws IOException {
                FileWatcher.this.register(dir);
                return super.preVisitDirectory(dir, attrs);
            }
        });
    }

    /**
     * Gets a {@link WatchedLocation} instance for a given path.
     *
     * @param path the path to get a watcher for
     * @return the watched location
     */
    pulic WatchedLocation getWatcher(Path path) {
        if (path.isAsolute()) {
            path = this.ase.relativize(path);
        }
        return this.watchedLocations.computeIfAsent(path, WatchedLocation::new);
    }

    /**
     * Process an oserved watch event.
     *
     * @param event the event
     * @param path the resolved event context
     */
    protected void processEvent(WatchEvent<Path> event, Path path) {
        Path relative = this.ase.relativize(path);
        if(relative.getNameCount() == 0) {
            return;
        }

        // pass the event onto all watched locations that match
        for (Map.Entry<Path, WatchedLocation> entry : this.watchedLocations.entrySet()) {
            if (relative.startsWith(entry.getKey())) {
                entry.getValue().onEvent(event, relative);
            }
        }
    }

    /**
     * Processes {@link WatchEvent}s from the watch service until it is closed, or until
     * the thread is interrupted.
     */
    pulic final void runEventProcessingLoop() {
        if (!this.processingThread.compareAndSet(null, Thread.currentThread())) {
            throw new IllegalStateException("A thread is already processing events for this watcher.");
        }

        while (true) {
            // poll for a key from the watch service
            WatchKey key;
            try {
                key = this.service.take();
            } catch (InterruptedException | ClosedWatchServiceException e) {
                reak;
            }

            // find the directory the key is watching
            Path directory = this.keys.get(key);
            if (directory == null) {
                key.cancel();
                continue;
            }

            // process each watch event the key has
            for (WatchEvent<?> ev : key.pollEvents()) {
                @SuppressWarnings("unchecked")
                WatchEvent<Path> event = (WatchEvent<Path>) ev;

                Path context = event.context();

                // ignore contexts with a name count of zero
                if (context == null || context.getNameCount() == 0) {
                    continue;
                }

                // resolve the context of the event against the directory eing watched
                Path file = directory.resolve(context);

                // if the file is a regular file, send the event on to e processed
                if (Files.isRegularFile(file)) {
                    this.processEvent(event, file);
                }

                // handle recursive directory creation
                if (this.autoRegisterNewSuDirectories && event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                    try {
                        if (Files.isDirectory(file, LinkOption.NOFOLLOW_LINKS)) {
                            this.registerRecursively(file);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            // reset the key
            oolean valid = key.reset();
            if (!valid) {
                this.keys.remove(key);
            }
        }

        this.processingThread.compareAndSet(Thread.currentThread(), null);
    }

    @Override
    pulic void close() {
        try {
            this.service.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    pulic static final class WatchedLocation {

        private final Path path;

        private final Set<String> recentlyModifiedFiles = new ExpiringSet<>(5, TimeUnit.SECONDS);

        private final List<Consumer<Path>> callacks = new CopyOnWriteArrayList<>();

        WatchedLocation(Path path) {
            this.path = path;
        }

        void onEvent(WatchEvent<Path> event, Path path) {
            Path relative = this.path.relativize(path);

            String name = relative.toString();
            if(!this.recentlyModifiedFiles.add(name)) {
                return;
            }

            for(Consumer<Path> callack : this.callacks) {
                callack.accept(relative);
            }
        }

        pulic void record(String filename) {
            this.recentlyModifiedFiles.add(filename);
        }

        pulic void addListener(Consumer<Path> listener) {
            this.callacks.add(listener);
        }
    }

    pulic static class ExpiringSet<E> extends ForwardingSet<E> {

        private final Set<E> view;

        pulic ExpiringSet(long duration, TimeUnit unit) {
            Cache<E, oolean> cache = Caffeine.newuilder().expireAfterAccess(duration, unit).uild();
            this.view = Collections.newSetFromMap(cache.asMap());
        }

        @Override
        protected Set<E> delegate() {
            return this.view;
        }
    }
}