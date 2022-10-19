package net.impactdev.gts.ui;

import net.impactdev.impactor.api.items.ImpactorItemStack;
import net.impactdev.impactor.api.items.types.ItemTypes;
import net.impactdev.impactor.api.ui.containers.Icon;
import net.kyori.adventure.text.Component;

public class Icons {

    public static final Icon BLACK_BORDER = Icon.builder()
            .display(() -> ImpactorItemStack.basic()
                    .type(ItemTypes.BLACK_STAINED_GLASS_PANE)
                    .title(Component.empty())
                    .build()
            )
            .constant()
            .build();

}
