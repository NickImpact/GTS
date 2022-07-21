package net.impactdev.gts.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.impactdev.gts.common.plugin.bootstrap.GTSBootstrap;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;
import net.impactdev.impactor.api.logging.PluginLogger;
import net.impactdev.impactor.api.logging.Slf4jLogger;
import org.slf4j.Logger;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

@Plugin(id = "gts", name = "GTS", version = "@version@", dependencies = @Dependency(id = "impactor"))
public class GTSVelocityBootstrap implements GTSBootstrap {

    private final GTSVelocityPlugin plugin;

    private final ProxyServer proxy;
    private final PluginLogger logger;
    private final Path configDir;

    @Inject
    public GTSVelocityBootstrap(ProxyServer server, Logger delegate, @DataDirectory Path configDir) {
        this.plugin = new GTSVelocityPlugin(this);
        this.proxy = server;
        this.logger = new Slf4jLogger(delegate);
        this.configDir = configDir;
    }

    @Subscribe
    public void onProxyInit(ProxyInitializeEvent event) {
        try {
            this.plugin.construct();
        } catch (Exception e) {
            //exception = e;
            ExceptionWriter.write(e);
        }
    }

    public ProxyServer getProxy() {
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
        return this.configDir;
    }

    @Override
    public Optional<InputStream> resource(Path path) {
        return Optional.ofNullable(this.getClass().getClassLoader().getResourceAsStream(path.toString().replace("\\", "/")));
    }

    @Override
    public Optional<Throwable> launchError() {
        return Optional.empty();
    }

    @Override
    public void construct() {}

    @Override
    public void shutdown() {}
}
