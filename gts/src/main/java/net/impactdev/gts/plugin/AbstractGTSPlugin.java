package net.impactdev.gts.plugin;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.events.lifecycle.DeserializerRegistrationEvent;
import net.impactdev.gts.api.extensions.ExtensionManager;
import net.impactdev.gts.api.storage.StorageProvider;
import net.impactdev.gts.commands.GTSCommands;
import net.impactdev.gts.commands.admin.TranslationCommands;
import net.impactdev.gts.components.provided.content.ItemStackContent;
import net.impactdev.gts.configuration.GTSConfigKeys;
import net.impactdev.gts.extensions.GTSExtensionManager;
import net.impactdev.gts.injections.GTSInjectionModule;
import net.impactdev.gts.locale.TranslationManager;
import net.impactdev.gts.plugin.bootstrapper.GTSBootstrapper;
import net.impactdev.gts.registries.RegistryAccessors;
import net.impactdev.gts.service.APIRegistrar;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.commands.events.CommandRegistrationEvent;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.logging.PluginLogger;
import net.impactdev.impactor.api.plugin.PluginMetadata;
import net.impactdev.impactor.api.utilities.ExceptionPrinter;

import java.nio.file.Path;
import java.util.Optional;

public abstract class AbstractGTSPlugin implements GTSPlugin {

    private static GTSPlugin instance;

    private final PluginMetadata metadata = PluginMetadata.builder()
            .id("gts")
            .name("GTS (Global Trading Station)")
            .version("@version@")
            .build();

    private final GTSService service;
    private final GTSBootstrapper bootstrapper;
    private final Injector injector;
    private final ExtensionManager extensions = new GTSExtensionManager();

    private Config config;
    private TranslationManager translations;
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
        try {
            Path confDir = this.bootstrapper.configDir();
            this.config = Config.builder()
                    .path(confDir.resolve("gts.conf"))
                    .providers(GTSConfigKeys.class)
                    .build();

            this.translations = new TranslationManager();
            //this.translations.reload();

            // TODO - Create storage factory
            //this.storage =

            this.registration();
            this.commands();

            this.extensions.construct(this.configDirectory()
                    .orElseThrow(() -> new IllegalStateException("No config directory was specified"))
                    .resolve("extensions")
            );
        } catch (Exception e) {
            throw new RuntimeException("Encountered an exception during plugin launch", e);
        }
    }

    /**
     * Used to initialize registries for the plugin
     */
    public void load() {
        try {
            RegistryAccessors.DESERIALIZERS.init();

            this.extensions.enable();
        } catch (Exception e) {
            ExceptionPrinter.print(this, e);
        }
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
        return this.extensions;
    }

    @Override
    public TranslationManager translations() {
        return this.translations;
    }

    @Override
    public Injector injector() {
        return this.injector;
    }

    @Override
    public Optional<Path> configDirectory() {
        return Optional.ofNullable(this.bootstrapper.configDir());
    }

    @Override
    public Optional<Config> config() {
        return Optional.ofNullable(this.config);
    }

    protected Iterable<Module> modules() {
        return Lists.newArrayList(new GTSInjectionModule());
    }

    private void commands() {
        Impactor.instance().events().subscribe(CommandRegistrationEvent.class, event -> {
            event.register(GTSCommands.class)
                    .register(TranslationCommands.class);
        });
    }

    private void registration() {
        Impactor.instance().events().subscribe(DeserializerRegistrationEvent.class, event -> {
            event.register(ItemStackContent.class, ItemStackContent::deserialize);
        });
    }
}
