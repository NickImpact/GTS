package com.nickimpact.GTS.guis.builder;

import com.google.common.collect.Lists;
import com.nickimpact.GTS.GTS;
import com.nickimpact.GTS.guis.InventoryBase;
import com.nickimpact.GTS.guis.InventoryIcon;
import com.nickimpact.GTS.guis.SharedItems;
import com.pixelmonmod.pixelmon.enums.EnumNature;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.meta.ItemEnchantment;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.Enchantments;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

public class Gender extends InventoryBase {

    private Player player;
    private BuilderBase base;
    private String gender;

    public Gender(Player player, BuilderBase base) {
        super(5, Text.of(
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

        InventoryIcon back = new InventoryIcon(17, ItemStack.builder()
                .itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:eject_button").orElse(ItemTypes.BARRIER))
                .keyValue(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "\u2190 Return to Spec Designer \u2190"))
                .build()
        );
        back.addListener(ClickInventoryEvent.class, e -> {
            this.base.gender = gender;

            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                this.player.closeInventory(Cause.of(NamedCause.source(GTS.getInstance())));

                this.base.addIcon(this.base.genderIcon(true));
                this.base.updateContents();
                this.player.openInventory(this.base.getInventory(), Cause.of(NamedCause.source(GTS.getInstance())));
            }).delayTicks(1).submit(GTS.getInstance());
        });
        this.addIcon(back);

        InventoryIcon reset = SharedItems.cancelIcon(35);
        reset.addListener(ClickInventoryEvent.class, e -> {
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

    private InventoryIcon genderIcon(boolean male, boolean selected){
        InventoryIcon icon;

        if(male){
            icon = new InventoryIcon(21, ItemStack.builder()
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
            icon = new InventoryIcon(23, ItemStack.builder()
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
                    new ItemEnchantment(Enchantments.UNBREAKING, 0)
            ));
            icon.getDisplay().offer(Keys.HIDE_ENCHANTMENTS, true);
        }

        icon.addListener(ClickInventoryEvent.class, e -> {
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

    private InventoryIcon selectedIcon(){
        return new InventoryIcon(9, ItemStack.builder()
                .itemType(ItemTypes.STAINED_HARDENED_CLAY)
                .keyValue(Keys.DYE_COLOR, this.gender.equals("Male") ? DyeColors.LIGHT_BLUE : this.gender.equals("Female") ? DyeColors.PINK : DyeColors.WHITE)
                .keyValue(Keys.DISPLAY_NAME, Text.of(
                        TextColors.DARK_AQUA, TextStyles.BOLD, "Selected Gender"
                ))
                .keyValue(Keys.ITEM_LORE, Lists.newArrayList(
                        Text.of(TextColors.GRAY, "Current: " + this.gender)
                ))
                .build()
        );
    }

    private InventoryIcon mapInfo(){
        return new InventoryIcon(27, ItemStack.builder()
                .itemType(ItemTypes.FILLED_MAP)
                .keyValue(Keys.DISPLAY_NAME, Text.of(
                        TextColors.DARK_AQUA, TextStyles.BOLD, "Gender Info"
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
