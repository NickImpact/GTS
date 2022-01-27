package net.impactdev.gts.commands.executors.subs;

import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.commands.GTSCommandExecutor;
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
import net.impactdev.gts.commands.elements.selling.SellingContext;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.plugin.permissions.GTSPermissions;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;
import net.impactdev.gts.sponge.commands.SpongeGTSCmdExecutor;
import net.impactdev.gts.sponge.commands.TimeElement;
import net.impactdev.gts.sponge.listings.makeup.SpongeEntry;
import net.impactdev.gts.sponge.listings.ui.AbstractSpongeEntryUI;
import net.impactdev.gts.sponge.listings.ui.creator.SpongeEntryTypeSelectionMenu;
import net.impactdev.gts.sponge.pricing.provided.MonetaryPrice;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * The Sell command processes requests to sell an item with minimal UI requirements.
 *
 * The proper usage for the command follows as such:
 * <br>
 * <code>/gts sell</code> - Navigates directly to the sell prompt<br>
 * <code>/gts sell (entry type)</code> - Navigates directly to the sell prompt for the particular entry type<br>
 * <code>/gts sell --time=(time) (entry type)</code> - Navigates directly to the sell prompt of the entry type with the time pre-configured<br>
 * <code>/gts sell (entry type) (entry args)</code> - Navigates directly to sell prompt of the entry type with identified entry<br>
 * <code>/gts sell --time=(time) (entry type) (entry args)</code> - Navigates directly to the entry prompt with all pre-configured settings<br>
 * <code>/gts sell --time=(time) (entry type) (entry args) (price type)</code> - Navigates directly to the price selection for the particular price, configured with settings regarding the entry and price<br>
 * <code>/gts sell --time=(time) (entry type) (entry args) (price type) (price arguments)</code> - Navigates directly to the price selection for the particular price, configured with settings regarding the entry and price<br>
 */
@Alias("sell")
@Permission(GTSPermissions.DEFAULT)
public class SellCommand extends SpongeGTSCmdExecutor {

    private final Text ENTRY = Text.of("entry");
    private final Text ARGUMENTS = Text.of("arguments");
    private final Text TIME = Text.of("time");

    private final Text[] all = new Text[] { this.ENTRY, this.ARGUMENTS, this.TIME };

    public SellCommand() {
        super(GTSPlugin.getInstance());
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.flags()
                        .valueFlag(new TimeElement(this.TIME), "-time")
                        .setAnchorFlags(true)
                        .buildWith(GenericArguments.seq(
                            GenericArguments.optional(new EntryManagerElement(this.ENTRY)),
                            GenericArguments.optional(GenericArguments.remainingJoinedStrings(this.ARGUMENTS))
                        ))
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
        if(!this.checkForArguments(args)) {
            new SpongeEntryTypeSelectionMenu(source).open();
            return CommandResult.success();
        }

        long duration = args.<Time>getOne(this.TIME).orElse(main.get(ConfigKeys.LISTING_TIME_MID)).getTime();

        EntryManager<?, ?> manager = args.requireOne(this.ENTRY);
        if(!args.hasAny(this.ARGUMENTS)) {
            ((AbstractSpongeEntryUI<EntrySelection<? extends SpongeEntry<?>>>)((EntryManager<? extends Entry<?, ?>, Player>) manager).getSellingUI(source)
                    .get())
                    .open(source);
            return CommandResult.success();
        }

        CommandGenerator.Context context = new SellingContext();
        context.time(duration);
        Queue<String> arguments = Arrays.stream(args.<String>requireOne(this.ARGUMENTS).split(" "))
                    .collect(Collectors.toCollection(LinkedList::new));
        try {
            EntrySelection<? extends Entry<?, ?>> entry = manager.getEntryCommandCreator().create(source.getUniqueId(), arguments, context);
            if(context.redirect()) {
                return CommandResult.success();
            }

            if(!arguments.isEmpty()) {
                String next = arguments.peek();
                Optional<? extends PriceManager<? extends Price<?, ?, ?>, ?>> pricing = GTSService.getInstance()
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
                    Price<?, ?, ?> result = pricing.get().getPriceCommandCreator().create(source.getUniqueId(), arguments, context);
                    if(context.redirect()) {
                        return CommandResult.success();
                    }
                    Impactor.getInstance().getRegistry().get(ListingManager.class)
                            .list(source.getUniqueId(), BuyItNow.builder()
                                    .lister(source.getUniqueId())
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
                            .create(source.getUniqueId(), arguments, context);

                    Impactor.getInstance().getRegistry().get(ListingManager.class)
                            .list(source.getUniqueId(), BuyItNow.builder()
                                    .lister(source.getUniqueId())
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
                        .open(source, entry, false, context.time());
            }
        } catch (Throwable e) {
            throw new CommandException(Text.of("Failure during execution: ", e.getMessage()), e);
        }
        return CommandResult.success();
    }

    private boolean checkForArguments(CommandContext context) {
        boolean available = false;
        Iterator<Text> iterator = Arrays.stream(this.all).iterator();
        while(!available && iterator.hasNext()) {
            Text key = iterator.next();
            available = context.hasAny(key) || context.hasFlag(key);
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
                    .title(service.parse(lang.get(MsgConfigKeys.SELL_COMMAND_HEADER)))
                    .padding(Text.of(TextColors.GOLD, "="))
                    .contents(service.parse(lang.get(MsgConfigKeys.SELL_COMMAND_USAGE)))
                    .linesPerPage(7)
                    .sendTo(src);
            return CommandResult.success();
        }
    }

}
