package com.nickimpact.gts.ui.builder;

import com.google.common.collect.Lists;
import com.nickimpact.gts.GTS;
import com.nickimpact.gts.api.gui.InventoryBase;
import com.nickimpact.gts.api.gui.Icon;
import com.nickimpact.gts.ui.shared.SharedItems;
import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

public class Levels extends InventoryBase {

    private Player player;
    private BuilderBase base;
    private int currLevel;

    public Levels(Player player, BuilderBase base){
        super(player, 3, Text.of(TextColors.RED, "gts", TextColors.DARK_GRAY, " \u00bb ", TextColors.DARK_GREEN, "LEVEL Query"));

        this.player = player;
        this.base = base;
        this.currLevel = base.level;

        setupDesign();
    }

    private void setupDesign() {
        for(int x = 0, y = 0; y < 3; x++){
            if(x > 8){
                x = 0;
                y += 2;
            }
            if(y >= 3) break;

            this.addIcon(SharedItems.forgeBorderIcon(x + (9 * y), DyeColors.BLACK));
        }

        this.addIcon(SharedItems.forgeBorderIcon(9, DyeColors.BLACK));
        this.addIcon(SharedItems.forgeBorderIcon(15, DyeColors.BLACK));
        this.addIcon(SharedItems.forgeBorderIcon(17, DyeColors.BLACK));

        this.addIcon(addOption());
        this.addIcon(removeOption());

        Icon reset = SharedItems.cancelIcon(14);
        reset.addListener(clickable -> {
            this.currLevel = 1;

            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                this.addIcon(addOption());
                this.addIcon(removeOption());
                this.updateContents();
            }).delayTicks(1).submit(GTS.getInstance());
        });
        this.addIcon(reset);

        Icon back = new Icon(16, ItemStack.builder()
                .itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:eject_button").orElse(ItemTypes.BARRIER))
                .keyValue(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "\u2190 Return to Spec Designer \u2190"))
                .build()
        );
        back.addListener(clickable -> {
            this.base.level = currLevel;

            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                this.player.closeInventory();

                this.base.addIcon(this.base.levelIcon());
                this.base.updateContents();
                this.player.openInventory(this.base.getInventory());
            })
            .delayTicks(1)
            .submit(GTS.getInstance());
        });
        this.addIcon(back);
    }

    private Icon addOption(){
        Icon icon =  new Icon(10, ItemStack.builder()
                .itemType(ItemTypes.STAINED_HARDENED_CLAY)
                .keyValue(Keys.DYE_COLOR, DyeColors.LIME)
                .keyValue(Keys.DISPLAY_NAME, Text.of(
                        TextColors.GREEN, TextStyles.BOLD, "Add Levels"
                ))
                .keyValue(Keys.ITEM_LORE, Lists.newArrayList(
                        Text.of(TextColors.GRAY, "Click here to make an"),
                        Text.of(TextColors.GRAY, "increase to the minimum"),
                        Text.of(TextColors.GRAY, "levels for your request"),
                        Text.EMPTY,
                        Text.of(TextColors.GRAY, "Current: ", TextColors.YELLOW, this.currLevel),
                        Text.EMPTY,
                        Text.of(TextColors.GREEN, "Note:"),
                        Text.of(TextColors.GRAY,
                                (this.currLevel + 1 <= PixelmonConfig.maxLevel ? TextStyles.NONE : TextStyles.STRIKETHROUGH),
                                "  Left Click: ", TextColors.GREEN, "+1"),
                        Text.of(TextColors.GRAY,
                                (this.currLevel + 10 <= PixelmonConfig.maxLevel ? TextStyles.NONE : TextStyles.STRIKETHROUGH),
                                "  Left + Shift Click: ", TextColors.GREEN, "+10")
                ))
                .build()
        );
        icon.addListener(clickable -> {
            if(!(clickable.getEvent() instanceof ClickInventoryEvent.Shift)){
                if(this.currLevel + 1 <= PixelmonConfig.maxLevel)
                this.currLevel += 1;
            } else {
                if(this.currLevel + 10 <= PixelmonConfig.maxLevel)
                    this.currLevel += 10;
            }

            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                this.addIcon(addOption());
                this.addIcon(removeOption());
                this.updateContents();
            }).delayTicks(1).submit(GTS.getInstance());
        });

        return icon;
    }

    private Icon removeOption(){
        Icon icon =  new Icon(12, ItemStack.builder()
                .itemType(ItemTypes.STAINED_HARDENED_CLAY)
                .keyValue(Keys.DYE_COLOR, DyeColors.RED)
                .keyValue(Keys.DISPLAY_NAME, Text.of(
                        TextColors.GREEN, TextStyles.BOLD, "Subtract Levels"
                ))
                .keyValue(Keys.ITEM_LORE, Lists.newArrayList(
                        Text.of(TextColors.GRAY, "Click here to make a"),
                        Text.of(TextColors.GRAY, "decrease to the minimum"),
                        Text.of(TextColors.GRAY, "levels for your request"),
                        Text.EMPTY,
                        Text.of(TextColors.GRAY, "Current: ", TextColors.YELLOW, this.currLevel),
                        Text.EMPTY,
                        Text.of(TextColors.GREEN, "Note:"),
                        Text.of(TextColors.GRAY,
                                (this.currLevel - 1 >= 1 ? TextStyles.NONE : TextStyles.STRIKETHROUGH),
                                "  Left Click: ", TextColors.RED, "-1"),
                        Text.of(TextColors.GRAY,
                                (this.currLevel - 10 >= 1 ? TextStyles.NONE : TextStyles.STRIKETHROUGH),
                                "  Left + Shift Click: ", TextColors.RED, "-10")
                ))
                .build()
        );
        icon.addListener(clickable -> {
            if(!(clickable.getEvent() instanceof ClickInventoryEvent.Shift)) {
                if (this.currLevel - 1 >= 1)
                    this.currLevel -= 1;
            } else {
                if (this.currLevel - 10 >= 1)
                    this.currLevel -= 10;
            }

            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                this.addIcon(addOption());
                this.addIcon(removeOption());
                this.updateContents();
            }).delayTicks(1).submit(GTS.getInstance());
        });

        return icon;
    }
}
