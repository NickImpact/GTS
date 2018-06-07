package com.nickimpact.gts.api.commands;

import com.google.common.collect.Lists;
import com.nickimpact.gts.GTS;
import com.nickimpact.gts.GTSInfo;
import com.nickimpact.gts.api.commands.annotations.AdminCmd;
import com.nickimpact.gts.api.commands.annotations.CommandAliases;
import com.nickimpact.gts.api.commands.annotations.Parent;
import com.nickimpact.gts.api.utils.MessageUtils;
import lombok.Getter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nullable;
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
    @Getter private String permission = "???";

    public SpongeCommand()
    {
        if(!hasProperAnnotations())
        {

            GTS.getInstance().getConsole().ifPresent(console -> console.sendMessages(
                    Text.of(GTSInfo.ERROR, "======= Invalid Command Structure ======="),
                    Text.of(GTSInfo.ERROR, "Executor: ", TextColors.RED, this.getClass().getSimpleName()),
                    Text.of(GTSInfo.ERROR, "Reason: ", TextColors.RED, "Missing header annotation"),
                    Text.of(GTSInfo.ERROR, "=========================================")
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

    public abstract Text getUsage();

    public abstract SpongeCommand[] getSubCommands();

    public CommandSpec getCommandSpec()
    {
    	CommandSpec.Builder cb = CommandSpec.builder();
    	if(!(this instanceof SpongeSubCommand)) {
		    this.permission = formPermission(null, this.getAllAliases().get(0));
	    }
	    cb.permission(this.permission);

        SpongeCommand[] subCmds = getSubCommands();
        HashMap<List<String>, CommandSpec> subCommands = new HashMap<>();
        if (subCmds != null && subCmds.length > 0)
            for (SpongeCommand cmd : subCmds)
            {
                cmd.permission = formPermission(this.getClass().isAnnotationPresent(Parent.class) ? this.getAllAliases().get(0) : null, cmd.getAllAliases().get(0));
                subCommands.put(cmd.getAllAliases(), cmd.getCommandSpec());
            }

        CommandElement[] args = getArgs();
        if (args == null || args.length == 0)
            args = new CommandElement[]{GenericArguments.none()};

        return cb.children(subCommands)
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
        return src.hasPermission(this.permission + "." + suffix);
    }

    public void sendCommandUsage(CommandSource src)
    {
        src.sendMessage(getDescription());
    }

    private String formPermission(@Nullable String parent, String alias) {
    	String perm = GTSInfo.ID + ".command.";
    	if((this.getClass().isAnnotationPresent(AdminCmd.class))) {
    		perm += "admin.";
	    }

	    if(parent != null) {
    		perm += parent + ".";
	    }
	    perm += alias;

    	return perm;
    }
}
