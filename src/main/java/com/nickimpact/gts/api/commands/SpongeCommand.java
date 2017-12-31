package com.nickimpact.gts.api.commands;

import com.google.common.collect.Lists;
import com.nickimpact.gts.GTS;
import com.nickimpact.gts.GTSInfo;
import com.nickimpact.gts.api.commands.annotations.AdminCmd;
import com.nickimpact.gts.api.commands.annotations.CommandAliases;
import com.nickimpact.gts.api.utils.MessageUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.HashMap;
import java.util.List;

/**
 * This class will represent a command built off the Sponge Command API, and will
 * serve as the basis for commands as they are written for a cleaner and much easier
 * registration process.
 *
 * @author NickImpact
 */
public abstract class SpongeCommand implements CommandExecutor
{
    private String basePermission;

    public SpongeCommand()
    {
        if(!hasProperAnnotations())
        {

            GTS.getInstance().getConsole().ifPresent(console -> console.sendMessages(
                    Text.of(GTSInfo.ERROR_PREFIX, "======= Invalid Command Structure ======="),
                    Text.of(GTSInfo.ERROR_PREFIX, "Executor: ", TextColors.RED, this.getClass().getSimpleName()),
                    Text.of(GTSInfo.ERROR_PREFIX, "Reason: ", TextColors.RED, "Missing header annotation"),
                    Text.of(GTSInfo.ERROR_PREFIX, "=========================================")
            ));

        }
    }

    public boolean hasProperAnnotations()
    {
        return this.getClass().isAnnotationPresent(CommandAliases.class);
    }

    public List<String> getAllAliases()
    {
        return Lists.newArrayList(this.getClass().getAnnotation(CommandAliases.class).value());
    }

    public abstract CommandElement[] getArgs();

    public abstract Text getDescription();

    public abstract SpongeCommand[] getSubCommands();

    private CommandSpec getCommandSpec()
    {
        this.basePermission = GTSInfo.ID + ".command." + (this.getClass().isAnnotationPresent(AdminCmd.class) ? "admin." : "") + getAllAliases().get(0);

        SpongeCommand[] subCmds = getSubCommands();
        HashMap<List<String>, CommandSpec> subCommands = new HashMap<>();
        if (subCmds != null && subCmds.length > 0)
            for (SpongeCommand cmd : subCmds)
            {
                cmd.basePermission = this.basePermission + "." + cmd.getAllAliases().get(0);
                subCommands.put(cmd.getAllAliases(), cmd.getCommandSpec());
            }

        CommandElement[] args = getArgs();
        if (args == null || args.length == 0)
            args = new CommandElement[]{GenericArguments.none()};

        return CommandSpec.builder()
                .children(subCommands)
                .permission(this.basePermission)
                .description(getDescription())
                .executor(this)
                .arguments(args)
                .build();
    }

    public void register()
    {
        try
        {
            if(this.hasProperAnnotations())
                Sponge.getCommandManager().register(GTS.getInstance(), getCommandSpec(), getAllAliases());
            else
                MessageUtils.genAndSendErrorMessage(
                        "Invalid Command",
                        "Offender: " + this.getClass().getName(),
                        "Reason: Lack of 'CommandAliases' annotation"
                );
        }
        catch (IllegalArgumentException iae)
        {
            iae.printStackTrace();
        }
    }

    public boolean testPermissionSuffix(CommandSource src, String suffix) {
        return src.hasPermission(this.basePermission + "." + suffix);
    }

    public void sendCommandUsage(CommandSource src)
    {
        src.sendMessage(getDescription());
    }
}
