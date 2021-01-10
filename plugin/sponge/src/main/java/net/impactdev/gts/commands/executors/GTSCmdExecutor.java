package net.impactdev.gts.commands.executors;

import com.google.common.collect.Lists;
import net.impactdev.gts.commands.annotations.Alias;
import net.impactdev.gts.commands.annotations.Permission;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class GTSCmdExecutor implements CommandExecutor {

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

    public abstract Optional<Text> getDescription();

    public abstract CommandElement[] getArguments();

    public abstract GTSCmdExecutor[] getSubcommands();

    protected CommandSpec build() {
        GTSCmdExecutor[] subcommands = this.getSubcommands();
        Map<List<String>, CommandSpec> children = new HashMap<>();
        if(subcommands != null) {
            for(GTSCmdExecutor child : subcommands) {
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
                .description(this.getDescription().orElse(Text.EMPTY))
                .executor(this)
                .arguments(arguments)
                .build();
    }

    private boolean hasNeededAnnotations() {
        return this.getClass().isAnnotationPresent(Alias.class) && this.getClass().isAnnotationPresent(Permission.class);
    }

}
