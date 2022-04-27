package net.impactdev.gts.commands;

import com.google.common.collect.Lists;
import net.impactdev.gts.api.commands.CommandRegistrar;
import net.impactdev.gts.commands.executors.GlobalExecutor;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;
import net.impactdev.gts.sponge.commands.SpongeGTSCmdExecutor;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.plugin.PluginContainer;

import java.util.List;

public class GTSCommandManager implements CommandRegistrar<Command.Parameterized, SpongeGTSCmdExecutor> {

    private final PluginContainer container;
    private final RegisterCommandEvent<Command.Parameterized> event;

    public GTSCommandManager(PluginContainer container, RegisterCommandEvent<Command.Parameterized> event) {
        this.container = container;
        this.event = event;
    }

    @Override
    public void register(SpongeGTSCmdExecutor executor) {
        try {
            String primary = executor.aliases().get(0);
            List<String> remaining = Lists.newArrayList(executor.aliases());
            remaining.remove(0);

            this.event.register(this.container, executor.build(), primary, remaining.toArray(new String[]{}));
        } catch (Exception e) {
            e.printStackTrace();
            ExceptionWriter.write(e);
        }
    }
}
