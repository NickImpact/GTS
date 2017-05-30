package com.nickimpact.GTS.guis.builder;

import com.google.common.collect.Lists;
import com.nickimpact.GTS.GTS;
import com.nickimpact.GTS.guis.InventoryBase;
import com.nickimpact.GTS.guis.InventoryIcon;
import com.nickimpact.GTS.guis.SharedItems;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.meta.ItemEnchantment;
import org.spongepowered.api.data.type.DyeColor;
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

public class Ability extends InventoryBase {

    private Player player;
    private BuilderBase base;
    private String ability;
    private String[] abilities;

    public Ability(Player player, BuilderBase base) {
        super(5, Text.of(
                TextColors.RED, "GTS", TextColors.DARK_GRAY, " \u00bb ", TextColors.DARK_GREEN, "Ability Query"
        ));

        this.player = player;
        this.base = base;
        this.ability = base.ability;
        this.abilities = ((EntityPixelmon) PixelmonEntityList.createEntityByName(base.pokemon, (World)player.getWorld())).baseStats.abilities;

        setupDisplay();
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

        for(int y = 1; y < 4; y++){
            this.addIcon(SharedItems.forgeBorderIcon((9 * y), DyeColors.BLACK));
            this.addIcon(SharedItems.forgeBorderIcon(6 + (9 * y), DyeColors.BLACK));
            this.addIcon(SharedItems.forgeBorderIcon(8 + (9 * y), DyeColors.BLACK));
        }

        for(int x = 1; x < 6; x++){
            this.addIcon(SharedItems.forgeBorderIcon(x + (9 * 2), DyeColors.GRAY));
        }

        this.addIcon(selectedIcon());
        this.addIcon(mapInfo());
        this.addIcon(abilityIcon(28, false));
        if(abilities[1] != null){
            this.addIcon(abilityIcon(30, false));
        } else {
            this.addIcon(invalidIcon(30));
        }
        if(abilities[2] != null){
            this.addIcon(abilityIcon(32, false));
        } else {
            this.addIcon(invalidIcon(32));
        }

        InventoryIcon back = new InventoryIcon(16, ItemStack.builder()
                .itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:eject_button").orElse(ItemTypes.BARRIER))
                .keyValue(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "\u2190 Return to Spec Designer \u2190"))
                .build()
        );
        back.addListener(ClickInventoryEvent.class, e -> {
            this.base.ability = ability;

            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                this.player.closeInventory(Cause.of(NamedCause.source(GTS.getInstance())));

                this.base.addIcon(this.base.abilityIcon());
                this.base.updateContents();
                this.player.openInventory(this.base.getInventory(), Cause.of(NamedCause.source(GTS.getInstance())));
            }).delayTicks(1).submit(GTS.getInstance());
        });
        this.addIcon(back);

        InventoryIcon reset = SharedItems.cancelIcon(34);
        reset.addListener(ClickInventoryEvent.class, e -> {
            this.ability = "N/A";

            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                this.addIcon(selectedIcon());
                this.addIcon(abilityIcon(28, false));
                if(abilities[1] != null){
                    this.addIcon(abilityIcon(30, false));
                } else {
                    this.addIcon(invalidIcon(30));
                }
                if(abilities[2] != null){
                    this.addIcon(abilityIcon(32, false));
                } else {
                    this.addIcon(invalidIcon(32));
                }
                this.updateContents();
            }).delayTicks(1).submit(GTS.getInstance());
        });
        this.addIcon(reset);
    }

    private InventoryIcon abilityIcon(int slot, boolean selected){
        InventoryIcon icon =  new InventoryIcon(slot, ItemStack.builder()
                .itemType(ItemTypes.STAINED_HARDENED_CLAY)
                .keyValue(Keys.DYE_COLOR,
                    slot == 28 ? DyeColors.GREEN :
                    slot == 30 ? DyeColors.YELLOW :
                                 DyeColors.RED
                )
                .keyValue(Keys.DISPLAY_NAME, Text.of(
                        slot == 28 ? Text.of(TextColors.GREEN, abilities[0]) :
                        slot == 30 ? Text.of(TextColors.YELLOW, abilities[1]) :
                                     Text.of(TextColors.RED, abilities[2], TextColors.GRAY, " (", TextColors.DARK_AQUA, "HA", TextColors.GRAY, ")")
                ))
                .keyValue(Keys.ITEM_LORE, Lists.newArrayList(
                        Text.of(TextColors.GRAY, "Click here to select"),
                        Text.of(TextColors.GRAY, "this ability.")
                ))
                .build()
        );

        if(selected){
            icon.getDisplay().offer(Keys.ITEM_ENCHANTMENTS, Lists.newArrayList(
                    new ItemEnchantment(Enchantments.UNBREAKING, 0)
            ));
            icon.getDisplay().offer(Keys.HIDE_ENCHANTMENTS, true);
        }

        icon.addListener(ClickInventoryEvent.class, e -> {
            if(slot == 28) {
                this.ability = abilities[0];
                this.addIcon(
                        abilities[1] != null ?
                                abilityIcon(30, false) :
                                invalidIcon(30)
                );
                this.addIcon(
                        abilities[2] != null ?
                                abilityIcon(32, false) :
                                invalidIcon(32)
                );
            } else if(slot == 30){
                this.ability = abilities[1];
                this.addIcon(abilityIcon(28, false));
                this.addIcon(
                        abilities[2] != null ?
                                abilityIcon(32, false) :
                                invalidIcon(32)
                );
            } else {
                this.ability = abilities[2];
                this.addIcon(abilityIcon(28, false));
                this.addIcon(
                        abilities[1] != null ?
                                abilityIcon(30, false) :
                                invalidIcon(30)
                );
            }

            InventoryIcon update = abilityIcon(slot, true);

            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                this.addIcon(selectedIcon());
                this.addIcon(update);
                this.updateContents();
            }).delayTicks(1).submit(GTS.getInstance());
        });

        return icon;
    }

    private InventoryIcon invalidIcon(int slot){
        return new InventoryIcon(slot, ItemStack.builder()
                .itemType(ItemTypes.BARRIER)
                .keyValue(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "N/A"))
                .build()
        );
    }

    private InventoryIcon selectedIcon(){
        DyeColor color = abilities[0].equalsIgnoreCase(ability) ? DyeColors.GREEN :
                         abilities[1] != null && abilities[1].equalsIgnoreCase(ability) ? DyeColors.YELLOW :
                         abilities[2] != null && abilities[2].equalsIgnoreCase(ability) ? DyeColors.RED : DyeColors.WHITE;

        return new InventoryIcon(11, ItemStack.builder()
                .itemType(ItemTypes.STAINED_HARDENED_CLAY)
                .keyValue(Keys.DISPLAY_NAME, Text.of(
                        TextColors.DARK_AQUA, TextStyles.BOLD, "Selected Ability"
                ))
                .keyValue(Keys.ITEM_LORE, Lists.newArrayList(
                        Text.of(TextColors.GRAY, "Current: " + this.ability)
                ))
                .keyValue(Keys.DYE_COLOR, color)
                .build()
        );
    }

    private InventoryIcon mapInfo(){
        return new InventoryIcon(13, ItemStack.builder()
                .itemType(ItemTypes.FILLED_MAP)
                .keyValue(Keys.DISPLAY_NAME, Text.of(
                        TextColors.DARK_AQUA, TextStyles.BOLD, "Ability Info"
                ))
                .keyValue(Keys.ITEM_LORE, Lists.newArrayList(
                        Text.of(TextColors.GRAY, "To pick an ability to search"),
                        Text.of(TextColors.GRAY, "for, simply click the representative"),
                        Text.of(TextColors.GRAY, "stained clay for the specific"),
                        Text.of(TextColors.GRAY, "ability below."),
                        Text.EMPTY,
                        Text.of(TextColors.GREEN, this.base.pokemon + "'s Abilities:"),
                        Text.of(TextColors.GRAY, "  1)  ", TextColors.YELLOW, abilities[0]),
                        Text.of(TextColors.GRAY, "  2)  ", TextColors.YELLOW, abilities[1] == null ? "N/A" : abilities[1]),
                        Text.of(TextColors.GRAY, "  HA) ", TextColors.YELLOW, abilities[2] == null ? "N/A" : abilities[2])
                ))
                .build()
        );
    }
}
