package me.nickimpact.gts.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;
import com.nickimpact.impactor.api.json.factory.JObject;
import me.nickimpact.gts.api.GTSService;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import me.nickimpact.gts.listings.SpongeItemEntry;
import me.nickimpact.gts.sponge.pricing.provided.MoneyPrice;
import me.nickimpact.gts.ui.SpongeMainMenu;
import me.nickimpact.gts.ui.admin.SpongeAdminMenu;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
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
        public void test(CommandIssuer issuer) {
            ItemStackSnapshot snapshot = ItemStack.builder()
                    .itemType(ItemTypes.DIAMOND)
                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.YELLOW, "Testing 123"))
                    .add(Keys.ITEM_LORE, Lists.newArrayList(
                            Text.of(TextColors.GRAY, "This is a set of test lore."),
                            Text.EMPTY,
                            Text.of("Let's see if it serializes well")
                    ))
                    .add(Keys.ITEM_ENCHANTMENTS, Lists.newArrayList(
                            Enchantment.builder().type(EnchantmentTypes.SHARPNESS).level(1).build(),
                            Enchantment.builder().type(EnchantmentTypes.FIRE_ASPECT).level(1).build()
                    ))
                    .quantity(21)
                    .build()
                    .createSnapshot();

            Listing listing = Listing.builder()
                    .id(UUID.randomUUID())
                    .lister(UUID.randomUUID())
                    .expiration(LocalDateTime.now().plusDays(7))
                    .entry(new SpongeItemEntry(snapshot))
                    .price(new MoneyPrice(420.00))
                    .build();

            GTSPlugin.getInstance().getPluginLogger().info(new GsonBuilder().setPrettyPrinting().create().toJson(listing.serialize().toJson()));

            listing.deserialize(listing.serialize().toJson());
        }

    }

}
