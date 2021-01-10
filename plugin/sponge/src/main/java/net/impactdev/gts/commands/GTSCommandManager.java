package net.impactdev.gts.commands;

import net.impactdev.gts.commands.executors.GlobalExecutor;
import net.impactdev.gts.common.plugin.GTSPlugin;
import org.spongepowered.api.plugin.PluginContainer;

public class GTSCommandManager {

    private final PluginContainer container;

    public GTSCommandManager(PluginContainer container) {
        this.container = container;
    }

    public void register() {
        new GlobalExecutor(GTSPlugin.getInstance()).register();
    }

}
