package me.nickimpact.gts.listings.ui;

import com.github.benmanes.caffeine.cache.Cache;
import com.google.common.collect.Lists;
import com.nickimpact.impactor.api.services.text.MessageService;
import com.nickimpact.impactor.sponge.ui.SpongeIcon;
import com.nickimpact.impactor.sponge.ui.SpongeLayout;
import com.nickimpact.impactor.sponge.ui.SpongeUI;
import me.nickimpact.gts.api.listings.ui.EntryUI;
import me.nickimpact.gts.common.config.MsgConfigKeys;
import me.nickimpact.gts.common.ui.Historical;
import me.nickimpact.gts.ui.SpongeMainMenu;
import me.nickimpact.gts.util.GTSReferences;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;
import java.util.function.Supplier;

import static me.nickimpact.gts.util.GTSReferences.readMessageConfigOption;

public class SpongeItemUI implements EntryUI<Player, ItemStackSnapshot>, Historical<SpongeMainMenu> {

    private Player viewer;

    private ItemStackSnapshot chosen;

    private SpongeUI display;

    public SpongeItemUI(Player viewer) {
        this.viewer = viewer;
        this.display = SpongeUI.builder()
                .title(Text.of("Listing Creator - Items"))
                .dimension(InventoryDimension.of(9, 5))
                .build();
        this.display.define(this.design());
    }

    @Override
    public Optional<ItemStackSnapshot> getChosenOption() {
        return Optional.ofNullable(this.chosen);
    }

    @Override
    public void open(Player user) {
        this.display.open(user);
    }

    private SpongeLayout design() {
        final MessageService<Text> PARSER = GTSReferences.PARSER;

        SpongeLayout.SpongeLayoutBuilder slb = SpongeLayout.builder();
        slb.dimension(9, 4).border().dimension(9, 5);
        slb.slots(new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.STAINED_GLASS_PANE)
                .add(Keys.DISPLAY_NAME, Text.EMPTY)
                .add(Keys.DYE_COLOR, DyeColors.RED)
                .build()),
                3, 4, 5, 10, 11, 12, 14, 15, 16, 21, 22, 23
        );
        slb.slots(SpongeIcon.BORDER, 19, 20, 24, 25, 37, 43);

        slb.slot(new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.STONE_BUTTON)
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.YELLOW, "Click an item in your inventory!"))
                .add(Keys.ITEM_LORE, Lists.newArrayList(
                        Text.of(TextColors.GRAY, "Clicking on an item will"),
                        Text.of(TextColors.GRAY, "select it for your listing!")
                ))
                .build()),
                13
        );

        SpongeIcon back = new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.BARRIER)
                .add(Keys.DISPLAY_NAME, PARSER.parse(readMessageConfigOption(MsgConfigKeys.UI_GENERAL_BACK), Lists.newArrayList(() -> this.viewer)))
                .build()
        );
        back.addListener(clickable -> {
            this.getParent().ifPresent(parent -> parent.get().open());
        });
        slb.slot(back, 36);




        return slb.build();
    }

    @Override
    public Optional<Supplier<SpongeMainMenu>> getParent() {
        return Optional.of(() -> new SpongeMainMenu(this.viewer));
    }
}
