package net.impactdev.gts;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.blacklist.Blacklist;
import net.impactdev.gts.api.environment.Environment;
import net.impactdev.gts.api.exceptions.LackingServiceException;
import net.impactdev.gts.api.extension.Extension;
import net.impactdev.gts.api.extension.ExtensionManager;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.api.listings.manager.ListingManager;
import net.impactdev.gts.api.messaging.message.errors.ErrorCodes;
import net.impactdev.gts.api.messaging.message.type.admin.ForceDeleteMessage;
import net.impactdev.gts.api.player.PlayerSettings;
import net.impactdev.gts.api.stashes.Stash;
import net.impactdev.gts.api.storage.GTSStorage;
import net.impactdev.gts.commands.GTSCommandManager;
import net.impactdev.gts.commands.executors.GlobalExecutor;
import net.impactdev.gts.common.api.ApiRegistrationUtil;
import net.impactdev.gts.common.api.GTSAPIProvider;
import net.impactdev.gts.common.blacklist.BlacklistImpl;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.config.ConfigProvider;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.data.ResourceManagerImpl;
import net.impactdev.gts.common.extension.SimpleExtensionManager;
import net.impactdev.gts.common.listings.GTSComponentManagerImpl;
import net.impactdev.gts.common.messaging.InternalMessagingService;
import net.impactdev.gts.common.messaging.messages.admin.ForceDeleteMessageImpl;
import net.impactdev.gts.common.player.PlayerSettingsImpl;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.storage.StorageFactory;
import net.impactdev.gts.common.utils.EconomicFormatter;
import net.impactdev.gts.common.utils.Version;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;
import net.impactdev.gts.listeners.AnvilRenameListener;
import net.impactdev.gts.listeners.JoinListener;
import net.impactdev.gts.listings.SpongeItemEntry;
import net.impactdev.gts.listings.data.SpongeItemManager;
import net.impactdev.gts.listings.legacy.SpongeLegacyItemStorable;
import net.impactdev.gts.listings.searcher.SpongeItemSearcher;
import net.impactdev.gts.listings.searcher.SpongeUserSearcher;
import net.impactdev.gts.manager.SpongeListingManager;
import net.impactdev.gts.messaging.SpongeMessagingFactory;
import net.impactdev.gts.messaging.interpreters.SpongeAdminInterpreters;
import net.impactdev.gts.messaging.interpreters.SpongeAuctionInterpreters;
import net.impactdev.gts.messaging.interpreters.SpongeBINInterpreters;
import net.impactdev.gts.messaging.interpreters.SpongeDeliveryInterpreters;
import net.impactdev.gts.messaging.interpreters.SpongeListingInterpreters;
import net.impactdev.gts.messaging.interpreters.SpongePingPongInterpreter;
import net.impactdev.gts.placeholders.GTSSpongePlaceholderManager;
import net.impactdev.gts.sponge.deliveries.SpongeDelivery;
import net.impactdev.gts.sponge.listings.SpongeAuction;
import net.impactdev.gts.sponge.listings.SpongeBuyItNow;
import net.impactdev.gts.sponge.listings.ui.SpongeMainPageProvider;
import net.impactdev.gts.sponge.pricing.provided.MonetaryPrice;
import net.impactdev.gts.sponge.stash.SpongeStash;
import net.impactdev.gts.ui.SpongeMainMenu;
import net.impactdev.gts.util.OreVersionChecker;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.dependencies.Dependency;
import net.impactdev.impactor.api.dependencies.DependencyManager;
import net.impactdev.impactor.api.dependencies.ProvidedDependencies;
import net.impactdev.impactor.api.dependencies.relocation.Relocation;
import net.impactdev.impactor.api.logging.PluginLogger;
import net.impactdev.impactor.api.plugin.PluginMetadata;
import net.impactdev.impactor.api.storage.StorageType;
import net.impactdev.impactor.api.utilities.printing.PrettyPrinter;
import org.spongepowered.api.Platform;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.LifecycleEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.RegisterRegistryValueEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.placeholder.PlaceholderParser;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.plugin.PluginContainer;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SpongeGTSPlugin implements GTSPlugin {

    private final SpongeGTSBootstrap bootstrap;

    private final Gson gson = new GsonBuilder().create();
    private final ExtensionManager extensions = new SimpleExtensionManager(this);
    private InternalMessagingService messenger;

    private GTSStorage storage;
    private ConfigProvider provider;
    private Environment environment;

    private String lastLifecycleEvent = "Construct";

    public SpongeGTSPlugin(SpongeGTSBootstrap bootstrap) {
        this.bootstrap = bootstrap;
        Impactor.getInstance().getRegistry().register(GTSPlugin.class, this);
        this.register();
    }

    @Override
    public <T extends GTSPlugin> T as(Class<T> type) {
        if(!type.isAssignableFrom(this.getClass())) {
            throw new RuntimeException("Invalid plugin typing");
        }
        return (T) this;
    }

    @Override
    public void construct() throws Exception {
        ApiRegistrationUtil.register(new GTSAPIProvider());
        Impactor.getInstance().getRegistry().register(Blacklist.class, new BlacklistImpl());

        Path configDirectory = this.bootstrap.configDirectory();
        this.copy(Paths.get("gts.conf"), configDirectory);
        Config main = Config.builder()
                .path(configDirectory.resolve("gts.conf"))
                .provider(ConfigKeys.class)
                .build();

        String langKey = main.get(ConfigKeys.LANGUAGE).toLowerCase();
        this.copy(Paths.get("lang").resolve(langKey + ".conf"), configDirectory);
        Config lang = Config.builder()
                .path(configDirectory.resolve("lang").resolve(langKey + ".conf"))
                .provider(MsgConfigKeys.class)
                .build();

        this.provider = new ConfigProvider(main, lang);
        Impactor.getInstance().getRegistry().register(ListingManager.class, new SpongeListingManager());
        Impactor.getInstance().getRegistry().register(
                EconomicFormatter.class,
                amount -> Sponge.server().serviceProvider().economyService()
                        .orElseThrow(IllegalStateException::new)
                        .defaultCurrency()
                        .format(new BigDecimal(amount))
        );

        GTSService service = GTSService.getInstance();

        service.getGTSComponentManager().registerListingResourceManager(BuyItNow.class, new ResourceManagerImpl<>("BIN", "minecraft:emerald", SpongeBuyItNow::deserialize));
        service.getGTSComponentManager().registerListingResourceManager(Auction.class, new ResourceManagerImpl<>("Auctions", "minecraft:gold_ingot", SpongeAuction::deserialize));
        service.getGTSComponentManager().registerPriceManager(MonetaryPrice.class, new MonetaryPrice.MonetaryPriceManager());
        service.addSearcher(new SpongeItemSearcher());
        service.addSearcher(new SpongeUserSearcher());
        this.builders();

        this.messenger = new SpongeMessagingFactory(this).getInstance();
        new SpongePingPongInterpreter().register(this);
        new SpongeBINInterpreters().register(this);
        new SpongeAuctionInterpreters().register(this);
        new SpongeListingInterpreters().register(this);
        new SpongeAdminInterpreters().register(this);
        new SpongeDeliveryInterpreters().register(this);

        this.storage = new StorageFactory(this).getInstance(StorageType.JSON);

        if(main.get(ConfigKeys.REDIS_ENABLED)) {
            this.logger().info("Setting up Redis Client...");
            Impactor.getInstance().getRegistry().get(DependencyManager.class).loadDependencies(Lists.newArrayList(
                    Dependency.builder()
                            .name("Jedis - Redis Client")
                            .group("redis{}clients")
                            .artifact("jedis")
                            .version("3.3.0")
                            .checksum("HuTfz9xW/mi1fwVQ3xgPmd6qwTRMF/3fyMzw2LmOgy4=")
                            .relocation(Relocation.of("redis{}clients{}jedis", "jedis"))
                            .relocation(Relocation.of("org{}apache{}commons{}pool2", "commonspool2"))
                            .with(Dependency.builder()
                                    .name("Apache Commons Pool 2")
                                    .group("org{}apache{}commons")
                                    .artifact("commons-pool2")
                                    .version("2.8.0")
                                    .checksum("Xvqfu1SlixoSIFpfrFZfaYKr/rD/Rb28MYdI71/To/8=")
                                    .relocation(Relocation.of("org{}apache{}commons{}pool2", "commonspool2"))
                                    .build()
                            )
                            .build()
            ));
        }

        if(main.get(ConfigKeys.ENABLE_ITEMS)) {
            GTSService.getInstance().getGTSComponentManager().registerEntryManager(SpongeItemEntry.class, new SpongeItemManager());
            GTSService.getInstance().getGTSComponentManager().registerLegacyEntryDeserializer("item", new SpongeLegacyItemStorable());
        }

        Sponge.eventManager().registerListeners(this.container(), new SpongeGTSPlugin.ListenerRegistrar(this, this.container()));
        this.extensions.loadExtensions(this.bootstrap.configDirectory().resolve("extensions"));

        if(!main.get(ConfigKeys.ENABLE_ITEMS)
                && this.extensions.getLoadedExtensions().isEmpty()
                && GTSService.getInstance().getGTSComponentManager().getAllEntryManagers().isEmpty()) {
            new PrettyPrinter(80)
                    .add("No Entry Types Available").center()
                    .hr('-')
                    .add("It seems you've disabled the ability to list items on the market,")
                    .add("but do not have any compatible extensions installed that provide")
                    .add("other types of entries. GTS is not setup to handle this, and has")
                    .add("enforced safe mode to avoid runtime errors!")
                    .newline()
                    .add("Please re-enable the items capability of GTS, or install an extension")
                    .add("that provides other entry types to proceed with plugin usage. If you truly")
                    .add("wish to turn off all entry types, please consult maintenance mode, where you")
                    .add("can effectively disable features of the plugin without hurting performance.")
                    .log(GTSPlugin.instance().logger(), PrettyPrinter.Level.ERROR);
            ((GTSAPIProvider) GTSService.getInstance()).setSafeMode(ErrorCodes.FATAL_ERROR);
            throw new IllegalStateException("No entry types available");
        }

        main.get(ConfigKeys.BLACKLIST).read();

        ((GTSComponentManagerImpl) GTSService.getInstance().getGTSComponentManager())
                .setDeliveryDeserializer(SpongeDelivery::deserialize);
    }

    @Override
    public void shutdown() {

    }

    @Override
    public SpongeGTSBootstrap bootstrap() {
        return this.bootstrap;
    }

    @Override
    public final ConfigProvider configuration() {
        return this.provider;
    }

    @Override
    public Optional<Path> configDirectory() {
        return Optional.ofNullable(this.bootstrap.configDirectory());
    }

    @Override
    public Optional<Config> config() {
        return Optional.ofNullable(this.configuration().main());
    }

    @Override
    public Environment environment() {
        return Optional.ofNullable(this.environment)
                .orElseGet(() -> {
                    this.environment = new Environment();

                    environment.append("LifeCycle Phase", this.lastLifecycleEvent);

                    environment.append("Sponge", Sponge.game().platform().container(Platform.Component.IMPLEMENTATION).metadata().name().get() + " " + Sponge.game().platform().container(Platform.Component.IMPLEMENTATION).metadata().version());
                    environment.append("Impactor", Sponge.pluginManager().plugin("impactor").get().metadata().version().toString());
                    environment.append("GTS", this.metadata().version());

                    if(Sponge.isServerAvailable()) {
                        boolean economy = Sponge.server().serviceProvider().economyService().isPresent();
                        this.environment.append("Economy Available", String.valueOf(economy));
                    }

//                    Sponge.getServiceManager().getRegistration(ProtocolService.class).ifPresent(provider -> {
//                        environment.append(provider.getPlugin().getName(), provider.getPlugin().getVersion().orElse("Unknown"));
//                    });

                    for(Extension extension : GTSService.getInstance().getAllExtensions()) {
                        environment.append(extension.metadata().name(), extension.metadata().version());
                        extension.getExtendedEnvironmentInformation(environment);
                    }

                    return environment;
                });
    }

    public PluginContainer container() {
        return this.bootstrap().container();
    }

    @Override
    public Gson gson() {
        return this.gson;
    }

    @Override
    public GTSStorage storage() {
        return this.storage;
    }

    @Override
    public ExtensionManager extensionManager() {
        return this.extensions;
    }

    @Override
    public InternalMessagingService messagingService() {
        return this.messenger;
    }

    @Override
    public CompletableFuture<String> playerDisplayName(UUID id) {
        return Sponge.server().userManager().load(id)
                .thenApply(user -> {
                    if(!user.isPresent()) {
                        throw new IllegalArgumentException("Could not find user with that ID");
                    }

                    User u = user.get();
                    return u.name();
                })
                .exceptionally(exception -> {
                    ExceptionWriter.write(exception);
                    return "Unknown";
                });
    }

    @Override
    public PluginMetadata metadata() {
        return PluginMetadata.builder()
                .id("gts")
                .name("GTS")
                .version("@version@")
                .build();
    }

    @Override
    public PluginLogger logger() {
        return this.bootstrap.logger();
    }

    @Override
    public Set<Dependency> dependencies() {
        return Sets.newHashSet(
                Dependency.builder()
                        .name("Caffeine")
                        .group("com{}github{}ben-manes{}caffeine")
                        .artifact("caffeine")
                        .version("2.8.4")
                        .checksum("KV9YN5gQj6b507VJApJpPF5PkCon0DZqAi0T7Ln0lag=")
                        .relocation(Relocation.of("com{}github{}benmanes{}caffeine", "caffeine"))
                        .build(),
                Dependency.builder()
                        .name("MxParser Math Library")
                        .group("org.mariuszgromada.math")
                        .artifact("MathParser.org-mXparser")
                        .version("4.4.2")
                        .checksum("z+nZN08mJQ8UniReVzNorIApq3QhAUws6ZtNrtWR8dA=")
                        .relocation(Relocation.of("org{}mariuszgromada{}math{}mxparser", "mxparser"))
                        .build()
        );
    }

    public static final class ListenerRegistrar {

        private final SpongeGTSPlugin parent;
        private final PluginContainer container;

        public ListenerRegistrar(SpongeGTSPlugin parent, PluginContainer container) {
            this.parent = parent;
            this.container = container;
        }

        @Listener
        public void onLifecycleEvent(final LifecycleEvent event) {
            this.parent.lastLifecycleEvent = event.getClass().getSimpleName();
        }

        @Listener
        public void whenCommandRegistration(RegisterCommandEvent<Command.Parameterized> event) {
            try {
                this.parent.logger().info("Registering commands");
                new GTSCommandManager(this.container, event).register(new GlobalExecutor(GTSPlugin.instance()));
            } catch (Exception e) {
                ExceptionWriter.write(e);
            }
        }

        @Listener
        public void onGlobalRegistryValueRegistrationEvent(final RegisterRegistryValueEvent.GameScoped event) {
            try {
                this.parent.logger().info("Registering placeholders");

                final RegisterRegistryValueEvent.RegistryStep<PlaceholderParser> placeholderParserRegistryStep =
                        event.registry(RegistryTypes.PLACEHOLDER_PARSER);
                new GTSSpongePlaceholderManager().getAllParsers().forEach(metadata -> {
                    placeholderParserRegistryStep.register(metadata.getKey(), metadata.getParser());
                });
            } catch (Exception e) {
                ExceptionWriter.write(e);
            }
        }

        @Listener
        public void onServerEngineStart(final StartingEngineEvent<Server> event) {
            this.parent.extensions.enableExtensions();

            Sponge.eventManager().registerListeners(this.container, new JoinListener(this.container));

            if(!this.parent.configuration().main().get(ConfigKeys.ITEMS_ALLOW_ANVIL_NAMES)) {
                Sponge.eventManager().registerListeners(this.container, new AnvilRenameListener());
            }
        }

        @Listener
        public void onServerEngineStarted(final StartedEngineEvent<Server> event) {
            Optional<EconomyService> economy = Sponge.server().serviceProvider().economyService();
            if(!economy.isPresent()) {
                throw new LackingServiceException(EconomyService.class);
            }

            MonetaryPrice.setEconomy(economy.get());
            final Version current = new Version(this.parent.metadata().version());
            OreVersionChecker.query().thenAccept(response -> {
                if(current.compareTo(response) < 0) {
                    Impactor.getInstance().getScheduler().executeSync(() -> {
                        GTSPlugin.instance().logger().warn("A new version of GTS is available on Ore!");
                        GTSPlugin.instance().logger().warn("You can download it from here: https://ore.spongepowered.org/api/v1/projects/gts/versions/recommended/download");
                    });
                }
            });
        }

    }

    private void builders() {
        Impactor.getInstance().getRegistry().registerBuilderSupplier(Auction.AuctionBuilder.class, SpongeAuction.SpongeAuctionBuilder::new);
        Impactor.getInstance().getRegistry().registerBuilderSupplier(BuyItNow.BuyItNowBuilder.class, SpongeBuyItNow.SpongeBuyItNowBuilder::new);
        Impactor.getInstance().getRegistry().registerBuilderSupplier(Stash.StashBuilder.class, SpongeStash.SpongeStashBuilder::new);
        Impactor.getInstance().getRegistry().registerBuilderSupplier(SpongeMainPageProvider.Creator.class, SpongeMainMenu.MainMenuCreator::new);
        Impactor.getInstance().getRegistry().registerBuilderSupplier(PlayerSettings.PlayerSettingsBuilder.class, PlayerSettingsImpl.PlayerSettingsImplBuilder::new);
        Impactor.getInstance().getRegistry().registerBuilderSupplier(ForceDeleteMessage.Response.ResponseBuilder.class, ForceDeleteMessageImpl.ForceDeleteResponse.ForcedDeleteResponseBuilder::new);
    }

    private void copy(Path target, Path destination) {
        Path base = Paths.get("assets", "gts");
        if(!Files.exists(destination.resolve(target))) {
            try(InputStream resource = this.bootstrap.resource(base.resolve(target)).orElseThrow(() -> new IllegalStateException("Could not locate target resource: " + base.resolve(target)))) {
                Files.createDirectories(destination.resolve(target).getParent());
                Files.copy(resource, destination.resolve(target));
            } catch (Exception e) {
                ExceptionWriter.write(e);
            }
        }
    }

}
