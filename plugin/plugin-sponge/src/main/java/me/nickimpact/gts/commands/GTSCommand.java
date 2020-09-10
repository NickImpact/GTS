package me.nickimpact.gts.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import com.nickimpact.impactor.api.Impactor;
import me.nickimpact.gts.api.listings.buyitnow.BuyItNow;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import me.nickimpact.gts.listings.SpongeItemEntry;
import me.nickimpact.gts.manager.SpongeListingManager;
import me.nickimpact.gts.sponge.listings.SpongeBuyItNow;
import me.nickimpact.gts.sponge.pricing.provided.MonetaryPrice;
import me.nickimpact.gts.ui.SpongeMainMenu;
import me.nickimpact.gts.ui.admin.SpongeAdminMenu;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.MainPlayerInventory;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.time.LocalDateTime;
import java.util.UUID;

@CommandAlias("gts")
public class GTSCommand extends BaseCommand {

    @Default
    public void execute(Player issuer) {
        new SpongeMainMenu(issuer).open();
    }

    @Subcommand("ignore")
    public void ignore(Player issuer) {

    }

    @Subcommand("admin")
    @CommandPermission("gts.commands.admin.base")
    public class Admin extends BaseCommand {

        @Default
        public void execute(CommandIssuer issuer) {
            if(issuer.isPlayer()) {
                Player player = issuer.getIssuer();
                new SpongeAdminMenu(player).open();
            }
        }

        @Subcommand("ping")
        @CommandPermission("gts.commands.admin.ping")
        public void processPingRequest(CommandIssuer issuer) {
            issuer.sendMessage("Check the console for the status of this message!");
            GTSPlugin.getInstance().getMessagingService().sendPing();
        }

        @Subcommand("test")
        public void test(Player player) {
            ItemStack test = ItemStack.builder()
                    .itemType(ItemTypes.DIAMOND)
                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.YELLOW, "Test Item"))
                    .build();
            SpongeItemEntry entry = new SpongeItemEntry(test.createSnapshot());
            BuyItNow bin = BuyItNow.builder()
                    .id(UUID.randomUUID())
                    .lister(player.getUniqueId())
                    .price(new MonetaryPrice(420.00))
                    .expiration(LocalDateTime.now().plusDays(7))
                    .entry(entry)
                    .build();
            SpongeListingManager manager = Impactor.getInstance().getRegistry().get(SpongeListingManager.class);
            manager.list(player.getUniqueId(), (SpongeBuyItNow) bin);
        }

    }

}
