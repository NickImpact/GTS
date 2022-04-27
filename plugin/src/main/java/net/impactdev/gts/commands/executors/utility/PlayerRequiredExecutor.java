package net.impactdev.gts.commands.executors.utility;

import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.sponge.commands.SpongeGTSCmdExecutor;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.services.text.MessageService;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Optional;

public abstract class PlayerRequiredExecutor extends SpongeGTSCmdExecutor {

    public PlayerRequiredExecutor(GTSPlugin plugin) {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        CommandCause cause = context.cause();
        Optional<ServerPlayer> source = cause.first(ServerPlayer.class);
        if(source.isPresent()) {
            return this.process(source.get(), context);
        }

        MessageService service = Utilities.PARSER;
        Config lang = GTSPlugin.instance().configuration().language();
        throw new CommandException(service.parse(lang.get(MsgConfigKeys.PLAYER_REQUIRED_COMMAND)));
    }

    protected abstract CommandResult process(ServerPlayer source, CommandContext context) throws CommandException;
}
