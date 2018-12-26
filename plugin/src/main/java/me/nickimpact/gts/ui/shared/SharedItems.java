package me.nickimpact.gts.ui.shared;

import com.google.common.collect.Lists;
import com.nickimpact.impactor.gui.v2.Icon;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

public class SharedItems {

    /**
     * This method is used to create a border representation of an item
     *
     * @param color The dye color of the border piece
     * @return An icon for an Inventory display
     */
    public static Icon forgeBorderIcon(DyeColor color){
        return new Icon(ItemStack.builder()
                .itemType(ItemTypes.STAINED_GLASS_PANE)
                .quantity(1)
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.BLACK, ""))
                .add(Keys.DYE_COLOR, color)
                .build()
        );
    }

    public static Icon confirmIcon(boolean auction){
        return new Icon(ItemStack.builder()
                .itemType(ItemTypes.DYE)
                .quantity(1)
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, (auction ? "Confirm Bid" : "Confirm Purchase")))
                .add(Keys.DYE_COLOR, DyeColors.LIME)
                .build()
        );
    }

    public static Icon denyIcon(){
        return new Icon(ItemStack.builder()
                .itemType(ItemTypes.DYE)
                .quantity(1)
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Cancel Action"))
                .add(Keys.DYE_COLOR, DyeColors.RED)
                .build()
        );
    }

    static Icon lastMenu(){
        return new Icon(ItemStack.builder()
                .itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:eject_button").get())
                .quantity(1)
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Last Menu"))
                .build()
        );
    }

    public static Icon cancelIcon(){
        return new Icon(ItemStack.builder()
                .itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:trash_can").orElse(ItemTypes.BARRIER))
                .add(Keys.DISPLAY_NAME, Text.of(
                        TextColors.RED, TextStyles.BOLD, "Reset Option"
                ))
                .add(Keys.ITEM_LORE, Lists.newArrayList(
                        Text.of(TextColors.GRAY, "Click here to reset the"),
                        Text.of(TextColors.GRAY, "current query back to its"),
                        Text.of(TextColors.GRAY, "default search option")
                ))
                .build()
        );
    }
}
