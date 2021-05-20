package net.impactdev.gts.commands.executors.subs;

import net.impactdev.gts.api.commands.GTSCommandExecutor;
import net.impactdev.gts.api.listings.manager.ListingManager;
import net.impactdev.gts.api.storage.GTSStorage;
import net.impactdev.gts.listings.SpongeItemEntry;
import net.impactdev.gts.util.GTSInfoGenerator;
import net.impactdev.gts.api.commands.annotations.Alias;
import net.impactdev.gts.api.commands.annotations.Permission;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.sponge.commands.GTSCmdExecutor;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.plugin.permissions.GTSPermissions;
import net.impactdev.gts.sponge.pricing.provided.MonetaryPrice;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.gts.ui.admin.SpongeAdminMenu;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.services.text.MessageService;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.time.LocalDateTime;
import java.util.UUID;

@Alias("admin")
@Permission(GTSPermissions.ADMIN_BASE)
public class AdminExecutor extends GTSCmdExecutor {

    public AdminExecutor(GTSPlugin plugin) {
        super(plugin);
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
                new Clean(this.plugin),
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
    @Permission(GTSPermissions.ADMIN_INFO)
    public static class Info extends GTSCmdExecutor {

        public Info(GTSPlugin plugin) {
            super(plugin);
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
    @Permission(GTSPermissions.ADMIN_PING)
    public static class Ping extends GTSCmdExecutor {

        public Ping(GTSPlugin plugin) {
            super(plugin);
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

    @Alias("clean")
    @Permission(GTSPermissions.ADMIN_BASE)
    public static class Clean extends GTSCmdExecutor {

        public Clean(GTSPlugin plugin) {
            super(plugin);
        }

        @Override
        public CommandElement[] getArguments() {
            return new CommandElement[0];
        }

        @Override
        public GTSCommandExecutor<CommandElement, CommandSpec>[] getSubcommands() {
            return new GTSCommandExecutor[0];
        }

        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);

            src.sendMessage(service.parse("{{gts:prefix}} Erasing legacy table if it exists..."));
            GTSStorage storage = GTSPlugin.getInstance().getStorage();
            if(storage.clean().join()) {
                src.sendMessage(service.parse("{{gts:prefix}} Legacy data successfully erased!"));
            } else {
                src.sendMessage(service.parse("{{gts:error}} Legacy data no longer exists..."));
            }

            return CommandResult.success();
        }
    }

    @Alias("test")
    @Permission(GTSPermissions.ADMIN_PING)
    public static class Test extends GTSCmdExecutor {

        public Test(GTSPlugin plugin) {
            super(plugin);
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
            BuyItNow bin = BuyItNow.builder()
                    .lister(Listing.SERVER_ID)
                    .entry(new SpongeItemEntry(ItemStack.builder().itemType(ItemTypes.GRASS).add(Keys.DISPLAY_NAME, Text.of(TextColors.YELLOW, "GTS Test Item")).build().createSnapshot()))
                    .expiration(LocalDateTime.now())
                    .price(new MonetaryPrice(50))
                    .purchased()
                    .stashedForPurchaser()
                    .purchaser(((Player) src).getUniqueId())
                    .build();

            Impactor.getInstance().getRegistry().get(ListingManager.class).list(Listing.SERVER_ID, bin);
            return CommandResult.success();
        }

    }
}
