package net.impactdev.gts.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.impactdev.gts.common.plugin.bootstrap.GTSBootstrap;
import net.impactdev.impactor.api.dependencies.classloader.PluginClassLoader;
import net.impactdev.impactor.api.logging.Logger;
import net.impactdev.impactor.velocity.logging.VelocityLogger;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

@Plugin(id = "gts", name = "GTS", version = "@version@", dependencies = @Dependency(id = "impactor"))
public class GTSVelocityBootstrap implements GTSBootstrap {

    private final GTSVelocityPlugin plugin;

    private final ProxyServer proxy;
    private final Logger logger;
    private final Path configDir;

    @Inject
    public GTSVelocityBootstrap(ProxyServer server, @DataDirectory Path configDir) {
        this.plugin = new GTSVelocityPlugin(this);
        this.proxy = server;
        this.logger = new VelocityLogger(this.plugin);
        this.configDir = configDir;
    }

    @Subscribe
    public void onProxyInit(ProxyInitializeEvent event) {
        try {
            this.plugin.init();
        } catch (Exception e) {
            //exception = e;
            e.printStackTrace();
        }
    }

    public ProxyServer getProxy() {
        return this.proxy;
    }

    @Override
    public Logger getPluginLogger() {
        return this.logger;
    }

    @Override
    public Path getDataDirectory() {
        return this.configDir;
    }

    @Override
    public Path getConfigDirectory() {
        return this.configDir;
    }

    @Override
    public InputStream getResourceStream(String path) {
        return null;
    }

    @Override
    public PluginClassLoader getPluginClassLoader() {
        return null;
    }

    @Override
    public Optional<Throwable> getLaunchError() {
        return Optional.empty();
    }

    @Override
    public void disable() {

    }
}
