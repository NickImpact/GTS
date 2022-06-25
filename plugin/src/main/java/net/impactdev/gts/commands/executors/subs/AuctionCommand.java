package net.impactdev.gts.commands.executors.subs;

import io.leangen.geantyref.TypeToken;
import net.impactdev.gts.api.commands.GTSCommandExecutor;
import net.impactdev.gts.api.commands.CommandGenerator;
import net.impactdev.gts.api.commands.annotations.Alias;
import net.impactdev.gts.api.commands.annotations.Permission;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.entries.Entry;
import net.impactdev.gts.api.listings.entries.EntryManager;
import net.impactdev.gts.api.listings.manager.ListingManager;
import net.impactdev.gts.api.listings.ui.EntrySelection;
import net.impactdev.gts.commands.elements.EntryManagerElement;
import net.impactdev.gts.commands.elements.PercentageElement;
import net.impactdev.gts.commands.elements.PositiveBoundedElement;
import net.impactdev.gts.commands.elements.TimeElement;
import net.impactdev.gts.commands.elements.selling.SellingContext;
import net.impactdev.gts.commands.executors.utility.PlayerRequiredExecutor;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.plugin.permissions.GTSPermissions;
import net.impactdev.gts.sponge.commands.SpongeGTSCmdExecutor;
import net.impactdev.gts.sponge.listings.makeup.SpongeEntry;
import net.impactdev.gts.sponge.listings.ui.AbstractSpongeEntryUI;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.platform.players.PlatformPlayer;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.impactor.api.utilities.Time;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.Flag;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.pagination.PaginationList;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Collectors;

@Alias("auction")
@Permission(GTSPermissions.DEFAULT)
public class AuctionCommand extends PlayerRequiredExecutor {

    private final Parameter.Value<Double> STARTING = Parameter.builder(Double.class)
            .key("starting")
            .addParser(new PositiveBoundedElement<>(Double::parseDouble))
            .build();

    private final Parameter.Value<Float> INCREMENT = Parameter.builder(Float.class)
            .key("increment")
            .addParser(new PercentageElement())
            .optional()
            .build();
    private final Parameter.Value<EntryManager<?>> ENTRY = Parameter.builder(new TypeToken<EntryManager<?>>() {})
            .key("entry")
            .usage(key -> "A valid entry type")
            .addParser(new EntryManagerElement())
            .optional()
            .build();
    private final Parameter.Value<String> ARGUMENTS = Parameter.remainingJoinedStrings()
            .key("arguments")
            .optional()
            .build();
    private final Parameter.Value<Time> TIME = Parameter.builder(Time.class)
            .key("time")
            .addParser(new TimeElement())
            .optional()
            .build();

    public AuctionCommand() {
        super(GTSPlugin.instance());
    }

    @Override
    public Parameter[] arguments() {
        return new Parameter[] {
                this.STARTING,
                this.INCREMENT,
                this.ENTRY,
                this.ARGUMENTS
        };
    }

    @Override
    public Flag[] flags() {
        return new Flag[] {
                Flag.builder()
                        .alias("time")
                        .alias("t")
                        .setParameter(this.TIME)
                        .build()
        };
    }

    @Override
    public SpongeGTSCmdExecutor[] children() {
        return new SpongeGTSCmdExecutor[]{
                new Helper(this.plugin)
        };
    }

    @Override
    protected CommandResult process(ServerPlayer source, CommandContext context) throws CommandException {
        MessageService service = Utilities.PARSER;
        Config main = GTSPlugin.instance().configuration().main();
        Config lang = GTSPlugin.instance().configuration().language();

        double start = context.requireOne(this.STARTING);
        float increment = context.requireOne(this.INCREMENT);
        long duration = context.one(this.TIME).orElse(main.get(ConfigKeys.LISTING_TIME_MID)).getTime();

        EntryManager<?> manager = context.requireOne(this.ENTRY);

        if(!context.hasAny(this.ARGUMENTS)) {
            PlatformPlayer platform = PlatformPlayer.from(source);
            ((AbstractSpongeEntryUI<?>) manager.getSellingUI(platform).get()).open(platform, true);
            return CommandResult.success();
        }

        Queue<String> remaining = Arrays.stream(context.<String>requireOne(this.ARGUMENTS).split(" "))
                .collect(Collectors.toCollection(LinkedList::new));

        try {
            CommandGenerator.Context.AuctionContext ctx = new SellingContext.SellingAuctionContext();
            ctx.time(duration);
            ctx.increment(increment);
            ctx.start(start);
            EntrySelection<? extends Entry<?, ?>> entry = manager.getEntryCommandCreator().create(source.uniqueId(), remaining, ctx);
            if(ctx.redirect()) {
                return CommandResult.success();
            }

            Auction auction = Auction.builder()
                    .start(start)
                    .increment(increment)
                    .entry(entry.createFromSelection())
                    .lister(source.uniqueId())
                    .expiration(LocalDateTime.now().plusSeconds(duration))
                    .build();
            Impactor.getInstance().getRegistry().get(ListingManager.class).list(source.uniqueId(), auction);
        } catch (Throwable e) {
            throw new CommandException(Component.text("Failure during execution: ").append(Component.text(e.getMessage())), e);
        }
        return CommandResult.success();
    }

    @Alias("help")
    @Permission(GTSPermissions.DEFAULT)
    public static class Helper extends SpongeGTSCmdExecutor {

        public Helper(GTSPlugin plugin) {
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
            Config lang = GTSPlugin.instance().configuration().language();
            PaginationList.builder()
                    .title(service.parse(lang.get(MsgConfigKeys.AUCTION_COMMAND_HEADER)))
                    .padding(Component.text("=").color(NamedTextColor.GOLD))
                    .contents(service.parse(lang.get(MsgConfigKeys.AUCTION_COMMAND_USAGE)))
                    .linesPerPage(7)
                    .sendTo(context.cause().audience());
            return CommandResult.success();
        }
    }
}
