package net.impactdev.gts.sponge.utils.items;

import net.impactdev.impactor.api.ui.containers.icons.DisplayProvider;
import net.impactdev.impactor.api.ui.containers.icons.Icon;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;

public class ProvidedIcons {

    public static final Icon<ItemStack> BORDER = Icon.builder(ItemStack.class)
            .display(new DisplayProvider.Constant<>(ItemStack.builder()
                    .itemType(ItemTypes.BLACK_STAINED_GLASS_PANE)
                    .add(Keys.CUSTOM_NAME, Component.empty())
                    .build()
            ))
            .build();

}
