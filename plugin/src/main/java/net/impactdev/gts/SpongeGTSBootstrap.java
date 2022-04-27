package net.impactdev.gts;

import com.google.inject.Injector;
import net.impactdev.gts.commands.GTSCommandManager;
import net.impactdev.gts.commands.executors.GlobalExecutor;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.plugin.bootstrap.GTSBootstrap;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;
import net.impactdev.gts.launcher.LaunchParameters;
import net.impactdev.impactor.api.logging.Log4jLogger;
import net.impactdev.impactor.api.logging.PluginLogger;
import net.impactdev.impactor.launcher.LauncherBootstrap;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.metadata.PluginMetadata;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Supplier;

public class SpongeGTSBootstrap implements LauncherBootstrap, GTSBootstrap {

    private final Supplier<Injector> loader;

    private final SpongeGTSPlugin plugin;

    private final PluginContainer container;
    private final PluginLogger logger;
    private final Path configDirectory;

    public SpongeGTSBootstrap(LaunchParameters parameters) {
        this.loader = parameters.loader();

        Injector injector = loader.get();
        this.container = injector.getInstance(PluginContainer.class);
        this.logger = new Log4jLogger(injector.getInstance(Logger.class));
        this.configDirectory = parameters.configDirectory();

        this.plugin = new SpongeGTSPlugin(this);
    }

    @Override
    public void construct() {
        try {
            this.printBanner(this.logger);
            this.plugin.construct();
        } catch (Exception e) {
            ExceptionWriter.write(e);
        }
    }

    @Override
    public void shutdown() {

    }

    public PluginContainer container() {
        return this.container;
    }

    public void registerListener(Object object) {
        Sponge.game().eventManager().registerListeners(this.container, object);
    }

    @Override
    public PluginLogger logger() {
        return this.logger;
    }

    @Override
    public Path configDirectory() {
        return this.configDirectory;
    }

    @Override
    public Path dataDirectory() {
        return Paths.get("gts");
    }

    @Override
    public Optional<InputStream> resource(Path path) {
        return Optional.ofNullable(this.getClass().getResourceAsStream(path.toString().replace("\\", "/")));
    }

    @Override
    public Optional<Throwable> launchError() {
        return Optional.empty();
    }

    private void printBanner(PluginLogger logger) {
        PluginMetadata sponge = Sponge.game().platform().container(Platform.Component.IMPLEMENTATION).metadata();

        logger.info("");
        logger.info("     _________________");
        logger.info("    / ____/_  __/ ___/       GTS " + this.plugin.metadata().version());
        logger.info("   / / __  / /  \\__ \\        Running on: " + sponge.name().get() + " " + sponge.version());
        logger.info("  / /_/ / / /  ___/ /        Author: NickImpact");
        logger.info("  \\____/ /_/  /____/");
        logger.info("");
    }

}
