package net.impactdev.gts.commands.executors.safety;

import com.google.common.collect.Lists;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.commands.annotations.Alias;
import net.impactdev.gts.api.commands.annotations.Permission;
import net.impactdev.gts.sponge.commands.SpongeGTSCmdExecutor;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.plugin.permissions.GTSPermissions;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.services.text.MessageService;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

@Alias("gts")
@Permission(GTSPermissions.DEFAULT)
public class SafeModeExecutor extends SpongeGTSCmdExecutor {

    public SafeModeExecutor(GTSPlugin plugin) {
        super(plugin);
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[0];
    }

    @Override
    public SpongeGTSCmdExecutor[] getSubCommands() {
        return new SpongeGTSCmdExecutor[0];
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);
        src.sendMessage(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.SAFE_MODE_FEEDBACK),
                Lists.newArrayList(() -> GTSService.getInstance().getSafeModeReason())));

        return CommandResult.success();
    }

}
