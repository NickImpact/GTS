package me.nickimpact.gts.bungee;

import com.nickimpact.impactor.api.logging.Logger;
import me.nickimpact.gts.common.plugin.bootstrap.GTSBootstrap;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

public class GTSBungeeBootstrap extends Plugin implements GTSBootstrap {

    private final GTSBungeePlugin plugin;

    private Throwable exception;

    public GTSBungeeBootstrap() {
        this.plugin = new GTSBungeePlugin(this);
    }

    @Override
    public void onLoad() {
        try {
            this.plugin.load();
        } catch (Exception e) {
            exception = e;
            e.printStackTrace();
        }
        this.plugin.getPluginLogger().info("&eTest");
    }

    @Override
    public void onEnable() {
        try {
            this.plugin.enable();
        } catch (Exception e) {
            exception = e;
            e.printStackTrace();
        }
    }

    @Override
    public Logger getPluginLogger() {
        return this.plugin.getPluginLogger();
    }

    @Override
    public Path getDataDirectory() {
        return null;
    }

    @Override
    public Path getConfigDirectory() {
        return null;
    }

    @Override
    public InputStream getResourceStream(String path) {
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
