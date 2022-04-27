package net.impactdev.gts.sponge.commands;

import com.google.common.collect.Lists;
import net.impactdev.gts.api.commands.GTSCommandExecutor;
import net.impactdev.gts.api.commands.annotations.Alias;
import net.impactdev.gts.api.commands.annotations.Permission;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.Flag;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.plugin.PluginContainer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SpongeGTSCmdExecutor implements CommandExecutor, GTSCommandExecutor<Parameter, Flag, Command.Parameterized> {

    protected final GTSPlugin plugin;
    private final String permission;

    public SpongeGTSCmdExecutor(GTSPlugin plugin) {
        this.plugin = plugin;
        if(!this.hasNeededAnnotations()) {
            plugin.logger().error("Attempted to create executor with missing information: " + this.getClass().getSimpleName());
        }
        this.permission = this.getClass().getAnnotation(Permission.class).value();
    }

    public Command.Parameterized build() {
        if(!this.hasNeededAnnotations()) {
            throw new IllegalStateException("Missing required annotations for command class: " + this.getClass().getSimpleName());
        }

        GTSCommandExecutor<Parameter, Flag, Command.Parameterized>[] subcommands = this.children();
        Map<List<String>, Command.Parameterized> children = new HashMap<>();
        if(subcommands != null) {
            for(GTSCommandExecutor<Parameter, Flag, Command.Parameterized> child : subcommands) {
                children.put(child.aliases(), child.build());
            }
        }

        Parameter[] arguments = this.arguments();
        if(arguments == null || arguments.length == 0) {
            arguments = new Parameter[]{};
        }

        Command.Builder builder = Command.builder();
        builder.addParameters(arguments);

        Flag[] flags = this.flags();
        if(flags == null || flags.length == 0) {
            flags = new Flag[]{};
        }

        builder.addFlags(flags);

        for(Map.Entry<List<String>, Command.Parameterized> entry : children.entrySet()) {
            builder.addChild(entry.getValue(), entry.getKey());
        }

        return builder.permission(this.permission)
                .executor(this)
                .build();
    }

}
