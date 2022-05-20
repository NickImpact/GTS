package net.impactdev.gts.commands.executors.safety;

import com.google.common.collect.Lists;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.commands.annotations.Alias;
import net.impactdev.gts.api.commands.annotations.Permission;
import net.impactdev.gts.api.communication.message.errors.ErrorCode;
import net.impactdev.gts.sponge.commands.SpongeGTSCmdExecutor;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.plugin.permissions.GTSPermissions;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.placeholders.PlaceholderSources;
import net.impactdev.impactor.api.services.text.MessageService;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.Flag;

@Alias("gts")
@Permission(GTSPermissions.DEFAULT)
public class SafeModeExecutor extends SpongeGTSCmdExecutor {

    public SafeModeExecutor(GTSPlugin plugin) {
        super(plugin);
    }

    @Override
    public Parameter[] arguments() {
        return new Parameter[0];
    }

    @Override
    public Flag[] flags() {
        return new Flag[0];
    }

    @Override
    public SpongeGTSCmdExecutor[] children() {
        return new SpongeGTSCmdExecutor[0];
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        MessageService service = Impactor.getInstance().getRegistry().get(MessageService.class);
        context.cause().audience().sendMessage(
                service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.SAFE_MODE_FEEDBACK),
                PlaceholderSources.builder().append(ErrorCode.class, () -> GTSService.getInstance().getSafeModeReason()).build()));

        return CommandResult.success();
    }

}
