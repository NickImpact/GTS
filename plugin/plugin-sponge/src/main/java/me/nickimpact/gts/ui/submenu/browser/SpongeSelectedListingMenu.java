package me.nickimpact.gts.ui.submenu.browser;

import com.google.common.collect.Lists;
import com.nickimpact.impactor.api.Impactor;
import com.nickimpact.impactor.sponge.ui.SpongeIcon;
import com.nickimpact.impactor.sponge.ui.SpongeLayout;
import com.nickimpact.impactor.sponge.ui.SpongeUI;
import me.nickimpact.gts.common.config.MsgConfigKeys;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import me.nickimpact.gts.common.ui.Historical;
import me.nickimpact.gts.sponge.listings.SpongeListing;
import me.nickimpact.gts.ui.submenu.SpongeListingMenu;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static me.nickimpact.gts.sponge.utils.Utilities.PARSER;
import static me.nickimpact.gts.sponge.utils.Utilities.readMessageConfigOption;

public class SpongeSelectedListingMenu implements Historical<SpongeListingMenu> {

    private Player viewer;
    private SpongeUI display;

    private SpongeListing listing;

    public SpongeSelectedListingMenu(Player viewer, SpongeListing listing) {
        this.viewer = viewer;
        this.listing = listing;
        this.display = SpongeUI.builder()
                .title(Text.EMPTY)
                .dimension(InventoryDimension.of(9, 6))
                .build()
                .define(this.design());

        Task task = Sponge.getScheduler().createTaskBuilder()
                .execute(this::update)
                .interval(1, TimeUnit.SECONDS)
                .submit(GTSPlugin.getInstance().getBootstrap());
        this.display.attachCloseListener(close -> task.cancel());
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

    @Override
    public Optional<Supplier<SpongeListingMenu>> getParent() {
        return Optional.of(() -> new SpongeListingMenu(this.viewer));
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
            this.getParent().ifPresent(parent -> parent.get().open());
        });
        builder.slot(back, 38);

        if(this.listing.getLister().equals(this.viewer.getUniqueId())) {
            builder.slot(this.createRemover(), 42);
        }

        return builder.build();
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
                    .requestBINRemoveRequest(this.listing.getID(), this.viewer.getUniqueId(), response -> {
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
}
