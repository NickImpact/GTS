package net.impactdev.gts.commands.executors.subs;

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
import net.impactdev.gts.commands.elements.selling.SellingContext;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.plugin.permissions.GTSPermissions;
import net.impactdev.gts.sponge.commands.SpongeGTSCmdExecutor;
import net.impactdev.gts.sponge.commands.TimeElement;
import net.impactdev.gts.sponge.listings.makeup.SpongeEntry;
import net.impactdev.gts.sponge.listings.ui.AbstractSpongeEntryUI;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.impactor.api.utilities.Time;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Collectors;

@Alias("auction")
@Permission(GTSPermissions.DEFAULT)
public class AuctionCommand extends SpongeGTSCmdExecutor {

    private final Text STARTING = Text.of("starting");
    private final Text INCREMENT = Text.of("increment");
    private final Text ENTRY = Text.of("entry");
    private final Text ARGUMENTS = Text.of("arguments");

    private final Text TIME = Text.of("time");

    public AuctionCommand() {
        super(GTSPlugin.getInstance());
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.flags()
                        .valueFlag(new TimeElement(this.TIME), "-time")
                        .setAnchorFlags(true)
                        .buildWith(GenericArguments.seq(
                            new PositiveBoundedElement<>(this.STARTING, input -> {
                                try {
                                    return Double.parseDouble(input);
                                } catch (Exception e) {
                                    return null;
                                }
                            }),
                            new PercentageElement(this.INCREMENT),
                            new EntryManagerElement(this.ENTRY),
                            GenericArguments.optional(GenericArguments.remainingJoinedStrings(this.ARGUMENTS))
                        )
                )
        };
    }

    @Override
    public GTSCommandExecutor<CommandElement, CommandSpec>[] getSubCommands() {
        return new GTSCommandExecutor[]{
                new Helper(this.plugin)
        };
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        MessageService<Text> service = Utilities.PARSER;
        Config main = GTSPlugin.getInstance().getConfiguration();
        Config lang = GTSPlugin.getInstance().getMsgConfig();
        if(!(src instanceof Player)) {
            throw new CommandException(service.parse(lang.get(MsgConfigKeys.PLAYER_REQUIRED_COMMAND)));
        }

        Player source = (Player) src;
        double start = args.requireOne(this.STARTING);
        float increment = args.requireOne(this.INCREMENT);
        long duration = args.<Time>getOne(this.TIME).orElse(main.get(ConfigKeys.LISTING_TIME_MID)).getTime();

        EntryManager<?, ?> manager = args.requireOne(this.ENTRY);

        if(!args.hasAny(this.ARGUMENTS)) {
            ((AbstractSpongeEntryUI<EntrySelection<? extends SpongeEntry<?>>>)((EntryManager<? extends Entry<?, ?>, Player>) manager).getSellingUI(source)
                    .get())
                    .open(source, true);
            return CommandResult.success();
        }

        Queue<String> remaining = Arrays.stream(args.<String>requireOne(this.ARGUMENTS).split(" "))
                .collect(Collectors.toCollection(LinkedList::new));

        try {
            CommandGenerator.Context.AuctionContext context = new SellingContext.SellingAuctionContext();
            context.time(duration);
            context.increment(increment);
            context.start(start);
            EntrySelection<? extends Entry<?, ?>> entry = manager.getEntryCommandCreator().create(source.getUniqueId(), remaining, context);
            if(context.redirect()) {
                return CommandResult.success();
            }
            
            Auction auction = Auction.builder()
                    .start(start)
                    .increment(increment)
                    .entry(entry.createFromSelection())
                    .lister(source.getUniqueId())
                    .expiration(LocalDateTime.now().plusSeconds(duration))
                    .build();
            Impactor.getInstance().getRegistry().get(ListingManager.class).list(source.getUniqueId(), auction);
        } catch (Throwable e) {
            throw new CommandException(Text.of("Failure during execution: ", e.getMessage()), e);
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
        public CommandElement[] getArguments() {
            return new CommandElement[0];
        }

        @Override
        public GTSCommandExecutor<CommandElement, CommandSpec>[] getSubCommands() {
            return new GTSCommandExecutor[0];
        }

        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);
            Config lang = GTSPlugin.getInstance().getMsgConfig();
            PaginationList.builder()
                    .title(service.parse(lang.get(MsgConfigKeys.AUCTION_COMMAND_HEADER)))
                    .padding(Text.of(TextColors.GOLD, "="))
                    .contents(service.parse(lang.get(MsgConfigKeys.AUCTION_COMMAND_USAGE)))
                    .linesPerPage(7)
                    .sendTo(src);
            return CommandResult.success();
        }
    }
}
