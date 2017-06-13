package com.nickimpact.GTS.guis;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.GTS.GTS;
import com.nickimpact.GTS.configuration.MessageConfig;
import com.nickimpact.GTS.guis.InventoryIcon;
import com.nickimpact.GTS.utils.LotCache;
import com.pixelmonmod.pixelmon.config.PixelmonItems;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.util.helpers.SpriteHelper;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.RepresentedPlayerData;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.data.type.SkullTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class SharedItems {

    private static HashMap<String, Optional<Object>> textOptions = Maps.newHashMap();

    /**
     * This method is used to create a border representation of an item
     *
     * @param slot The slot this border piece will be placed in
     * @param color The dye color of the border piece
     * @return An icon for an Inventory display
     */
    public static InventoryIcon forgeBorderIcon(int slot, DyeColor color){
        return new InventoryIcon(slot, ItemStack.builder()
                .itemType(ItemTypes.STAINED_GLASS_PANE)
                .quantity(1)
                .keyValue(Keys.DISPLAY_NAME, Text.of(TextColors.BLACK, ""))
                .keyValue(Keys.DYE_COLOR, color)
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

    static InventoryIcon pageIcon(int slot, boolean nextOrLast, int curr, int next){
        return new InventoryIcon(slot, ItemStack.builder()
                .itemType(ItemTypes.DYE)
                .quantity(1)
                .keyValue(Keys.DISPLAY_NAME, nextOrLast ? Text.of(
                            TextColors.GREEN, "\u2192 ", MessageConfig.getMessage("UI.Items.Page Up", null),
                            TextColors.GREEN, " \u2192"
                    ) : Text.of(
                            TextColors.RED, "\u2190 ", MessageConfig.getMessage("UI.Items.Page Down", null),
                            TextColors.RED, " \u2190"
                    )
                )
                .keyValue(Keys.DYE_COLOR, nextOrLast ? DyeColors.LIME : DyeColors.RED)
                .keyValue(Keys.ITEM_LORE, Lists.newArrayList(
                        Text.of(TextColors.GRAY, "Curr Page: ", TextColors.DARK_AQUA, curr),
                        Text.of(TextColors.GRAY, "Next Page: ", TextColors.DARK_AQUA, next)
                ))
                .build()
        );
    }

    static InventoryIcon refreshIcon(int slot){
        return new InventoryIcon(slot, ItemStack.builder()
                .itemType(ItemTypes.BOOK)
                .quantity(1)
                .keyValue(Keys.DISPLAY_NAME, MessageConfig.getMessage("UI.Items.Refresh List", null))
                .build()
        );
    }

    static InventoryIcon playerIcon(int slot, Player player){
        ItemStack icon = ItemStack.builder()
                .itemType(ItemTypes.SKULL)
                .build();

        textOptions.clear();
        textOptions.put("player", Optional.of(player.getName()));
        icon.offer(Keys.DISPLAY_NAME, MessageConfig.getMessage("UI.Items.Player Icon", textOptions));
        icon.offer(Keys.SKULL_TYPE, SkullTypes.PLAYER);

        RepresentedPlayerData skinData = Sponge.getGame().getDataManager().getManipulatorBuilder(RepresentedPlayerData.class).get().create();
        skinData.set(Keys.REPRESENTED_PLAYER, player.getProfile());
        icon.offer(skinData);

        return new InventoryIcon(slot, icon);
    }

    static InventoryIcon balanceIcon(int slot, Player player){
        ItemStack icon = ItemStack.builder()
                .itemType(ItemTypes.GOLD_INGOT)
                .build();

        textOptions.clear();

        if(GTS.getInstance().getEconomy() == null){
            textOptions.put("balance", Optional.of(0));
            icon.offer(Keys.DISPLAY_NAME, MessageConfig.getMessage("UI.Items.Balance Icon", textOptions));
        }
        Optional<UniqueAccount> acc = GTS.getInstance().getEconomy().getOrCreateAccount(player.getUniqueId());
        if(acc.isPresent()) {
            textOptions.put("curr_symbol", Optional.of(GTS.getInstance().getEconomy().getDefaultCurrency().getSymbol().toPlain()));
            textOptions.put("balance", Optional.of(acc.get().getBalance(GTS.getInstance().getEconomy().getDefaultCurrency())));
            icon.offer(Keys.DISPLAY_NAME, MessageConfig.getMessage("UI.Items.Balance Icon", textOptions));
        } else {
            textOptions.put("balance", Optional.of(0));
            icon.offer(Keys.DISPLAY_NAME, MessageConfig.getMessage("UI.Items.Balance Icon", textOptions));
        }

        return new InventoryIcon(slot, icon);
    }

    static InventoryIcon searchIcon(int slot, final List<String> pokemon){
        ItemStack dummy = ItemStack.builder()
                .itemType(ItemTypes.MAP)
                .build();
        dummy.offer(Keys.DISPLAY_NAME, MessageConfig.getMessage("UI.Items.Search For.Title", null));
        List<Text> lore = new ArrayList<>();
        textOptions.clear();
        for (String poke : pokemon) {
            if(lore.stream().noneMatch(p -> p.toPlain().equalsIgnoreCase(EnumPokemon.getFromNameAnyCase(poke).name))) {
                textOptions.put("pokemon", Optional.of(EnumPokemon.getFromNameAnyCase(poke).name));
                lore.add(MessageConfig.getMessage("UI.Items.Search For.Lore Format", textOptions));
            }
        }
        dummy.offer(Keys.ITEM_LORE, lore);

        return new InventoryIcon(slot, dummy);
    }

    static InventoryIcon playerListingsIcon(int slot){
        return new InventoryIcon(slot, ItemStack.builder()
                .itemType(ItemTypes.WRITTEN_BOOK)
                .quantity(1)
                .keyValue(Keys.DISPLAY_NAME, MessageConfig.getMessage("UI.Items.Player Listings", null))
                .build()

        );
    }

    public static InventoryIcon confirmIcon(int slot){
        return new InventoryIcon(slot, ItemStack.builder()
                .itemType(ItemTypes.DYE)
                .quantity(1)
                .keyValue(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "Confirm Action"))
                .keyValue(Keys.DYE_COLOR, DyeColors.LIME)
                .build()
        );
    }

    public static InventoryIcon denyIcon(int slot){
        return new InventoryIcon(slot, ItemStack.builder()
                .itemType(ItemTypes.DYE)
                .quantity(1)
                .keyValue(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Cancel Action"))
                .keyValue(Keys.DYE_COLOR, DyeColors.RED)
                .build()
        );
    }

    static InventoryIcon lastMenu(int slot){
        return new InventoryIcon(slot, ItemStack.builder()
                .itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:eject_button").get())
                .quantity(1)
                .keyValue(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Last Menu"))
                .build()
        );
    }

    public static InventoryIcon cancelIcon(int slot){
        return new InventoryIcon(slot, ItemStack.builder()
                .itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:trash_can").orElse(ItemTypes.BARRIER))
                .keyValue(Keys.DISPLAY_NAME, Text.of(
                        TextColors.RED, TextStyles.BOLD, "Reset Option"
                ))
                .keyValue(Keys.ITEM_LORE, Lists.newArrayList(
                        Text.of(TextColors.GRAY, "Click here to reset the"),
                        Text.of(TextColors.GRAY, "current query back to its"),
                        Text.of(TextColors.GRAY, "default search option")
                ))
                .build()
        );
    }
}
