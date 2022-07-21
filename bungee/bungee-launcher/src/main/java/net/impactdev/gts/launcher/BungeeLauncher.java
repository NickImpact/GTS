package net.impactdev.gts.launcher;

import net.impactdev.impactor.launcher.ImpactorPluginLauncher;
import net.impactdev.impactor.launcher.JarInJarClassLoader;
import net.impactdev.impactor.launcher.LaunchablePlugin;
import net.impactdev.impactor.launcher.LauncherBootstrap;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeLauncher extends Plugin implements LaunchablePlugin {

    private static final String INTERNAL_JAR = "gts-bungee.jarinjar";
    private static final String BOOTSTRAP_CLASS = "net.impactdev.gts.bungee.GTSBungeeBootstrap";

    private final LauncherBootstrap plugin;

    public BungeeLauncher() {
        this.plugin = ImpactorPluginLauncher.get().bootstrap(this.getClass().getClassLoader(), this);
    }

    @Override
    public void onEnable() {
        this.plugin.construct();
    }

    @Override
    public String path() {
        return INTERNAL_JAR;
    }

    @Override
    public String bootstrapper() {
        return BOOTSTRAP_CLASS;
    }

    @Override
    public LauncherBootstrap create(JarInJarClassLoader loader) {
        return loader.instantiatePlugin(this.bootstrapper(), Plugin.class, this);
    }
}
