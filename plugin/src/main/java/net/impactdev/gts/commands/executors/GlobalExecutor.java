package net.impactdev.gts.commands.executors;

import com.google.common.collect.Lists;
import net.impactdev.gts.api.commands.annotations.Alias;
import net.impactdev.gts.api.commands.annotations.Permission;
import net.impactdev.gts.commands.executors.subs.AdminExecutor;
import net.impactdev.gts.commands.executors.subs.AuctionCommand;
import net.impactdev.gts.commands.executors.subs.SellCommand;
import net.impactdev.gts.commands.executors.utility.PlayerRequiredExecutor;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.plugin.permissions.GTSPermissions;
import net.impactdev.gts.sponge.commands.SpongeGTSCmdExecutor;
import net.impactdev.gts.ui.SpongeMainMenu;
import net.impactdev.impactor.api.configuration.Config;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.Flag;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.List;

@Alias("gts")
@Permission(GTSPermissions.DEFAULT)
public class GlobalExecutor extends PlayerRequiredExecutor {

    public GlobalExecutor(GTSPlugin plugin) {
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
        List<SpongeGTSCmdExecutor> children = Lists.newArrayList();
        children.add(new AdminExecutor());

        Config config = GTSPlugin.instance().configuration().main();
        if(config.get(ConfigKeys.BINS_ENABLED)) {
            children.add(new SellCommand());
            if(config.get(ConfigKeys.AUCTIONS_ENABLED)) {
                children.add(new AuctionCommand());
            }
        } else if(config.get(ConfigKeys.AUCTIONS_ENABLED)) {
            children.add(new AuctionCommand());
        } else {
            children.add(new SellCommand());
        }

        return children.toArray(new SpongeGTSCmdExecutor[0]);
    }

    @Override
    public CommandResult process(ServerPlayer source, CommandContext context) throws CommandException {
        new SpongeMainMenu(source).open();
        return CommandResult.success();
    }
}
