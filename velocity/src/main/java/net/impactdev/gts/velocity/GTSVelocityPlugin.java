package net.impactdev.gts.velocity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.velocitypowered.api.proxy.Player;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.blacklist.Blacklist;
import net.impactdev.gts.api.extension.ExtensionManager;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.api.messaging.message.type.admin.ForceDeleteMessage;
import net.impactdev.gts.api.storage.GTSStorage;
import net.impactdev.gts.common.api.ApiRegistrationUtil;
import net.impactdev.gts.common.api.GTSAPIProvider;
import net.impactdev.gts.common.blacklist.BlacklistImpl;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.config.ConfigProvider;
import net.impactdev.gts.common.data.ResourceManagerImpl;
import net.impactdev.gts.common.dependencies.GTSDependencies;
import net.impactdev.gts.common.messaging.InternalMessagingService;
import net.impactdev.gts.common.messaging.MessagingFactory;
import net.impactdev.gts.common.messaging.messages.admin.ForceDeleteMessageImpl;
import net.impactdev.gts.api.environment.Environment;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.storage.StorageFactory;
import net.impactdev.gts.velocity.listings.VelocityAuction;
import net.impactdev.gts.velocity.listings.VelocityBIN;
import net.impactdev.gts.velocity.messaging.VelocityMessagingFactory;
import net.impactdev.gts.velocity.messaging.interpreters.VelocityAdminInterpreters;
import net.impactdev.gts.velocity.messaging.interpreters.VelocityAuctionInterpreter;
import net.impactdev.gts.velocity.messaging.interpreters.VelocityBINInterpreter;
import net.impactdev.gts.velocity.messaging.interpreters.VelocityListingInterpreter;
import net.impactdev.gts.velocity.messaging.interpreters.VelocityPingPongInterpreter;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.dependencies.Dependency;
import net.impactdev.impactor.api.dependencies.ProvidedDependencies;
import net.impactdev.impactor.api.logging.PluginLogger;
import net.impactdev.impactor.api.plugin.PluginMetadata;
import net.impactdev.impactor.api.storage.StorageType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class GTSVelocityPlugin implements GTSPlugin {

    private static GTSVelocityPlugin plugin;

    private final GTSVelocityBootstrap bootstrap;

    private ConfigProvider provider;

    private GTSStorage storage;
    private InternalMessagingService messagingService;

    private Environment environment;

    public GTSVelocityPlugin(GTSVelocityBootstrap bootstrap) {
        this.bootstrap = bootstrap;
        plugin = this;
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
    public GTSVelocityBootstrap bootstrap() {
        return this.bootstrap;
    }

    @Override
    public ConfigProvider configuration() {
        return this.provider;
    }

    @Override
    public Environment environment() {
        Environment environment = Optional.ofNullable(this.environment)
                .orElseGet(() -> (this.environment = new Environment()));
        environment.append(this.bootstrap.getProxy().getVersion().getName(), this.bootstrap.getProxy().getVersion().getVersion() + " - " + this.bootstrap.getProxy().getVersion().getVendor());
        environment.append("Impactor", this.bootstrap.getProxy().getPluginManager().getPlugin("impactor").get().getDescription().getVersion().get());
        environment.append("GTS", this.metadata().version());

        return environment;
    }

    @Override
    public Gson gson() {
        return new GsonBuilder().create();
    }

    @Override
    public GTSStorage storage() {
        return this.storage;
    }

    @Override
    public ExtensionManager extensionManager() {
        return null;
    }

    @Override
    public InternalMessagingService messagingService() {
        return this.messagingService;
    }

    @Override
    public CompletableFuture<String> playerDisplayName(UUID id) {
        return CompletableFuture.completedFuture(this.bootstrap().getProxy().getPlayer(id).map(Player::getUsername).orElse("Unknown"));
    }

    public MessagingFactory<?> getMessagingFactory() {
        return new VelocityMessagingFactory(this);
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
    public void construct() throws Exception {
        ApiRegistrationUtil.register(new GTSAPIProvider());

        Impactor.getInstance().getRegistry().register(GTSPlugin.class, this);
        Impactor.getInstance().getRegistry().register(Blacklist.class, new BlacklistImpl());

        GTSService.getInstance().getGTSComponentManager().registerListingResourceManager(BuyItNow.class, new ResourceManagerImpl<>("BIN", "N/A", VelocityBIN::deserialize));
        GTSService.getInstance().getGTSComponentManager().registerListingResourceManager(Auction.class, new ResourceManagerImpl<>("Auctions", "N/A", VelocityAuction::deserialize));
        Impactor.getInstance().getRegistry().registerBuilderSupplier(Auction.AuctionBuilder.class, VelocityAuction.VelocityAuctionBuilder::new);
        Impactor.getInstance().getRegistry().registerBuilderSupplier(BuyItNow.BuyItNowBuilder.class, VelocityBIN.VelocityBINBuilder::new);
        Impactor.getInstance().getRegistry().registerBuilderSupplier(ForceDeleteMessage.Response.ResponseBuilder.class, ForceDeleteMessageImpl.ForceDeleteResponse.ForcedDeleteResponseBuilder::new);

        Path configDirectory = this.bootstrap.configDirectory();
        this.copyResource(Paths.get("gts.conf"), configDirectory);
        Config config = Config.builder()
                .path(configDirectory.resolve("gts.conf"))
                .provider(ConfigKeys.class)
                .build();

        this.provider = new ConfigProvider(config, null);
        this.storage = new StorageFactory(this).getInstance(StorageType.MARIADB);

        this.messagingService = this.getMessagingFactory().getInstance();

        new VelocityPingPongInterpreter().register(this);
        new VelocityBINInterpreter().register(this);
        new VelocityAuctionInterpreter().register(this);
        new VelocityListingInterpreter().register(this);
        new VelocityAdminInterpreters().register(this);
    }

    @Override
    public void shutdown() throws Exception {

    }

    @Override
    public Set<Dependency> dependencies() {
        return Sets.newHashSet(
                ProvidedDependencies.CAFFEINE,
                ProvidedDependencies.CONFIGURATE_CORE,
                ProvidedDependencies.CONFIGURATE_HOCON,
                ProvidedDependencies.CONFIGURATE_GSON,
                ProvidedDependencies.CONFIGURATE_YAML,
                ProvidedDependencies.TYPESAFE_CONFIG,
                GTSDependencies.MXPARSER
        );
    }

    private void copyResource(Path path, Path destination) {
        if(!Files.exists(destination.resolve(path))) {
            try (InputStream resource = this.resource(path.toString().replace("\\", "/"))) {
                Files.createDirectories(destination.resolve(path).getParent());
                Files.copy(resource, destination.resolve(path));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
