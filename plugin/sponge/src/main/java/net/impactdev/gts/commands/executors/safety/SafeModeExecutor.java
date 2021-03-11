package net.impactdev.gts.commands.executors.safety;

import net.impactdev.gts.api.messaging.message.Message;
import net.impactdev.gts.commands.annotations.Alias;
import net.impactdev.gts.commands.annotations.Permission;
import net.impactdev.gts.commands.executors.GTSCmdExecutor;
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

import java.util.Optional;

@Alias("gts")
@Permission(GTSPermissions.DEFAULT)
public class SafeModeExecutor extends GTSCmdExecutor {

    public SafeModeExecutor(GTSPlugin plugin) {
        super(plugin);
    }

    @Override
    public Optional<Text> getDescription() {
        return Optional.empty();
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[0];
    }

    @Override
    public GTSCmdExecutor[] getSubcommands() {
        return new GTSCmdExecutor[0];
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);
        src.sendMessage(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.SAFE_MODE_FEEDBACK)));

        return CommandResult.success();
    }

}
