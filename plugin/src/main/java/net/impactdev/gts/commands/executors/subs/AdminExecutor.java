package net.impactdev.gts.commands.executors.subs;

import com.google.common.collect.Lists;
import net.impactdev.gts.api.commands.GTSCommandExecutor;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.storage.GTSStorage;
import net.impactdev.gts.commands.executors.utility.PlayerRequiredExecutor;
import net.impactdev.gts.util.GTSInfoGenerator;
import net.impactdev.gts.api.commands.annotations.Alias;
import net.impactdev.gts.api.commands.annotations.Permission;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.sponge.commands.SpongeGTSCmdExecutor;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.plugin.permissions.GTSPermissions;
import net.impactdev.gts.sponge.pricing.provided.MonetaryPrice;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.gts.ui.admin.SpongeAdminMenu;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.services.text.MessageService;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.Flag;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.pagination.PaginationList;

import java.util.List;


@Alias("admin")
@Permission(GTSPermissions.ADMIN_BASE)
public class AdminExecutor extends PlayerRequiredExecutor {

    public AdminExecutor() {
        super(GTSPlugin.instance());
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
        return new SpongeGTSCmdExecutor[] {
                new Info(this.plugin),
                new Ping(this.plugin),
                new Clean(this.plugin),
                new UserQuery(this.plugin)
        };
    }

    @Override
    protected CommandResult process(ServerPlayer source, CommandContext context) throws CommandException {
        new SpongeAdminMenu(source).open();
        return CommandResult.success();
    }

    @Alias("info")
    @Permission(GTSPermissions.ADMIN_INFO)
    public static class Info extends SpongeGTSCmdExecutor {

        public Info(GTSPlugin plugin) {
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
            MessageService service = Utilities.PARSER;

            new GTSInfoGenerator().create(context.cause().audience()).thenAccept(x -> {
                context.cause().audience().sendMessage(service.parse("{{gts:prefix}} Report saved to: &a" + x));
            });
            return CommandResult.success();
        }

    }

    @Alias("ping")
    @Permission(GTSPermissions.ADMIN_PING)
    public static class Ping extends SpongeGTSCmdExecutor {

        public Ping(GTSPlugin plugin) {
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
            GTSPlugin.instance().messagingService()
                    .sendPing()
                    .thenAccept(pong -> {
                        if(pong.wasSuccessful()) {
                            context.cause().audience().sendMessage(service.parse(
                                    "&eGTS &7\u00bb Ping request &asuccessful&7, took &b" + pong.getResponseTime() + " ms&7!"
                            ));
                        }
                    });
            return CommandResult.success();
        }

    }

//    @Alias("switch-storage")
//    @Permission(GTSPermissions.ADMIN_PING)
//    public static class TranslateStorage extends SpongeGTSCmdExecutor {
//
//        public static final Text TO = Text.of("TO");
//
//        public TranslateStorage(GTSPlugin plugin) {
//            super(plugin);
//        }
//
//        @Override
//        public CommandElement[] getArguments() {
//            return new CommandElement[] {
//                    new StorageTypeElement(TO)
//            };
//        }
//
//        @Override
//        public GTSCommandExecutor<CommandElement, CommandSpec>[] getSubCommands() {
//            return new GTSCommandExecutor[0];
//        }
//
//        @Override
//        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
//            StorageTranslator translator = new StorageTranslator(args.requireOne(TO));
//            src.sendMessage(Text.of(TextColors.GRAY, "Processing storage conversion, please wait..."));
//            translator.run().thenAccept(result -> {
//                src.sendMessage(Text.of(TextColors.GRAY, "Conversion complete!"));
//            });
//            return CommandResult.success();
//        }
//    }

    @Alias("clean")
    @Permission(GTSPermissions.ADMIN_BASE)
    public static class Clean extends SpongeGTSCmdExecutor {

        public Clean(GTSPlugin plugin) {
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

            Audience audience = context.cause().audience();
            audience.sendMessage(service.parse("{{gts:prefix}} Erasing legacy table if it exists..."));
            GTSStorage storage = GTSPlugin.instance().storage();
            if(storage.clean().join()) {
                audience.sendMessage(service.parse("{{gts:prefix}} Legacy data successfully erased!"));
            } else {
                audience.sendMessage(service.parse("{{gts:error}} Legacy data no longer exists..."));
            }

            return CommandResult.success();
        }
    }

    @Alias("user-query")
    @Permission(GTSPermissions.ADMIN_USER_QUERY)
    public static class UserQuery extends SpongeGTSCmdExecutor {

        private final Parameter.Value<ServerPlayer> PLAYER = Parameter.player()
                .key("player")
                .usage(key -> "Any currently logged in player")
                .build();

        public UserQuery(GTSPlugin plugin) {
            super(plugin);
        }

        @Override
        public Parameter[] arguments() {
            return new Parameter[] {
                    PLAYER
            };
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
            ServerPlayer player = context.requireOne(PLAYER);
            GTSPlugin.instance().storage().fetchListings(Lists.newArrayList(
                    listing -> listing.getLister().equals(player.uniqueId())
            )).thenAccept(listings -> {
                MessageService service = Impactor.getInstance().getRegistry().get(MessageService.class);
                PaginationList.Builder builder = PaginationList.builder()
                        .title(service.parse("&e" + player.name() + "'s Known Listings"))
                        .linesPerPage(8);

                List<Component> results = Lists.newArrayList();
                int index = 1;
                for(Listing listing : listings) {
                    results.add(this.listing(listing, index++));
                }
                builder.contents(results);
                builder.sendTo(context.cause().audience());
            });

            return CommandResult.success();
        }

        private Component listing(Listing listing, int index) {
            MessageService service = Impactor.getInstance().getRegistry().get(MessageService.class);
            Component result = service.parse("&7" + index + ": ");

            if(listing instanceof BuyItNow) {
                result = result.append(service.parse("&bBIN &7(&6" + listing.getID() + "&7)"));

                BuyItNow bin = (BuyItNow) listing;
                boolean purchased = bin.isPurchased();
                boolean stashed = bin.stashedForPurchaser() || bin.hasExpired();

                Component PURCHASED = service.parse("&aPurchased");
                Component STASHED = service.parse("&6Stashed");
                Component LISTED = service.parse("&dAvailable for Purchase");

                Component hover =
                        service.parse("&7Entry: ").append(listing.getEntry().getName())
                                .append(Component.newline())
                                .append(service.parse("&7Price: &e")).append(((BuyItNow) listing).getPrice().getText())
                                .append(Component.newline())
                                .append(service.parse("&7Status: "))
                                .append(purchased ? PURCHASED : stashed ? STASHED : LISTED);

                result = result.hoverEvent(HoverEvent.showText(hover));

            } else {
                result = result.append(service.parse("&cAuction &7(&6" + listing.getID() + "&7)"));

                Auction auction = (Auction) listing;
                boolean stashed = auction.hasExpired() && auction.hasAnyBidsPlaced();

                Component STASHED = service.parse("&6Stashed");
                Component LISTED = service.parse("&dAvailable for Purchase");

                Component hover =
                        service.parse("&7Entry: ").append(listing.getEntry().getName())
                                .append(Component.newline())
                                .append(service.parse("&7Price: &e")).append(new MonetaryPrice(auction.getCurrentPrice()).getText())
                                .append(Component.newline())
                                .append(service.parse("&7Status: "))
                                .append(stashed ? STASHED : LISTED);

                result = result.hoverEvent(HoverEvent.showText(hover));
            }

            return result;
        }
    }
}
