package net.impactdev.gts.bungee;

import net.impactdev.gts.common.plugin.bootstrap.GTSBootstrap;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;
import net.impactdev.impactor.api.logging.JavaLogger;
import net.impactdev.impactor.api.logging.PluginLogger;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

public class GTSBungeeBootstrap implements GTSBootstrap {

    private final GTSBungeePlugin plugin;

    private final Plugin proxy;
    private final PluginLogger logger;
    private final Path configDir;
    private final Path dataDir;

    private Throwable exception;

    public GTSBungeeBootstrap(final Plugin plugin) {
        this.plugin = new GTSBungeePlugin(this);
        this.proxy = plugin;
        this.logger = new JavaLogger(this.plugin, plugin.getLogger());
        this.configDir = plugin.getDataFolder().toPath();
        this.dataDir = plugin.getDataFolder().toPath();
    }

    @Override
    public void construct() {
        try {
            this.plugin.construct();
        } catch (Exception e) {
            ExceptionWriter.write(e);
            this.exception = e;
        }
    }

    @Override
    public void shutdown() {}

    public Plugin proxy() {
        return this.proxy;
    }

    @Override
    public PluginLogger logger() {
        return this.logger;
    }

    @Override
    public Path configDirectory() {
        return this.configDir;
    }

    @Override
    public Path dataDirectory() {
        return this.dataDir;
    }

    @Override
    public Optional<InputStream> resource(Path path) {
        return Optional.ofNullable(this.getClass().getClassLoader().getResourceAsStream(path.toString().replace("\\", "/")));
    }

    @Override
    public Optional<Throwable> launchError() {
        return Optional.ofNullable(this.exception);
    }

}
