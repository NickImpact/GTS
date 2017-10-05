package com.nickimpact.GTS.guis.builder;

import com.google.common.collect.Lists;
import com.nickimpact.GTS.GTS;
import com.nickimpact.GTS.guis.InventoryBase;
import com.nickimpact.GTS.guis.InventoryIcon;
import com.nickimpact.GTS.guis.SharedItems;
import com.pixelmonmod.pixelmon.enums.items.EnumPokeballs;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.meta.ItemEnchantment;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.Enchantments;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

public class Pokeball extends InventoryBase {

    private Player player;
    private BuilderBase base;
    private String pokeball;

    public Pokeball(Player player, BuilderBase base) {
        super(6, Text.of(
                TextColors.RED, "GTS", TextColors.DARK_GRAY, " \u00bb ", TextColors.DARK_GREEN, "Pokeball Query"
        ));

        this.player = player;
        this.base = base;
        this.pokeball = base.pokeball;

        this.setupDisplay();
    }

    private void setupDisplay() {
        for (int y = 0; y < 3; y += 2) {
            for (int x = 0; x < 9; x++) {
                this.addIcon(SharedItems.forgeBorderIcon(x + (9 * y), DyeColors.BLACK));
            }
        }
        this.addIcon(SharedItems.forgeBorderIcon(13, DyeColors.BLACK));

        this.addIcon(selectedIcon());
        this.addIcon(mapInfo());

        int slot = 27;
        for (EnumPokeballs pokeball : EnumPokeballs.values()) {
            this.addIcon(this.pokeballIcon(slot, pokeball, this.pokeball.equalsIgnoreCase(pokeball.name())));
            slot++;
        }

        InventoryIcon back = new InventoryIcon(14, ItemStack.builder()
                .itemType(
                        Sponge.getRegistry().getType(ItemType.class, "pixelmon:eject_button").orElse(ItemTypes.BARRIER))
                .keyValue(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "\u2190 Return to Spec Designer \u2190"))
                .build()
        );
        back.addListener(ClickInventoryEvent.class, e -> {
            this.base.pokeball = pokeball;

            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                this.player.closeInventory();

                this.base.addIcon(this.base.pokeballIcon());
                this.base.updateContents();
                this.player.openInventory(this.base.getInventory());
            }).delayTicks(1).submit(GTS.getInstance());
        });
        this.addIcon(back);

        InventoryIcon reset = SharedItems.cancelIcon(17);
        reset.addListener(ClickInventoryEvent.class, e -> {
            if(this.pokeball.equals("N/A")) return;

            this.addIcon(pokeballIcon(EnumPokeballs.valueOf(this.pokeball).ordinal() + 27,
                                      EnumPokeballs.valueOf(this.pokeball), false));
            this.pokeball = "N/A";

            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                this.addIcon(selectedIcon());
                updateContents();
            }).delayTicks(1).submit(GTS.getInstance());
        });
        this.addIcon(reset);
    }

    private InventoryIcon selectedIcon() {
        return new InventoryIcon(9, ItemStack.builder()
                .itemType(Sponge.getRegistry().getType(ItemType.class,
                                                       getItemName(this.pokeball)).orElse(ItemTypes.BARRIER)
                )
                .keyValue(Keys.DISPLAY_NAME, Text.of(
                        TextColors.DARK_AQUA, TextStyles.BOLD, "Selected Pokeball"
                ))
                .keyValue(Keys.ITEM_LORE, Lists.newArrayList(
                        Text.of(TextColors.GRAY, "Current: ", TextColors.YELLOW,
                                (this.pokeball.equals("N/A") ?
                                        this.pokeball :
                                        this.pokeball.substring(0, this.pokeball.indexOf("Ball")) + " Ball")
                        )
                ))
                .build()
        );
    }

    private InventoryIcon mapInfo() {
        return new InventoryIcon(12, ItemStack.builder()
                .itemType(ItemTypes.FILLED_MAP)
                .keyValue(Keys.DISPLAY_NAME, Text.of(
                        TextColors.DARK_AQUA, TextStyles.BOLD, "Pokeball Info"
                ))
                .keyValue(Keys.ITEM_LORE, Lists.newArrayList(
                        Text.of(TextColors.GRAY, "To pick a pokeball to search"),
                        Text.of(TextColors.GRAY, "for, simply click the representative"),
                        Text.of(TextColors.GRAY, "pokeball")
                ))
                .build()
        );
    }

    private InventoryIcon pokeballIcon(int slot, EnumPokeballs pokeball, boolean selected) {
        InventoryIcon icon = new InventoryIcon(slot, ItemStack.builder()
                .itemType(Sponge.getRegistry().getType(ItemType.class,
                                                       getItemName(pokeball.name()))
                                  .orElse(Sponge.getRegistry().getType(ItemType.class, "pixelmon:poke_ball").orElse(
                                          ItemTypes.BARRIER))
                )
                .keyValue(Keys.DISPLAY_NAME, Text.of(
                        TextColors.DARK_AQUA, TextStyles.BOLD, pokeball.name()
                                .substring(0, pokeball.name().indexOf("Ball")) + " Ball"
                ))
                .keyValue(Keys.ITEM_LORE, Lists.newArrayList(
                        Text.of(TextColors.GRAY, "Click this pokeball to set"),
                        Text.of(TextColors.GRAY, "the query to be of this type")
                ))
                .build()
        );

        if (selected) {
            icon.getDisplay().offer(Keys.ITEM_ENCHANTMENTS, Lists.newArrayList(
                    new ItemEnchantment(Enchantments.UNBREAKING, 0)
            ));
            icon.getDisplay().offer(Keys.HIDE_ENCHANTMENTS, true);
        }

        icon.addListener(ClickInventoryEvent.class, e -> {
            if (!this.pokeball.equals("N/A"))
                this.addIcon(pokeballIcon(EnumPokeballs.valueOf(this.pokeball).ordinal() + 27,
                                          EnumPokeballs.valueOf(this.pokeball), false));

            this.pokeball = pokeball.name();

            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                this.addIcon(pokeballIcon(pokeball.ordinal() + 27, pokeball, true));
                this.addIcon(selectedIcon());
                this.updateContents();
            }).delayTicks(1).submit(GTS.getInstance());
        });

        return icon;
    }

    private String getItemName(String name) {
        if (name.equals("N/A")) return "minecraft:barrier";
        return "pixelmon:" + (name.substring(0, name.indexOf("Ball")) + "_ball").toLowerCase();
    }
}
