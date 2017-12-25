package com.nickimpact.gts.ui.builder;

import com.google.common.collect.Lists;
import com.nickimpact.gts.GTS;
import com.nickimpact.gts.api.gui.InventoryBase;
import com.nickimpact.gts.api.gui.Icon;
import com.nickimpact.gts.ui.shared.SharedItems;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import com.pixelmonmod.pixelmon.enums.EnumNature;
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

public class Nature extends InventoryBase {

    private Player player;
    private BuilderBase base;

    private String nature;
    private int slot = -1;
    private DyeColor color = DyeColors.BLACK;

    public Nature(Player player, BuilderBase base) {
        super(player, 5, Text.of(
                TextColors.RED, "gts", TextColors.DARK_GRAY, " \u00bb ", TextColors.DARK_GREEN, "NATURE Query"
        ));

        this.player = player;
        this.base = base;
        this.nature = base.nature;

        this.setupDisplay();
    }

    private void setupDisplay(){
        this.addIcon(SharedItems.forgeBorderIcon(0, DyeColors.BLACK));
        this.addIcon(SharedItems.forgeBorderIcon(8, DyeColors.BLACK));
        this.addIcon(SharedItems.forgeBorderIcon(36, DyeColors.BLACK));
        this.addIcon(SharedItems.forgeBorderIcon(44, DyeColors.BLACK));

        for(int x = 1, y = 0; y < 5; x += 6){
            if(x > 7){
                x = 1;
                y++;
            }
            if(y >= 5) break;

            this.addIcon(SharedItems.forgeBorderIcon(x + (9 * y), DyeColors.BLACK));
        }

        this.addIcon(selectedIcon());
        this.addIcon(mapInfo());

        int x = 2, y = 0;
        for(EnumNature nature : EnumNature.values()){
            if(x == 7){
                x = 2;
                y++;
            }

            boolean selected = false;
            if(this.nature.equals(nature.name())){
                selected = true;
                this.slot = x + (9 * y);
                this.color = getColor(nature);
                this.addIcon(selectedIcon());
            }
            this.addIcon(natureIcon(x + (9 * y), nature, selected));
            x++;
        }

        Icon back = new Icon(17, ItemStack.builder()
                .itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:eject_button").orElse(ItemTypes.BARRIER))
                .keyValue(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "\u2190 Return to Spec Designer \u2190"))
                .build()
        );
        back.addListener(clickable -> {
            this.base.nature = nature;

            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                this.player.closeInventory();

                this.base.addIcon(this.base.natureIcon());
                this.base.updateContents();
                this.player.openInventory(this.base.getInventory());
            }).delayTicks(1).submit(GTS.getInstance());
        });
        this.addIcon(back);

        Icon reset = SharedItems.cancelIcon(35);
        reset.addListener(clickable -> {
            if(this.nature.equals("N/A")) return;

            this.addIcon(natureIcon(this.slot, EnumNature.natureFromString(this.nature), false));
            this.nature = "N/A";
            this.slot = -1;
            this.color = DyeColors.BLACK;

            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                this.addIcon(selectedIcon());
                this.updateContents();
            }).delayTicks(1).submit(GTS.getInstance());
        });
        this.addIcon(reset);
    }

    private Icon natureIcon(int slot, EnumNature nature, boolean selected){
        DyeColor color = getColor(nature);

        Icon icon = new Icon(slot, ItemStack.builder()
                .itemType(ItemTypes.STAINED_HARDENED_CLAY)
                .keyValue(Keys.DISPLAY_NAME, Text.of(
                        TextColors.DARK_AQUA, TextStyles.BOLD, nature.name()
                ))
                .keyValue(Keys.ITEM_LORE, Lists.newArrayList(
                        Text.of(TextColors.GRAY, "NATURE Modifications:"),
                        Text.of(TextColors.GRAY, "  Boosted: ", TextColors.RED, nature.increasedStat.name()),
                        Text.of(TextColors.GRAY, "  Lowered: ", TextColors.AQUA, nature.decreasedStat.name())
                ))
                .keyValue(Keys.DYE_COLOR, color)
                .build()
        );

        if(selected){
            icon.getDisplay().offer(Keys.ITEM_ENCHANTMENTS, Lists.newArrayList(
                    Enchantment.builder().type(EnchantmentTypes.UNBREAKING).level(0).build()
            ));
            icon.getDisplay().offer(Keys.HIDE_ENCHANTMENTS, true);
        }

        icon.addListener(clickable -> {
            if(!this.nature.equals("N/A")){
                this.addIcon(natureIcon(this.slot, EnumNature.natureFromString(this.nature), false));
            }

            this.nature = nature.name();
            this.slot = slot;
            this.color = color;

            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                this.addIcon(natureIcon(this.slot, nature, true));
                this.addIcon(selectedIcon());
                this.updateContents();
            }).delayTicks(1).submit(GTS.getInstance());
        });

        return icon;
    }

    private Icon selectedIcon(){
        return new Icon(9, ItemStack.builder()
                .itemType(ItemTypes.STAINED_HARDENED_CLAY)
                .keyValue(Keys.DYE_COLOR, this.color)
                .keyValue(Keys.DISPLAY_NAME, Text.of(
                        TextColors.DARK_AQUA, TextStyles.BOLD, "Selected NATURE"
                ))
                .keyValue(Keys.ITEM_LORE, Lists.newArrayList(
                        Text.of(TextColors.GRAY, "Current: ", TextColors.YELLOW, this.nature)
                ))
                .build()
        );
    }

    private Icon mapInfo(){
        return new Icon(27, ItemStack.builder()
                .itemType(ItemTypes.FILLED_MAP)
                .keyValue(Keys.DISPLAY_NAME, Text.of(
                        TextColors.DARK_AQUA, TextStyles.BOLD, "NATURE Info"
                ))
                .keyValue(Keys.ITEM_LORE, Lists.newArrayList(
                        Text.of(TextColors.GRAY, "To pick a nature to search"),
                        Text.of(TextColors.GRAY, "for, simply click the representative"),
                        Text.of(TextColors.GRAY, "stained clay for the specific"),
                        Text.of(TextColors.GRAY, "nature to the right.")
                ))
                .build()
        );
    }

    private DyeColor getColor(EnumNature nature){
        return nature.increasedStat.equals(StatsType.HP)                ? DyeColors.GREEN   :
                nature.increasedStat.equals(StatsType.Attack)           ? DyeColors.RED     :
                nature.increasedStat.equals(StatsType.Defence)          ? DyeColors.BLUE    :
                nature.increasedStat.equals(StatsType.SpecialAttack)    ? DyeColors.ORANGE  :
                nature.increasedStat.equals(StatsType.SpecialDefence)   ? DyeColors.PURPLE  :
                nature.increasedStat.equals(StatsType.Speed)            ? DyeColors.YELLOW  :
                                                                          DyeColors.WHITE;
    }
}
