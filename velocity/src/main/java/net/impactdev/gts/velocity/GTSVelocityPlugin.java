package net.impactdev.gts.velocity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
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
import net.impactdev.gts.common.data.ResourceManagerImpl;
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
import net.impactdev.impactor.api.plugin.PluginMetadata;
import net.impactdev.impactor.api.storage.StorageType;
import net.impactdev.impactor.velocity.config.VelocityConfig;
import net.impactdev.impactor.velocity.config.VelocityConfigAdapter;
import net.impactdev.impactor.velocity.plugin.AbstractVelocityPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GTSVelocityPlugin extends AbstractVelocityPlugin implements GTSPlugin {

    private static GTSVelocityPlugin plugin;

    private final GTSVelocityBootstrap bootstrap;

    private Config config;

    private GTSStorage storage;
    private InternalMessagingService messagingService;

    private Environment environment;

    public GTSVelocityPlugin(GTSVelocityBootstrap bootstrap) {
        super(PluginMetadata.builder()
                .id("gts")
                .name("GTS")
                .version("@version@")
                .build());
        this.bootstrap = bootstrap;
        plugin = this;
    }

    public void init() {
        ApiRegistrationUtil.register(new GTSAPIProvider());

        Impactor.getInstance().getRegistry().register(GTSPlugin.class, this);
        Impactor.getInstance().getRegistry().register(Blacklist.class, new BlacklistImpl());

        GTSService.getInstance().getGTSComponentManager().registerListingResourceManager(BuyItNow.class, new ResourceManagerImpl<>("BIN", "N/A", VelocityBIN::deserialize));
        GTSService.getInstance().getGTSComponentManager().registerListingResourceManager(Auction.class, new ResourceManagerImpl<>("Auctions", "N/A", VelocityAuction::deserialize));
        Impactor.getInstance().getRegistry().registerBuilderSupplier(Auction.AuctionBuilder.class, VelocityAuction.VelocityAuctionBuilder::new);
        Impactor.getInstance().getRegistry().registerBuilderSupplier(BuyItNow.BuyItNowBuilder.class, VelocityBIN.VelocityBINBuilder::new);
        Impactor.getInstance().getRegistry().registerBuilderSupplier(ForceDeleteMessage.Response.ResponseBuilder.class, ForceDeleteMessageImpl.ForceDeleteResponse.ForcedDeleteResponseBuilder::new);

        this.copyResource(Paths.get("gts.conf"), this.getConfigDir());
        this.config = new VelocityConfig(new VelocityConfigAdapter(this, new File(this.getConfigDir().toFile(), "gts.conf")), new ConfigKeys());
        this.storage = new StorageFactory(this).getInstance(StorageType.MARIADB);

        this.messagingService = this.getMessagingFactory().getInstance();

        new VelocityPingPongInterpreter().register(this);
        new VelocityBINInterpreter().register(this);
        new VelocityAuctionInterpreter().register(this);
        new VelocityListingInterpreter().register(this);
        new VelocityAdminInterpreters().register(this);
    }

    @Override
    public <T extends GTSPlugin> T as(Class<T> type) {
        if(!type.isAssignableFrom(this.getClass())) {
            throw new RuntimeException("Invalid plugin typing");
        }
        return (T) this;
    }

    @Override
    public GTSVelocityBootstrap getBootstrap() {
        return this.bootstrap;
    }

    @Override
    public Environment getEnvironment() {
        Environment environment = Optional.ofNullable(this.environment)
                .orElseGet(() -> (this.environment = new Environment()));
        environment.append(this.bootstrap.getProxy().getVersion().getName(), this.bootstrap.getProxy().getVersion().getVersion() + " - " + this.bootstrap.getProxy().getVersion().getVendor());
        environment.append("Impactor", this.bootstrap.getProxy().getPluginManager().getPlugin("impactor").get().getDescription().getVersion().get());
        environment.append("GTS", this.getMetadata().getVersion());

        return environment;
    }

    @Override
    public Gson getGson() {
        return new GsonBuilder().create();
    }

    @Override
    public GTSStorage getStorage() {
        return this.storage;
    }

    @Override
    public ExtensionManager getExtensionManager() {
        return null;
    }

    @Override
    public InternalMessagingService getMessagingService() {
        return this.messagingService;
    }

    @Override
    public String getPlayerDisplayName(UUID id) {
        return this.getBootstrap().getProxy().getPlayer(id).map(Player::getUsername).orElse("Unknown");
    }

    @Override
    public Path getConfigDir() {
        return this.bootstrap.getConfigDirectory();
    }

    @Override
    public Config getConfiguration() {
        return this.config;
    }

    @Override
    public List<StorageType> getStorageRequirements() {
        return Lists.newArrayList(StorageType.MARIADB);
    }

    @Override
    public Config getMsgConfig() {
        return null;
    }

    public MessagingFactory<?> getMessagingFactory() {
        return new VelocityMessagingFactory(this);
    }

    @Override
    public List<Dependency> getAllDependencies() {
        return Lists.newArrayList(
                Dependency.CAFFEINE,
                Dependency.MXPARSER
        );
    }

    private void copyResource(Path path, Path destination) {
        if(!Files.exists(destination.resolve(path))) {
            try (InputStream resource = this.getResourceStream(path.toString().replace("\\", "/"))) {
                Files.createDirectories(destination.resolve(path).getParent());
                Files.copy(resource, destination.resolve(path));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
