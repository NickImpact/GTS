package net.impactdev.gts.sponge.commands;

import com.google.common.collect.Lists;
import net.impactdev.gts.api.commands.GTSCommandExecutor;
import net.impactdev.gts.api.commands.annotations.Alias;
import net.impactdev.gts.api.commands.annotations.Permission;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class GTSCmdExecutor implements CommandExecutor, GTSCommandExecutor<CommandElement, CommandSpec> {

    protected final GTSPlugin plugin;
    private final String permission;

    public GTSCmdExecutor(GTSPlugin plugin) {
        this.plugin = plugin;
        if(!this.hasNeededAnnotations()) {
            plugin.getPluginLogger().error("Attempted to create executor with missing information: " + this.getClass().getSimpleName());
        }
        this.permission = this.getClass().getAnnotation(Permission.class).value();
    }

    public void register() {
        try {
            if(this.hasNeededAnnotations()) {
                Sponge.getCommandManager().register(
                        this.plugin.getBootstrap(),
                        this.build(),
                        this.getAliases()
                );
            }
        } catch (Exception e) {
            ExceptionWriter.write(e);
        }
    }

    public List<String> getAliases() {
        return Lists.newArrayList(this.getClass().getAnnotation(Alias.class).value());
    }

    public CommandSpec build() {
        GTSCommandExecutor<CommandElement, CommandSpec>[] subcommands = this.getSubcommands();
        Map<List<String>, CommandSpec> children = new HashMap<>();
        if(subcommands != null) {
            for(GTSCommandExecutor<CommandElement, CommandSpec> child : subcommands) {
                children.put(child.getAliases(), child.build());
            }
        }

        CommandElement[] arguments = this.getArguments();
        if(arguments == null || arguments.length == 0) {
            arguments = new CommandElement[]{GenericArguments.none()};
        }

        return CommandSpec.builder()
                .children(children)
                .permission(this.permission)
                .executor(this)
                .arguments(arguments)
                .build();
    }

}
