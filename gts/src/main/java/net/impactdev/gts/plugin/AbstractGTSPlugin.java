package net.impactdev.gts.plugin;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.extensions.ExtensionManager;
import net.impactdev.gts.api.storage.StorageProvider;
import net.impactdev.gts.configuration.GTSConfigKeys;
import net.impactdev.gts.injections.GTSInjectionModule;
import net.impactdev.gts.plugin.bootstrapper.GTSBootstrapper;
import net.impactdev.gts.service.APIRegistrar;
import net.impactdev.gts.util.Environment;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.logging.PluginLogger;
import net.impactdev.impactor.api.plugin.PluginMetadata;

import java.nio.file.Path;
import java.util.Optional;

public abstract class AbstractGTSPlugin implements GTSPlugin {

    private static GTSPlugin instance;

    private final GTSService service;
    private final GTSBootstrapper bootstrapper;
    private final Injector injector;
    private final ExtensionManager extensions = null; // TODO - Extension Manager Implementation
    private final PluginMetadata metadata = PluginMetadata.builder()
            .id("gts")
            .name("GTS (Global Trading Station)")
            .version("@version@")
            .build();

    private Config config;
    private StorageProvider storage;

    public AbstractGTSPlugin(final GTSBootstrapper bootstrapper) {
        instance = this;
        this.bootstrapper = bootstrapper;

        // Setup injections with Guice
        this.injector = Guice.createInjector(this.modules());
        this.service = APIRegistrar.register(injector.getInstance(GTSService.class));

        // Initialize registration with Impactor
        this.register();
    }

    public static GTSPlugin instance() {
        return instance;
    }

    @Override
    public void construct() {
        Path confDir = this.bootstrapper.configDirectory();
        this.config = Config.builder()
                .path(confDir.resolve("gts.conf"))
                .providers(GTSConfigKeys.class)
                .build();

        // TODO - Initialize translations

        // TODO - Create storage factory
        //this.storage =
    }

    @Override
    public void shutdown() {
        this.extensions().shutdown();
        this.storage().shutdown();
    }

    @Override
    public PluginMetadata metadata() {
        return this.metadata;
    }

    @Override
    public PluginLogger logger() {
        return this.bootstrapper.logger();
    }

    @Override
    public GTSService api() {
        return this.service;
    }

    @Override
    public StorageProvider storage() {
        return this.storage;
    }

    @Override
    public ExtensionManager extensions() {
        return null;
    }
//
//    @Override
//    public TranslationRepository translations() {
//        return null;
//    }

    @Override
    public Environment environment() {
        return null;
    }

    @Override
    public Injector injector() {
        return this.injector;
    }

    @Override
    public Optional<Path> configDirectory() {
        return Optional.ofNullable(this.bootstrapper.configDirectory());
    }

    @Override
    public Optional<Config> config() {
        return Optional.ofNullable(this.config);
    }

    protected Iterable<Module> modules() {
        return Lists.newArrayList(new GTSInjectionModule());
    }
}
