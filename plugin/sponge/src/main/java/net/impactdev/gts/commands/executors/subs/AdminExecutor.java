package net.impactdev.gts.commands.executors.subs;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.manager.ListingManager;
import net.impactdev.gts.commands.annotations.Alias;
import net.impactdev.gts.commands.annotations.Permission;
import net.impactdev.gts.commands.executors.GTSCmdExecutor;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.listings.SpongeItemEntry;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.gts.ui.admin.SpongeAdminMenu;
import net.impactdev.gts.util.GTSInfoGenerator;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.services.text.MessageService;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Alias("admin")
@Permission("gts.admin.base")
public class AdminExecutor extends GTSCmdExecutor {

    public AdminExecutor(GTSPlugin plugin) {
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
        return new GTSCmdExecutor[] {
                new Info(this.plugin),
                new Ping(this.plugin),
                new Test(this.plugin)
        };
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext arguments) throws CommandException {
        if(source instanceof Player) {
            new SpongeAdminMenu((Player) source).open();
            return CommandResult.success();
        }

        throw new CommandException(Text.of("Only players can use the base command!"));
    }

    @Alias("info")
    @Permission("gts.admin.info")
    public static class Info extends GTSCmdExecutor {

        public Info(GTSPlugin plugin) {
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
            MessageService<Text> service = Utilities.PARSER;

            new GTSInfoGenerator(src).create(src).thenAccept(x -> {
                src.sendMessage(service.parse("{{gts:prefix}} Report saved to: &a" + x));
            });
            return CommandResult.success();
        }

    }

    @Alias("ping")
    @Permission("gts.admin.ping")
    public static class Ping extends GTSCmdExecutor {

        public Ping(GTSPlugin plugin) {
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
            GTSPlugin.getInstance().getMessagingService()
                    .sendPing()
                    .thenAccept(pong -> {
                        if(pong.wasSuccessful()) {
                            src.sendMessage(service.parse(
                                    "&eGTS &7\u00bb Ping request &asuccessful&7, took &b" + pong.getResponseTime() + " ms&7!"
                            ));
                        }
                    });
            return CommandResult.success();
        }

    }

    @Alias("test")
    @Permission("gts.admin.test")
    public static class Test extends GTSCmdExecutor {

        public Test(GTSPlugin plugin) {
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
            UUID me = UUID.fromString("a8d614a7-7e28-4f69-ae54-3ad8deb82efc");
            Multimap<UUID, Auction.Bid> sellerBids = ArrayListMultimap.create();
            sellerBids.put(UUID.randomUUID(), new Auction.Bid(50));
            sellerBids.put(UUID.randomUUID(), new Auction.Bid(55));
            Auction seller = Auction.builder()
                    .id(UUID.randomUUID())
                    .lister(me)
                    .published(LocalDateTime.now())
                    .expiration(LocalDateTime.now())
                    .start(50)
                    .increment(0.03f)
                    .entry(new SpongeItemEntry(ItemStack.builder().itemType(ItemTypes.COMMAND_BLOCK).build().createSnapshot()))
                    .bids(sellerBids)
                    .build();
            Impactor.getInstance().getRegistry().get(ListingManager.class).list(me, seller);

            Multimap<UUID, Auction.Bid> loseBid = ArrayListMultimap.create();
            loseBid.put(me, new Auction.Bid(50));
            loseBid.put(UUID.randomUUID(), new Auction.Bid(55));
            Auction lose = Auction.builder()
                    .id(UUID.randomUUID())
                    .lister(UUID.randomUUID())
                    .published(LocalDateTime.now())
                    .expiration(LocalDateTime.now())
                    .start(50)
                    .increment(0.03f)
                    .entry(new SpongeItemEntry(ItemStack.builder().itemType(ItemTypes.COMMAND_BLOCK).build().createSnapshot()))
                    .bids(loseBid)
                    .build();
            Impactor.getInstance().getRegistry().get(ListingManager.class).list(UUID.randomUUID(), lose);

            Multimap<UUID, Auction.Bid> winBid = ArrayListMultimap.create();
            winBid.put(UUID.randomUUID(), new Auction.Bid(50));
            winBid.put(me, new Auction.Bid(55));
            Auction win = Auction.builder()
                    .id(UUID.randomUUID())
                    .lister(UUID.randomUUID())
                    .published(LocalDateTime.now())
                    .expiration(LocalDateTime.now())
                    .start(50)
                    .increment(0.03f)
                    .entry(new SpongeItemEntry(ItemStack.builder().itemType(ItemTypes.COMMAND_BLOCK).build().createSnapshot()))
                    .bids(winBid)
                    .build();
            Impactor.getInstance().getRegistry().get(ListingManager.class).list(UUID.randomUUID(), win);

            return CommandResult.success();
        }
    }
}
