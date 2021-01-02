package net.impactdev.gts.commands;

import co.aikar.commands.SpongeCommandManager;
import org.spongepowered.api.plugin.PluginContainer;

public class GTSCommandManager {

    private final PluginContainer container;

    public GTSCommandManager(PluginContainer container) {
        this.container = container;
    }

    public void register() {
        SpongeCommandManager commands = new SpongeCommandManager(this.container);
        commands.registerCommand(new GTSCommand());
    }

}
