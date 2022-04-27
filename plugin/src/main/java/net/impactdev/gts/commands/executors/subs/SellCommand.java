package net.impactdev.gts.commands.executors.subs;

import io.leangen.geantyref.TypeToken;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.commands.CommandGenerator;
import net.impactdev.gts.api.commands.annotations.Alias;
import net.impactdev.gts.api.commands.annotations.Permission;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.api.listings.entries.Entry;
import net.impactdev.gts.api.listings.entries.EntryManager;
import net.impactdev.gts.api.listings.manager.ListingManager;
import net.impactdev.gts.api.listings.prices.Price;
import net.impactdev.gts.api.listings.prices.PriceManager;
import net.impactdev.gts.api.listings.ui.EntrySelection;
import net.impactdev.gts.commands.elements.EntryManagerElement;
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
import net.impactdev.gts.sponge.listings.ui.creator.SpongeEntryTypeSelectionMenu;
import net.impactdev.gts.sponge.pricing.provided.MonetaryPrice;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.Config;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * The Sell command processes requests to sell an item with minimal UI requirements.
 *
 * The proper usage for the command follows as such:
 * <br>
 * <code>/gts sell</code> - Navigates directly to the sell prompt<br>
 * <code>/gts sell (entry type)</code> - Navigates directly to the sell prompt for the particular entry type<br>
 * <code>/gts sell --time (time) (entry type)</code> - Navigates directly to the sell prompt of the entry type with the time pre-configured<br>
 * <code>/gts sell (entry type) (entry args)</code> - Navigates directly to sell prompt of the entry type with identified entry<br>
 * <code>/gts sell --time (time) (entry type) (entry args)</code> - Navigates directly to the entry prompt with all pre-configured settings<br>
 * <code>/gts sell --time (time) (entry type) (entry args) (price type)</code> - Navigates directly to the price selection for the particular price, configured with settings regarding the entry and price<br>
 * <code>/gts sell --time (time) (entry type) (entry args) (price type) (price arguments)</code> - Navigates directly to the price selection for the particular price, configured with settings regarding the entry and price<br>
 */
@Alias("sell")
@Permission(GTSPermissions.DEFAULT)
public class SellCommand extends PlayerRequiredExecutor {

    private final Parameter.Value<EntryManager<?, ?>> ENTRY = Parameter.builder(new TypeToken<EntryManager<?, ?>>() {})
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

    private final Parameter.Value<?>[] all = new Parameter.Value<?>[] { this.ENTRY, this.ARGUMENTS };

    public SellCommand() {
        super(GTSPlugin.instance());
    }

    @Override
    public Parameter[] arguments() {
        return this.all;
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

        if(!this.checkForArguments(context)) {
            new SpongeEntryTypeSelectionMenu(source).open();
            return CommandResult.success();
        }

        long duration = context.one(this.TIME).orElse(main.get(ConfigKeys.LISTING_TIME_MID)).getTime();

        EntryManager<?, ?> manager = context.requireOne(this.ENTRY);
        if(!context.hasAny(this.ARGUMENTS)) {
            ((AbstractSpongeEntryUI<EntrySelection<? extends SpongeEntry<?>>>)((EntryManager<? extends Entry<?, ?>, Player>) manager).getSellingUI(source)
                    .get())
                    .open(source);
            return CommandResult.success();
        }

        CommandGenerator.Context ctx = new SellingContext();
        ctx.time(duration);
        Queue<String> arguments = Arrays.stream(context.requireOne(this.ARGUMENTS).split(" "))
                .collect(Collectors.toCollection(LinkedList::new));
        try {
            EntrySelection<? extends Entry<?, ?>> entry = manager.getEntryCommandCreator().create(source.uniqueId(), arguments, ctx);
            if(ctx.redirect()) {
                return CommandResult.success();
            }

            if(!arguments.isEmpty()) {
                String next = arguments.peek();
                Optional<? extends PriceManager<? extends Price<?, ?, ?>>> pricing = GTSService.getInstance()
                        .getGTSComponentManager()
                        .getAllPriceManagers()
                        .entrySet()
                        .stream()
                        .filter(m -> Arrays.asList(m.getKey().value()).contains(next))
                        .map(Map.Entry::getValue)
                        .findAny();

                // Verify a pricing manager was located. If none was specified, default to parsing a monetary price.
                if(pricing.isPresent()) {
                    arguments.poll();
                    Price<?, ?, ?> result = pricing.get().getPriceCommandCreator().create(source.uniqueId(), arguments, ctx);
                    if(ctx.redirect()) {
                        return CommandResult.success();
                    }
                    Impactor.getInstance().getRegistry().get(ListingManager.class)
                            .list(source.uniqueId(), BuyItNow.builder()
                                    .lister(source.uniqueId())
                                    .entry(entry.createFromSelection())
                                    .price(result)
                                    .expiration(LocalDateTime.now().plusSeconds(duration))
                                    .build()
                            );
                } else {
                    MonetaryPrice.MonetaryPriceManager fallback = GTSService.getInstance().getGTSComponentManager()
                            .getPriceManager("currency")
                            .map(m -> (MonetaryPrice.MonetaryPriceManager) m)
                            .orElseThrow(() -> new IllegalStateException("Unable to locate monetary fallback price"));
                    MonetaryPrice result = (MonetaryPrice) fallback.getPriceCommandCreator()
                            .create(source.uniqueId(), arguments, ctx);

                    Impactor.getInstance().getRegistry().get(ListingManager.class)
                            .list(source.uniqueId(), BuyItNow.builder()
                                    .lister(source.uniqueId())
                                    .entry(entry.createFromSelection())
                                    .price(result)
                                    .expiration(LocalDateTime.now().plusSeconds(duration))
                                    .build()
                            );
                }
            } else {
                ((AbstractSpongeEntryUI<EntrySelection<? extends Entry<?, ?>>>)((EntryManager<?, Player>) manager)
                        .getSellingUI(source)
                        .get())
                        .open(source, entry, false, ctx.time());
            }
        } catch (Throwable e) {
            throw new CommandException(Component.text("Failure during execution: ").append(Component.text(e.getMessage())), e);
        }
        return CommandResult.success();
    }

    private boolean checkForArguments(CommandContext context) {
        boolean available = false;
        Iterator<Parameter.Value<?>> iterator = Arrays.stream(this.all).iterator();
        while(!available && iterator.hasNext()) {
            Parameter.Value<?> key = iterator.next();
            available = context.hasAny(key) || context.hasFlag(key.key().key());
        }

        return available;
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
                    .title(service.parse(lang.get(MsgConfigKeys.SELL_COMMAND_HEADER)))
                    .padding(Component.text("=").color(NamedTextColor.GOLD))
                    .contents(service.parse(lang.get(MsgConfigKeys.SELL_COMMAND_USAGE)))
                    .linesPerPage(7)
                    .sendTo(context.cause().audience());
            return CommandResult.success();
        }
    }

}
