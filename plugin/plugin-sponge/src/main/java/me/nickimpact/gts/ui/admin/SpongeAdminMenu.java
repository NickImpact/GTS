package me.nickimpact.gts.ui.admin;

import com.google.common.collect.Lists;
import com.nickimpact.impactor.api.services.text.MessageService;
import com.nickimpact.impactor.sponge.ui.SpongeIcon;
import com.nickimpact.impactor.sponge.ui.SpongeLayout;
import com.nickimpact.impactor.sponge.ui.SpongeUI;
import me.nickimpact.gts.common.config.MsgConfigKeys;
import me.nickimpact.gts.util.Utilities;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.Text;

public class SpongeAdminMenu {

    private final static SpongeLayout LAYOUT = generate();

    private final Player viewer;
    private final SpongeUI view;

    public SpongeAdminMenu(Player viewer) {
        this.viewer = viewer;
        this.view = SpongeUI.builder()
                .title(Utilities.parse(MsgConfigKeys.UI_ADMIN_MAIN_TITLE, Lists.newArrayList(() -> this.viewer)))
                .dimension(InventoryDimension.of(9, 6))
                .build();
        this.view.define(LAYOUT);
    }

    public void open() {
        this.view.open(this.viewer);
    }

    private static SpongeLayout generate() {
        MessageService<Text> service = Utilities.PARSER;

        SpongeLayout.SpongeLayoutBuilder builder = SpongeLayout.builder()
                .dimension(9, 6)
                .border()
                .slots(SpongeIcon.BORDER, 19, 20, 24, 25, 28, 29, 30, 31, 32, 33, 34);

        final SpongeIcon darkBlue = new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.STAINED_GLASS_PANE)
                .add(Keys.DISPLAY_NAME, Text.EMPTY)
                .add(Keys.DYE_COLOR, DyeColors.BLUE)
                .build()
        );
        final SpongeIcon lightBlue = new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.STAINED_GLASS_PANE)
                .add(Keys.DISPLAY_NAME, Text.EMPTY)
                .add(Keys.DYE_COLOR, DyeColors.LIGHT_BLUE)
                .build()
        );

        SpongeIcon info = new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.COMMAND_BLOCK)
                .add(Keys.DISPLAY_NAME, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_ADMIN_MAIN_INFO_TITLE)))
                .add(Keys.ITEM_LORE, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_ADMIN_MAIN_INFO_LORE)))
                .build()
        );
        builder.slot(info, 13);

        SpongeIcon manager = new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.ANVIL)
                .add(Keys.DISPLAY_NAME, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_ADMIN_MAIN_MANAGER)))
                .build()
        );
        manager.addListener(clickable -> {

        });

        SpongeIcon pricing = new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.GOLD_INGOT)
                .add(Keys.DISPLAY_NAME, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_ADMIN_MAIN_PRICE_MGMT)))
                .build()
        );
        pricing.addListener(clickable -> {

        });

        SpongeIcon disabler = new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.REPEATING_COMMAND_BLOCK)
                .add(Keys.DISPLAY_NAME, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_ADMIN_MAIN_DISABLER)))
                .build()
        );
        disabler.addListener(clickable -> {
            // TODO - Disable running tasks, commands, etc
        });

        builder.slots(darkBlue, 10, 11, 15, 16);
        builder.slots(lightBlue, 3, 4, 5, 12, 14, 21, 22, 23);

        builder.slot(manager, 38);
        builder.slot(pricing, 40);
        builder.slot(disabler, 42);

        return builder.build();
    }

}
