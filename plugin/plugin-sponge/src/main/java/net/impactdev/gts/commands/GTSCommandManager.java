package net.impactdev.gts.commands;

import co.aikar.commands.SpongeCommandManager;
import lombok.RequiredArgsConstructor;
import org.spongepowered.api.plugin.PluginContainer;

@RequiredArgsConstructor
public class GTSCommandManager {

    private final PluginContainer container;

    public void register() {
        SpongeCommandManager commands = new SpongeCommandManager(this.container);
        commands.registerCommand(new GTSCommand());
    }

}
