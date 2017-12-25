package com.nickimpact.gts.ui.builder;

import com.google.common.collect.Lists;
import com.nickimpact.gts.GTS;
import com.nickimpact.gts.api.gui.InventoryBase;
import com.nickimpact.gts.api.gui.Icon;
import com.nickimpact.gts.ui.shared.SharedItems;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

public class Gender extends InventoryBase {

    private Player player;
    private BuilderBase base;
    private String gender;

    public Gender(Player player, BuilderBase base) {
        super(player, 5, Text.of(
                TextColors.RED, "GTS", TextColors.DARK_GRAY, " \u00bb ", TextColors.DARK_GREEN, "Gender Query"
        ));

        this.player = player;
        this.base = base;
        this.gender = base.gender;

        this.setupDisplay();
    }

    private void setupDisplay(){
        for(int x = 0, y = 0; y < 5; x++){
            if(x == 9){
                x = 0;
                y += 4;
            }
            if(y >= 5) break;

            this.addIcon(SharedItems.forgeBorderIcon(x + (9 * y), DyeColors.BLACK));
        }

        for (int x = 1, y = 0; y < 4; x += 6) {
            if (x > 7) {
                x = 1;
                y++;
            }
            if(y >= 4) break;

            this.addIcon(SharedItems.forgeBorderIcon(x + (9 * y), DyeColors.BLACK));
        }

        this.addIcon(selectedIcon());
        this.addIcon(mapInfo());
        this.addIcon(genderIcon(true, this.gender.equalsIgnoreCase("male")));
        this.addIcon(genderIcon(false, this.gender.equalsIgnoreCase("female")));

        Icon back = new Icon(17, ItemStack.builder()
                .itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:eject_button").orElse(ItemTypes.BARRIER))
                .keyValue(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "\u2190 Return to Spec Designer \u2190"))
                .build()
        );
        back.addListener(clickable -> {
            this.base.gender = gender;

            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                this.player.closeInventory();

                this.base.addIcon(this.base.genderIcon(true));
                this.base.updateContents();
                this.player.openInventory(this.base.getInventory());
            }).delayTicks(1).submit(GTS.getInstance());
        });
        this.addIcon(back);

        Icon reset = SharedItems.cancelIcon(35);
        reset.addListener(clickable -> {
            if(this.gender.equals("N/A")) return;

            this.addIcon(genderIcon(true, false));
            this.addIcon(genderIcon(false, false));
            this.gender = "N/A";

            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                this.addIcon(selectedIcon());
                this.updateContents();
            }).delayTicks(1).submit(GTS.getInstance());
        });
        this.addIcon(reset);
    }

    private Icon genderIcon(boolean male, boolean selected){
        Icon icon;

        if(male){
            icon = new Icon(21, ItemStack.builder()
                    .itemType(ItemTypes.STAINED_HARDENED_CLAY)
                    .keyValue(Keys.DYE_COLOR, DyeColors.LIGHT_BLUE)
                    .keyValue(Keys.DISPLAY_NAME, Text.of(
                            TextColors.AQUA, "\u2642 Male \u2642"
                    ))
                    .keyValue(Keys.ITEM_LORE, Lists.newArrayList(
                            Text.of(TextColors.GRAY, "Click here to select the"),
                            Text.of(TextColors.AQUA, "male ", TextColors.GRAY, "gender")
                    ))
                    .build()
            );
        } else {
            icon = new Icon(23, ItemStack.builder()
                    .itemType(ItemTypes.STAINED_HARDENED_CLAY)
                    .keyValue(Keys.DYE_COLOR, DyeColors.PINK)
                    .keyValue(Keys.DISPLAY_NAME, Text.of(
                            TextColors.LIGHT_PURPLE, "\u2640 Female \u2640"
                    ))
                    .keyValue(Keys.ITEM_LORE, Lists.newArrayList(
                            Text.of(TextColors.GRAY, "Click here to select the"),
                            Text.of(TextColors.LIGHT_PURPLE, "female ", TextColors.GRAY, "gender")
                    ))
                    .build()
            );
        }

        if(selected){
            icon.getDisplay().offer(Keys.ITEM_ENCHANTMENTS, Lists.newArrayList(
                    Enchantment.builder().type(EnchantmentTypes.UNBREAKING).level(0).build()
            ));
            icon.getDisplay().offer(Keys.HIDE_ENCHANTMENTS, true);
        }

        icon.addListener(clickable -> {
            this.gender = (male ? "Male" : "Female");

            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                this.addIcon(genderIcon(true, male));
                this.addIcon(genderIcon(false, !male));
                this.addIcon(selectedIcon());
                this.updateContents();
            }).delayTicks(1).submit(GTS.getInstance());
        });

        return icon;
    }

    private Icon selectedIcon(){
        return new Icon(9, ItemStack.builder()
                .itemType(ItemTypes.STAINED_HARDENED_CLAY)
                .keyValue(Keys.DYE_COLOR, this.gender.equals("Male") ? DyeColors.LIGHT_BLUE : this.gender.equals("Female") ? DyeColors.PINK : DyeColors.WHITE)
                .keyValue(Keys.DISPLAY_NAME, Text.of(
                        TextColors.DARK_AQUA, TextStyles.BOLD, "Selected GENDER"
                ))
                .keyValue(Keys.ITEM_LORE, Lists.newArrayList(
                        Text.of(TextColors.GRAY, "Current: ", TextColors.YELLOW, this.gender)
                ))
                .build()
        );
    }

    private Icon mapInfo(){
        return new Icon(27, ItemStack.builder()
                .itemType(ItemTypes.FILLED_MAP)
                .keyValue(Keys.DISPLAY_NAME, Text.of(
                        TextColors.DARK_AQUA, TextStyles.BOLD, "GENDER Info"
                ))
                .keyValue(Keys.ITEM_LORE, Lists.newArrayList(
                        Text.of(TextColors.GRAY, "To pick a gender to search"),
                        Text.of(TextColors.GRAY, "for, simply click the representative"),
                        Text.of(TextColors.GRAY, "stained clay for the specific"),
                        Text.of(TextColors.GRAY, "gender to the right.")
                ))
                .build()
        );
    }
}
