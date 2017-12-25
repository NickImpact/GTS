package com.nickimpact.gts.ui.builder;

import com.google.common.collect.Lists;
import com.nickimpact.gts.GTS;
import com.nickimpact.gts.api.gui.InventoryBase;
import com.nickimpact.gts.api.gui.Icon;
import com.nickimpact.gts.ui.shared.SharedItems;
import com.pixelmonmod.pixelmon.enums.EnumGrowth;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColor;
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

public class Growth extends InventoryBase {

    private Player player;
    private BuilderBase base;
    private String growth;

    public Growth(Player player, BuilderBase base) {
        super(player, 5, Text.of(
                TextColors.RED, "gts", TextColors.DARK_GRAY, " \u00bb ", TextColors.DARK_GREEN, "GROWTH Query"
        ));

        this.player = player;
        this.base = base;
        this.growth = base.growth;

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

        for(int i = 11, in = 8, ord = 0; ord < 9; i += 2, ord++){
            this.addIcon(growthIcon(i, EnumGrowth.getGrowthFromIndex(in), this.growth.equalsIgnoreCase(EnumGrowth.getGrowthFromIndex(in).name())));

            if(in == 8) in = 0;
            else in++;

            if(i > 13 && i < 20){
                i = 18;
            }

            if(i > 22 && i < 29){
                i = 27;
            }
        }

        Icon back = new Icon(17, ItemStack.builder()
                .itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:eject_button").orElse(ItemTypes.BARRIER))
                .keyValue(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "\u2190 Return to Spec Designer \u2190"))
                .build()
        );
        back.addListener(clickable -> {
            this.base.growth = growth;

            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                this.player.closeInventory();

                this.base.addIcon(this.base.growthIcon());
                this.base.updateContents();
                this.player.openInventory(this.base.getInventory());
            }).delayTicks(1).submit(GTS.getInstance());
        });
        this.addIcon(back);

        Icon reset = SharedItems.cancelIcon(35);
        reset.addListener(clickable -> {
            if(this.growth.equals("N/A")) return;

            for(int i = 11, in = 8, ord = 0; ord < 9; i += 2, ord++){
                this.addIcon(growthIcon(i, EnumGrowth.getGrowthFromIndex(in), false));

                if(in == 8) in = 0;
                else in++;

                if(i > 13 && i < 20){
                    i = 18;
                }

                if(i > 22 && i < 29){
                    i = 27;
                }
            }
            this.growth = "N/A";

            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                this.addIcon(selectedIcon());
                this.updateContents();
            }).delayTicks(1).submit(GTS.getInstance());
        });
        this.addIcon(reset);
    }

    private Icon growthIcon(int slot, EnumGrowth growth, boolean selected) {
        Icon icon = new Icon(slot, ItemStack.builder()
                .itemType(ItemTypes.STAINED_HARDENED_CLAY)
                .keyValue(Keys.DYE_COLOR, getColor(growth.name()))
                .keyValue(Keys.DISPLAY_NAME, Text.of(
                    TextColors.DARK_AQUA, growth.name()
                ))
                .build()
        );

        if(selected){
            icon.getDisplay().offer(Keys.ITEM_ENCHANTMENTS, Lists.newArrayList(
                    Enchantment.builder().type(EnchantmentTypes.UNBREAKING).level(0).build()
            ));
            icon.getDisplay().offer(Keys.HIDE_ENCHANTMENTS, true);
        }

        icon.addListener(clickable -> {
            this.growth = growth.name();

            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                this.addIcon(selectedIcon());
                for(int i = 11, in = 8, ord = 0; ord < 9; i += 2, ord++){
                    this.addIcon(growthIcon(i, EnumGrowth.getGrowthFromIndex(in), this.growth.equalsIgnoreCase(EnumGrowth.getGrowthFromIndex(in).name())));

                    if(in == 8) in = 0;
                    else in++;

                    if(i > 13 && i < 20){
                        i = 18;
                    }

                    if(i > 22 && i < 29){
                        i = 27;
                    }
                }
                this.updateContents();
            }).delayTicks(1).submit(GTS.getInstance());
        });

        return icon;
    }

    private Icon selectedIcon(){
        return new Icon(9, ItemStack.builder()
                .itemType(ItemTypes.STAINED_HARDENED_CLAY)
                .keyValue(Keys.DYE_COLOR, getColor(this.growth))
                .keyValue(Keys.DISPLAY_NAME, Text.of(
                        TextColors.DARK_AQUA, TextStyles.BOLD, "Selected GROWTH"
                ))
                .keyValue(Keys.ITEM_LORE, Lists.newArrayList(
                        Text.of(TextColors.GRAY, "Current: ", TextColors.YELLOW, this.growth)
                ))
                .build()
        );
    }

    private Icon mapInfo(){
        return new Icon(27, ItemStack.builder()
                .itemType(ItemTypes.FILLED_MAP)
                .keyValue(Keys.DISPLAY_NAME, Text.of(
                        TextColors.DARK_AQUA, TextStyles.BOLD, "GROWTH Info"
                ))
                .keyValue(Keys.ITEM_LORE, Lists.newArrayList(
                        Text.of(TextColors.GRAY, "To pick a growth to search"),
                        Text.of(TextColors.GRAY, "for, simply click the representative"),
                        Text.of(TextColors.GRAY, "stained clay for the specific"),
                        Text.of(TextColors.GRAY, "growth to the right.")
                ))
                .build()
        );
    }

    private DyeColor getColor(String growth){
        return growth.equals("Microscopic") ? DyeColors.RED :
                growth.equals("Pygmy") ? DyeColors.ORANGE :
                growth.equals("Runt") ? DyeColors.YELLOW :
                growth.equals("Small") ? DyeColors.GREEN :
                growth.equals("Ordinary") ? DyeColors.LIGHT_BLUE :
                growth.equals("Huge") ? DyeColors.PURPLE :
                growth.equals("Giant") ? DyeColors.PINK :
                growth.equals("Enormous") ? DyeColors.GRAY :
                growth.equals("Ginormous") ? DyeColors.BLACK :

                DyeColors.WHITE;
    }
}
