package com.nickimpact.GTS.Inventories;

import com.google.common.collect.Maps;
import com.nickimpact.GTS.Configuration.MessageConfig;
import com.nickimpact.GTS.GTS;
import com.nickimpact.GTS.Utils.Lot;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.RepresentedPlayerData;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.data.type.SkullTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Created by Nick on 12/15/2016.
 */
class SharedItems {

    private static HashMap<String, Optional<Object>> textOptions = Maps.newHashMap();

    static ItemStack border(String color){
        ItemStack border = ItemStack.builder()
                .itemType(ItemTypes.STAINED_GLASS_PANE)
                .build();

        border.offer(Keys.DISPLAY_NAME, Text.of(TextColors.BLACK, ""));
        if(color.equalsIgnoreCase("black")) {
            border.offer(Keys.DYE_COLOR, DyeColors.BLACK);
        } else if(color.equalsIgnoreCase("red")){
            border.offer(Keys.DYE_COLOR, DyeColors.RED);
        } else if(color.equalsIgnoreCase("green")){
            border.offer(Keys.DYE_COLOR, DyeColors.GREEN);
        } else {
            border.offer(Keys.DYE_COLOR, DyeColors.WHITE);
        }
        return border;
    }

    static ItemStack page(boolean up){
        ItemStack button = ItemStack.builder()
                .itemType(ItemTypes.DYE)
                .build();
        button.offer(Keys.DISPLAY_NAME, up ? MessageConfig.getMessage("UI Items.Page Up", null) : MessageConfig.getMessage("UI Items.Page Down", null));
        if(up) {
            button.offer(Keys.DYE_COLOR, DyeColors.GREEN);
        } else {
            button.offer(Keys.DYE_COLOR, DyeColors.RED);
        }
        return button;
    }

    static ItemStack refreshList(){
        ItemStack button = ItemStack.builder()
                .itemType(ItemTypes.BOOK)
                .build();
        button.offer(Keys.DISPLAY_NAME, MessageConfig.getMessage("UI Items.Refresh List", null));
        button.offer(Keys.DYE_COLOR, DyeColors.YELLOW);
        return button;
    }

    static ItemStack balance(Player p){
        ItemStack dummy = ItemStack.builder()
                .itemType(ItemTypes.GOLD_INGOT)
                .build();

        textOptions.clear();

        if(GTS.getInstance().getEconomy() == null){
            textOptions.put("balance", Optional.of(0));
            dummy.offer(Keys.DISPLAY_NAME, MessageConfig.getMessage("UI Items.Balance Icon", textOptions));
            return dummy;
        }
        Optional<UniqueAccount> acc = GTS.getInstance().getEconomy().getOrCreateAccount(p.getUniqueId());
        if(acc.isPresent()) {
            textOptions.put("curr_symbol", Optional.of(GTS.getInstance().getEconomy().getDefaultCurrency().getSymbol().toPlain()));
            textOptions.put("balance", Optional.of(acc.get().getBalance(GTS.getInstance().getEconomy().getDefaultCurrency())));
            dummy.offer(Keys.DISPLAY_NAME, MessageConfig.getMessage("UI Items.Balance Icon", textOptions));
        } else {
            textOptions.put("balance", Optional.of(0));
            dummy.offer(Keys.DISPLAY_NAME, MessageConfig.getMessage("UI Items.Balance Icon", textOptions));
        }
        return dummy;
    }

    static ItemStack search(List<Lot> pokemon){
        ItemStack dummy = ItemStack.builder()
                .itemType(ItemTypes.MAP)
                .build();
        dummy.offer(Keys.DISPLAY_NAME, MessageConfig.getMessage("UI Items.Search For.Title", null));
        List<Text> lore = new ArrayList<>();
        if(pokemon != null) {
            textOptions.clear();
            for (Lot lot : pokemon) {
                if(lore.stream().noneMatch(p -> p.toString().contains(lot.getItem().getName()))) {
                    textOptions.put("pokemon", Optional.of(lot.getItem().getName()));
                    lore.add(MessageConfig.getMessage("UI Items.Search For.Lore Format", textOptions));
                }
            }
            dummy.offer(Keys.ITEM_LORE, lore);
        }
        return dummy;
    }

    static ItemStack playerIcon(Player p){
        ItemStack dummy = ItemStack.builder()
                .itemType(ItemTypes.SKULL)
                .build();

        textOptions.clear();
        textOptions.put("player", Optional.of(p.getName()));
        dummy.offer(Keys.DISPLAY_NAME, MessageConfig.getMessage("UI Items.Player Icon", textOptions));
        dummy.offer(Keys.SKULL_TYPE, SkullTypes.PLAYER);

        RepresentedPlayerData skinData = Sponge.getGame().getDataManager().getManipulatorBuilder(RepresentedPlayerData.class).get().create();
        skinData.set(Keys.REPRESENTED_PLAYER, p.getProfile());
        dummy.offer(skinData);
        return dummy;
    }

    static ItemStack listings(){
        ItemStack button = ItemStack.builder()
                .itemType(ItemTypes.WRITTEN_BOOK)
                .build();
        button.offer(Keys.DISPLAY_NAME, MessageConfig.getMessage("UI Items.Player Listings", null));
        return button;
    }

    static ItemStack lastMenu(){
        Optional<ItemType> type = Sponge.getRegistry().getType(ItemType.class, "pixelmon:eject_button");
        if(type.isPresent()){
            ItemStack button = ItemStack.builder()
                    .itemType(type.get())
                    .build();
            button.offer(Keys.DISPLAY_NAME, MessageConfig.getMessage("UI Items.Last Menu", null));
            return button;
        } else {
            ItemStack button = ItemStack.builder()
                    .itemType(ItemTypes.REDSTONE_BLOCK)
                    .build();
            button.offer(Keys.DISPLAY_NAME, MessageConfig.getMessage("UI Items.Last Menu", null));
            return button;
        }
    }
}
