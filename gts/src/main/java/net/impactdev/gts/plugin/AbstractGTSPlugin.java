package net.impactdev.gts.plugin;

import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.events.lifecycle.DeserializerRegistrationEvent;
import net.impactdev.gts.api.modules.markets.ListingManager;
import net.impactdev.gts.commands.GTSCommands;
import net.impactdev.gts.commands.admin.TranslationCommands;
import net.impactdev.gts.communication.implementation.CommunicationFactory;
import net.impactdev.gts.communication.implementation.CommunicationService;
import net.impactdev.gts.communication.implementation.messages.types.utility.PingMessage;
import net.impactdev.gts.elements.provided.content.ItemStackContent;
import net.impactdev.gts.configuration.GTSConfigKeys;
import net.impactdev.gts.extensions.ExtensionManager;
import net.impactdev.gts.locale.TranslationManager;
import net.impactdev.gts.locale.TranslationRepository;
import net.impactdev.gts.locale.placeholders.GTSPlaceholders;
import net.impactdev.gts.plugin.bootstrapper.GTSBootstrapper;
import net.impactdev.gts.registries.RegistryAccessors;
import net.impactdev.gts.service.APIRegistrar;
import net.impactdev.gts.service.GTSServiceImplementation;
import net.impactdev.gts.storage.GTSStorage;
import net.impactdev.gts.storage.GTSStorageFactory;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.commands.events.CommandRegistrationEvent;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.logging.PluginLogger;
import net.impactdev.impactor.api.platform.plugins.PluginMetadata;
import net.impactdev.impactor.api.plugin.PluginRegistry;
import net.impactdev.impactor.api.storage.StorageType;
import net.impactdev.impactor.api.text.events.RegisterPlaceholdersEvent;
import net.impactdev.impactor.api.utility.ExceptionPrinter;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public abstract class AbstractGTSPlugin implements GTSPlugin {

    private static GTSPlugin instance;

    private final PluginMetadata metadata = PluginMetadata.builder()
            .id("gts")
            .name("GTS (Global Trading Station)")
            .version("@version@")
            .build();

    private final GTSService service;
    private final GTSBootstrapper bootstrapper;
    private final ExtensionManager extensions = new ExtensionManager();

    private Config config;
    private TranslationManager translations;
    private CommunicationService communications;
    private GTSStorage storage;

    public AbstractGTSPlugin(final GTSBootstrapper bootstrapper) {
        instance = this;
        this.bootstrapper = bootstrapper;

        PluginRegistry.register(this.metadata, this);
        this.service = APIRegistrar.register(new GTSServiceImplementation());
    }

    public static GTSPlugin instance() {
        return instance;
    }

    protected abstract CommunicationFactory communicationFactory();

    @Override
    public void construct() {
        try {
            this.logger().info("Starting GTS service...");

            Path confDir = this.bootstrapper.configDir();
            this.config = Config.builder()
                    .path(confDir.resolve("gts.conf"))
                    .provider(GTSConfigKeys.class)
                    .provideIfMissing(() -> this.resource(root -> root.resolve("gts.conf")))
                    .build();

            this.translations = new TranslationManager();

            TranslationRepository repository = new TranslationRepository();
            repository.scheduleRefresh();
            //this.translations.reload();

            this.communications = this.communicationFactory().instance();
            communications.communicator().publish(new PingMessage(UUID.randomUUID()));
            this.storage = GTSStorageFactory.create(StorageType.JSON);
            //this.listings = new GTSListingManager();

            this.registration();
            this.commands();
            Impactor.instance().events().subscribe(RegisterPlaceholdersEvent.class, event -> {
                event.registerAll(GTSPlaceholders.parsers());
            });

            this.extensions.construct(this.configurationDirectory().resolve("extensions"));
        } catch (Exception e) {
            throw new RuntimeException("Encountered an exception during plugin launch", e);
        }
    }

    @Override
    public void setup() {

    }

    @Override
    public void starting() {

    }

    @Override
    public void started() {

    }

    /**
     * Used to initialize registries for the plugin
     */
    public void load() {
        try {
            RegistryAccessors.DESERIALIZERS.init();

            this.extensions.enable();
        } catch (Exception e) {
            ExceptionPrinter.print(this.logger(), e);
        }
    }

    @Override
    public void shutdown() {
        try {
            this.extensions().shutdown();
            this.storage().shutdown();
        } catch (Exception e) {
            ExceptionPrinter.print(this.logger(), e);
        }
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
    public GTSStorage storage() {
        return this.storage;
    }

    @Override
    public ExtensionManager extensions() {
        return this.extensions;
    }

    @Override
    public CommunicationService communication() {
        return this.communications;
    }

    @Override
    public ListingManager listings() {
        return null;
    }

    @Override
    public TranslationManager translations() {
        return this.translations;
    }

//    @Override
//    public Optional<Path> configDirectory() {
//        return Optional.ofNullable();
//    }
//
//    @Override
//    public Optional<Config> config() {
//        return Optional.ofNullable();
//    }


    @Override
    public boolean inSafeMode() {
        return false;
    }

    @Override
    public Path configurationDirectory() {
        return this.bootstrapper.configDir();
    }

    @Override
    public Config configuration() {
        return this.config;
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

    @Override
    public InputStream resource(Function<Path, Path> target) {
        Path path = target.apply(Paths.get("gts").resolve("assets"));
        return Optional.ofNullable(this.getClass()
                .getClassLoader()
                .getResourceAsStream(path.toString().replace("\\", "/"))
        ).orElseThrow(() -> new IllegalArgumentException("Target resource not located"));
    }
}
