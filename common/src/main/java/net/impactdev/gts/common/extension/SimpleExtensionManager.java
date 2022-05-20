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

package net.impactdev.gts.common.extension;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.impactdev.gts.api.commands.GTSCommandExecutor;
import net.impactdev.gts.api.event.factory.GTSEventFactory;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.dependencies.Dependency;
import net.impactdev.impactor.api.dependencies.DependencyManager;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.extensions.Extension;
import net.impactdev.gts.api.extensions.ExtensionManager;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;
import net.impactdev.impactor.api.dependencies.classpath.ClassPathAppender;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimpleExtensionManager implements ExtensionManager, AutoCloseable {

    private final GTSPlugin plugin;
    private final Set<LoadedExtension> extensions = new HashSet<>();

    public SimpleExtensionManager(GTSPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void close() {
        for (LoadedExtension extension : this.extensions) {
            try {
                extension.instance.unload();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.extensions.clear();
    }

    @Override
    public void loadExtension(Extension extension) {
        if(this.extensions.stream().anyMatch(e -> e.instance.equals(extension))) {
            return;
        }

        this.plugin.logger().info("Loading extension: " + extension.metadata().name());
        this.extensions.add(new LoadedExtension(extension, null));
        extension.load(GTSService.getInstance(), GTSPlugin.instance().configDirectory().orElseThrow(NoSuchElementException::new));
        Impactor.getInstance().getEventBus().post(GTSEventFactory.createExtensionLoadEvent(extension));
    }

    public void loadExtensions(Path directory) {
        if(!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if(!Files.isDirectory(directory)) {
            return;
        }

        Set<Dependency> dependencies = Sets.newHashSet();
        try(Stream<Path> stream = Files.list(directory)) {
            stream.forEach(path -> {
                if(path.getFileName().toString().endsWith(".jar")) {
                    try {
                        Extension extension = this.loadExtension(path);
                        dependencies.addAll(extension.getRequiredDependencies());
                    } catch (Exception e) {
                        RuntimeException exception = new RuntimeException("Exception loading extension from " + path, e);
                        ExceptionWriter.write(exception);
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.plugin.logger().info("Initializing any additionally required dependencies...");
        Impactor.getInstance().getRegistry().get(DependencyManager.class).loadDependencies(dependencies);

        for(Extension extension : this.getLoadedExtensions()) {
            try {
                this.plugin.logger().info("Loading extension: &a" + extension.metadata().name());
                extension.load(GTSService.getInstance(), GTSPlugin.instance().configDirectory().orElseThrow(NoSuchElementException::new));
            } catch (Exception e) {
                this.plugin.logger().error("Failed to load extension '" + extension.metadata().name() + "', check error below...");
                ExceptionWriter.write(e);
            }
        }
    }

    @Override
    public @NonNull Extension loadExtension(Path path) throws IOException {
        if(this.extensions.stream().anyMatch(e -> path.endsWith(e.path))) {
            throw new IllegalStateException("Extension at path " + path + " is already loaded");
        }

        if(!Files.exists(path)) {
            throw new NoSuchFileException("No file at " + path);
        }

        String className = null;
        try (JarFile jar = new JarFile(path.toFile())) {
            JarEntry extensionJarEntry = jar.getJarEntry("extension.json");
            if (extensionJarEntry == null) {
                throw new IllegalStateException("Likely extension at path " + path + " missing extension.json");
            }
            try (InputStream in = jar.getInputStream(extensionJarEntry)) {
                if (in == null) {
                    throw new IllegalStateException("Unable to read extension.json for extension at path " + path);
                }
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                    JsonElement parsed = JsonParser.parseReader(reader);
                    if(parsed.getAsJsonObject().has("main")) {
                        className = parsed.getAsJsonObject().get("main").getAsString();
                    }
                }
            }
        }

        if (className == null) {
            throw new IllegalArgumentException("Failed to locate main class from extension.json for path: " + path);
        }

        Impactor.getInstance().getRegistry().get(ClassPathAppender.class).addJarToClasspath(path);

        Class<? extends Extension> extensionClass;
        try {
            extensionClass = Class.forName(className).asSubclass(Extension.class);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        Extension extension;
        try {
            Constructor<? extends Extension> constructor = extensionClass.getConstructor();
            extension = constructor.newInstance();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Unable to find valid constructor in " + extensionClass.getName());
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        this.extensions.add(new LoadedExtension(extension, path));
        Impactor.getInstance().getEventBus().post(GTSEventFactory.createExtensionLoadEvent(extension));

        return extension;
    }

    @Override
    public void enableExtensions() {
        for(Extension extension : this.getLoadedExtensions()) {
            extension.enable(GTSService.getInstance());
            for(GTSCommandExecutor<?, ?, ?> executor : extension.getExecutors()) {
                executor.register();
            }
        }
    }

    @Override
    public @NonNull Collection<Extension> getLoadedExtensions() {
        return this.extensions.stream().map(e -> e.instance).collect(Collectors.toSet());
    }

    private static final class LoadedExtension {
        private final Extension instance;
        private final Path path;

        private LoadedExtension(Extension instance, Path path) {
            this.instance = instance;
            this.path = path;
        }
    }
}
