package com.nickimpact.gts.ui.shared;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.gts.GTS;
import com.nickimpact.gts.configuration.MsgConfigKeys;
import com.nickimpact.gts.api.gui.Icon;
import com.nickimpact.gts.utils.ItemUtils;
import com.pixelmonmod.pixelmon.config.PixelmonItems;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.util.helpers.SpriteHelper;
import io.github.nucleuspowered.nucleus.api.exceptions.NucleusException;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class SharedItems {

    private static HashMap<String, Optional<Object>> textOptions = Maps.newHashMap();

    /**
     * This method is used to create a border representation of an item
     *
     * @param slot The slot this border piece will be placed in
     * @param color The dye color of the border piece
     * @return An icon for an Inventory display
     */
    public static Icon forgeBorderIcon(int slot, DyeColor color){
        return new Icon(slot, ItemStack.builder()
                .itemType(ItemTypes.STAINED_GLASS_PANE)
                .quantity(1)
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.BLACK, ""))
                .add(Keys.DYE_COLOR, color)
                .build()
        );
    }

    public static ItemStack pokemonDisplay(EntityPixelmon pokemon, int form){
        net.minecraft.item.ItemStack nativeItem = new net.minecraft.item.ItemStack(PixelmonItems.itemPixelmonSprite);
        NBTTagCompound nbt = new NBTTagCompound();
        String idValue = String.format("%03d", pokemon.baseStats.nationalPokedexNumber);
        if (pokemon.isEgg){
            if (pokemon.getName().equalsIgnoreCase("Manaphy")){
                nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/eggs/manaphy1");
            } else if (pokemon.getName().equalsIgnoreCase("Togepi")){
                nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/eggs/togepi1");
            } else {
                nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/eggs/egg1");
            }
        } else {
            if (pokemon.getIsShiny()) {
                nbt.setString(NbtKeys.SPRITE_NAME,
                              "pixelmon:sprites/shinypokemon/" + idValue + SpriteHelper.getSpriteExtra(
                                      pokemon.baseStats.pixelmonName, form));
            } else {
                nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/pokemon/" + idValue + SpriteHelper.getSpriteExtra(
                        pokemon.baseStats.pixelmonName, form));
            }
        }
        nativeItem.setTagCompound(nbt);

        return ItemStackUtil.fromNative(nativeItem);
    }

    public static Icon confirmIcon(int slot, boolean auction){
        return new Icon(slot, ItemStack.builder()
                .itemType(ItemTypes.DYE)
                .quantity(1)
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, (auction ? "Confirm Bid" : "Confirm Purchase")))
                .add(Keys.DYE_COLOR, DyeColors.LIME)
                .build()
        );
    }

    public static Icon denyIcon(int slot){
        return new Icon(slot, ItemStack.builder()
                .itemType(ItemTypes.DYE)
                .quantity(1)
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Cancel Action"))
                .add(Keys.DYE_COLOR, DyeColors.RED)
                .build()
        );
    }

    static Icon lastMenu(int slot){
        return new Icon(slot, ItemStack.builder()
                .itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:eject_button").get())
                .quantity(1)
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Last Menu"))
                .build()
        );
    }

    public static Icon cancelIcon(int slot){
        return new Icon(slot, ItemStack.builder()
                .itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:trash_can").orElse(ItemTypes.BARRIER))
                .add(Keys.DISPLAY_NAME, Text.of(
                        TextColors.RED, TextStyles.BOLD, "Reset Option"
                ))
                .add(Keys.ITEM_LORE, Lists.newArrayList(
                        Text.of(TextColors.GRAY, "Click here to reset the"),
                        Text.of(TextColors.GRAY, "current query back to its"),
                        Text.of(TextColors.GRAY, "default search option")
                ))
                .build()
        );
    }
}
