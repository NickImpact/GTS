package com.nickimpact.gts.ui.builder;

import com.google.common.collect.Lists;
import com.nickimpact.gts.GTS;
import com.nickimpact.gts.api.gui.InventoryBase;
import com.nickimpact.gts.api.gui.Icon;
import com.nickimpact.gts.ui.shared.SharedItems;
import com.pixelmonmod.pixelmon.enums.items.EnumPokeballs;
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

public class Pokeball extends InventoryBase {

    private Player player;
    private BuilderBase base;
    private String pokeball;

    public Pokeball(Player player, BuilderBase base) {
        super(player, 6, Text.of(
                TextColors.RED, "gts", TextColors.DARK_GRAY, " \u00bb ", TextColors.DARK_GREEN, "Pokeball Query"
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

        Icon back = new Icon(14, ItemStack.builder()
                .itemType(
                        Sponge.getRegistry().getType(ItemType.class, "pixelmon:eject_button").orElse(ItemTypes.BARRIER))
                .keyValue(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "\u2190 Return to Spec Designer \u2190"))
                .build()
        );
        back.addListener(clickable -> {
            this.base.pokeball = pokeball;

            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                this.player.closeInventory();

                this.base.addIcon(this.base.pokeballIcon());
                this.base.updateContents();
                this.player.openInventory(this.base.getInventory());
            }).delayTicks(1).submit(GTS.getInstance());
        });
        this.addIcon(back);

        Icon reset = SharedItems.cancelIcon(17);
        reset.addListener(clickable -> {
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

    private Icon selectedIcon() {
        return new Icon(9, ItemStack.builder()
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

    private Icon mapInfo() {
        return new Icon(12, ItemStack.builder()
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

    private Icon pokeballIcon(int slot, EnumPokeballs pokeball, boolean selected) {
        Icon icon = new Icon(slot, ItemStack.builder()
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
                    Enchantment.builder().type(EnchantmentTypes.UNBREAKING).level(0).build()
            ));
            icon.getDisplay().offer(Keys.HIDE_ENCHANTMENTS, true);
        }

        icon.addListener(clickable -> {
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
