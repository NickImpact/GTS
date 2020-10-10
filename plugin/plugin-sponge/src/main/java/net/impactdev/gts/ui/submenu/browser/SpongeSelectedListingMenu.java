package net.impactdev.gts.ui.submenu.browser;

import com.google.common.collect.Lists;
import net.impactdev.gts.GTSSpongePlugin;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.common.config.updated.ConfigKeys;
import net.impactdev.gts.sponge.ui.SpongeAsyncPage;
import net.impactdev.gts.ui.submenu.SpongeListingMenu;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.utilities.mappings.Tuple;
import net.impactdev.impactor.sponge.ui.SpongeIcon;
import net.impactdev.impactor.sponge.ui.SpongeLayout;
import net.impactdev.impactor.sponge.ui.SpongePage;
import net.impactdev.impactor.sponge.ui.SpongeUI;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.ui.Historical;
import net.impactdev.gts.sponge.listings.SpongeListing;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static net.impactdev.gts.sponge.utils.Utilities.PARSER;
import static net.impactdev.gts.sponge.utils.Utilities.readMessageConfigOption;

public class SpongeSelectedListingMenu {

    private Player viewer;
    private SpongeUI display;

    private SpongeListing listing;

    private Supplier<SpongeAsyncPage<?>> parent;

    public SpongeSelectedListingMenu(Player viewer, SpongeListing listing, Supplier<SpongeAsyncPage<?>> parent, boolean update) {
        this.viewer = viewer;
        this.listing = listing;
        this.parent = parent;
        this.display = SpongeUI.builder()
                .title(Text.EMPTY)
                .dimension(InventoryDimension.of(9, 6))
                .build()
                .define(this.design());

        if(update) {
            Task task = Sponge.getScheduler().createTaskBuilder()
                    .execute(this::update)
                    .interval(1, TimeUnit.SECONDS)
                    .submit(GTSPlugin.getInstance().getBootstrap());
            this.display.attachCloseListener(close -> task.cancel());
        }
    }

    public void open() {
        this.display.open(this.viewer);
    }

    private void update() {
        SpongeIcon icon = new SpongeIcon(this.listing.getEntry()
                .getDisplay(this.viewer.getUniqueId(), this.listing)
                .get()
        );
        this.display.setSlot(13, icon);
    }

    private SpongeLayout design() {
        SpongeLayout.SpongeLayoutBuilder builder = SpongeLayout.builder();
        SpongeIcon colored = new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.STAINED_GLASS_PANE)
                .add(Keys.DYE_COLOR, DyeColors.LIGHT_BLUE)
                .add(Keys.DISPLAY_NAME, Text.EMPTY)
                .build()
        );

        builder.border();
        builder.slots(colored, 3, 4, 5, 10, 11, 12, 14, 15, 16, 21, 22, 23);
        builder.slots(SpongeIcon.BORDER, 19, 20, 24, 25);
        builder.row(SpongeIcon.BORDER, 3);

        SpongeIcon icon = new SpongeIcon(this.listing.getEntry()
                .getDisplay(this.viewer.getUniqueId(), this.listing)
                .get()
        );
        builder.slot(icon, 13);

        SpongeIcon back = new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.BARRIER)
                .add(Keys.DISPLAY_NAME, PARSER.parse(readMessageConfigOption(MsgConfigKeys.UI_GENERAL_BACK), Lists.newArrayList(() -> this.viewer)))
                .build()
        );
        back.addListener(clickable -> {
            this.parent.get().open();
        });
        builder.slot(back, 38);

        if(this.listing.getLister().equals(this.viewer.getUniqueId())) {
            builder.slot(this.createRemover(), 42);
        } else {
            this.createSubmitters(builder);
        }

        return builder.build();
    }

    private void createSubmitters(SpongeLayout.SpongeLayoutBuilder builder) {
        if(this.listing instanceof Auction) {
            Auction auction = (Auction) this.listing;
            double current = auction.hasAnyBidsPlaced() ? auction.getCurrentPrice() : auction.getStartingPrice();
            double newBid = auction.hasAnyBidsPlaced() ? current * GTSPlugin.getInstance().getConfiguration().get(ConfigKeys.AUCTIONS_INCREMENT_RATE) : current;

            Currency currency = GTSPlugin.getInstance().as(GTSSpongePlugin.class).getEconomy().getDefaultCurrency();
            Tuple<Boolean, Boolean> affordability = this.getBalanceAbilities(currency, auction.hasAnyBidsPlaced() ? current : newBid);

            SpongeIcon normal = new SpongeIcon(ItemStack.builder()
                    .itemType(ItemTypes.WRITABLE_BOOK)
                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "TODO - Place Bid"))
                    .add(Keys.ITEM_LORE, Lists.newArrayList(
                            Text.of(TextColors.RED, "TODO - Bid lore"),
                            Text.of(TextColors.GRAY, "New Bid: ", TextColors.YELLOW, currency.format(new BigDecimal(newBid)))
                    ))
                    .build()
            );
            normal.addListener(clickable -> {
                if(affordability.getFirst()) {
                    this.display.close(this.viewer);
                    GTSPlugin.getInstance().getMessagingService().publishBid(
                            auction.getID(),
                            this.viewer.getUniqueId(),
                            newBid
                    ).thenAccept(response -> {
                        if(response.wasSuccessful()) {
                            this.viewer.sendMessage(Text.of(TextColors.RED, "TODO - Bid placed at " + currency.format(new BigDecimal(newBid))));
                        } else {
                            this.viewer.sendMessage(Text.of());
                        }
                    });
                } else {
                    this.viewer.sendMessage(Text.of(TextColors.RED, "TODO - Can't afford"));
                }
            });
            builder.slot(normal, 41);

            if(affordability.getSecond()) {
                SpongeIcon custom = new SpongeIcon(ItemStack.builder().build());
            }
        } else {

        }
    }

    private SpongeIcon createRemover() {
        ItemStack display = ItemStack.builder()
                .itemType(ItemTypes.ANVIL)
                .add(Keys.DISPLAY_NAME, PARSER.parse("&cTODO - Remove Listing Title"))
                .add(Keys.ITEM_LORE, PARSER.parse(Lists.newArrayList(
                        "&cTODO - Remove Listing Lore"
                )))
                .build();

        SpongeIcon icon = new SpongeIcon(display);
        icon.addListener(clickable -> {
            this.display.close(this.viewer);
            this.viewer.sendMessage(Text.of("TODO - Processing request..."));

            GTSPlugin.getInstance().getMessagingService()
                    .requestBINRemoveRequest(this.listing.getID(), this.viewer.getUniqueId())
                    .thenAccept(response -> {
                        if(response.wasSuccessful()) {
                            Impactor.getInstance().getScheduler().executeSync(() -> {
                                this.viewer.sendMessage(Text.of("TODO - Listing returned"));
                                this.listing.getEntry().give(this.viewer.getUniqueId());
                            });
                        }
                    });
        });

        return icon;
    }

    private Tuple<Boolean, Boolean> getBalanceAbilities(Currency currency, double value) {
        AtomicBoolean canAfford = new AtomicBoolean();
        AtomicBoolean isExact = new AtomicBoolean();
        GTSPlugin.getInstance().as(GTSSpongePlugin.class)
                .getEconomy()
                .getOrCreateAccount(this.viewer.getUniqueId())
                .ifPresent(account -> {
                    double balance = account.getBalance(currency).doubleValue();
                    canAfford.set(balance >= value);
                    isExact.set(balance > value);
                });

        return new Tuple<>(canAfford.get(), isExact.get());
    }
}
