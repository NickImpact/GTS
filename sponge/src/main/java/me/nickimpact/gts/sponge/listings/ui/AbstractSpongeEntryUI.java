package me.nickimpact.gts.sponge.listings.ui;

import com.google.common.collect.Lists;
import com.nickimpact.impactor.sponge.ui.SpongeIcon;
import com.nickimpact.impactor.sponge.ui.SpongeUI;
import me.nickimpact.gts.api.listings.ui.AbstractEntryUI;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.InventoryTransformation;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.entity.MainPlayerInventory;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.function.BiFunction;

public abstract class AbstractSpongeEntryUI<E> extends AbstractEntryUI<Player, E, SpongeIcon> {

    private SpongeUI display;

    public AbstractSpongeEntryUI(Player viewer) {
        super(viewer);
        this.display = SpongeUI.builder()
                .title(this.getTitle())
                .dimension(this.getDimensions())
                .build();
    }

    protected abstract Text getTitle();
    protected abstract InventoryDimension getDimensions();

    protected SpongeUI getDisplay() {
        return this.display;
    }

    @Override
    public void open(Player user) {

    }

    @Override
    public SpongeIcon generateWaitingIcon(boolean auction) {
        return new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.CONCRETE)
                .add(Keys.DYE_COLOR, DyeColors.RED)
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Create BIN Listing"))
                .add(Keys.ITEM_LORE, Lists.newArrayList(
                        Text.of(TextColors.GRAY, "No item selected..."),
                        Text.EMPTY,
                        Text.of(TextColors.GRAY, "Click an item in your"),
                        Text.of(TextColors.GRAY, "to list it on the GTS"),
                        Text.of(TextColors.GRAY, "for a BIN purchase!")
                ))
                .build()
        );
    }

    @Override
    public SpongeIcon generateConfirmIcon() {
        ItemStack rep = ItemStack.builder()
                .itemType(ItemTypes.CONCRETE)
                .add(Keys.DYE_COLOR, DyeColors.LIME)
                .add(Keys.DISPLAY_NAME, Text.of())
                .add(Keys.ITEM_LORE, Lists.newArrayList(

                ))
                .build();
        SpongeIcon confirm = new SpongeIcon(rep);
        confirm.addListener(clickable -> {

        });
        return confirm;
    }

    @Override
    public SpongeIcon createNoneChosenIcon() {
        return new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.STONE_BUTTON)
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.YELLOW, "Click an item in your inventory!"))
                .add(Keys.ITEM_LORE, Lists.newArrayList(
                        Text.of(TextColors.GRAY, "Clicking on an item will"),
                        Text.of(TextColors.GRAY, "select it for your listing!")
                ))
                .build()
        );
    }

    private final SpongeIcon red = border(DyeColors.RED);
    private final SpongeIcon darkGreen = border(DyeColors.GREEN);
    private final SpongeIcon green = border(DyeColors.LIME);

    @Override
    public void style(boolean selected) {
        BiFunction<Boolean, SpongeIcon, SpongeIcon> applier = (state, color) -> {
            if(state) {
                return color;
            }

            return red;
        };

        this.display.setSlot(10, applier.apply(selected, darkGreen));
        this.display.setSlot(11, applier.apply(selected, darkGreen));
        this.display.setSlot(15, applier.apply(selected, darkGreen));
        this.display.setSlot(16, applier.apply(selected, darkGreen));

        this.display.setSlot( 3, applier.apply(selected, green));
        this.display.setSlot( 4, applier.apply(selected, green));
        this.display.setSlot( 5, applier.apply(selected, green));
        this.display.setSlot(12, applier.apply(selected, green));
        this.display.setSlot(14, applier.apply(selected, green));
        this.display.setSlot(21, applier.apply(selected, green));
        this.display.setSlot(22, applier.apply(selected, green));
        this.display.setSlot(23, applier.apply(selected, green));
    }

    protected SpongeIcon border(DyeColor color) {
        return new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.STAINED_GLASS_PANE)
                .add(Keys.DISPLAY_NAME, Text.EMPTY)
                .add(Keys.DYE_COLOR, color)
                .build());
    }
}
