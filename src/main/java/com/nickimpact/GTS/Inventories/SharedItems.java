package com.nickimpact.GTS.Inventories;

import com.nickimpact.GTS.Configuration.MessageConfig;
import com.nickimpact.GTS.GTS;
import com.sun.org.apache.xml.internal.serialize.TextSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.RepresentedPlayerData;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.data.type.SkullTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by Nick on 12/15/2016.
 */
class SharedItems {

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
        button.offer(Keys.DISPLAY_NAME, up ? MessageConfig.getMessage("Menus.Page Up") : MessageConfig.getMessage("Menus.Page Down"));
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
        button.offer(Keys.DISPLAY_NAME, MessageConfig.getMessage("Menus.Refresh List"));
        button.offer(Keys.DYE_COLOR, DyeColors.YELLOW);
        return button;
    }

    static ItemStack balance(Player p){
        ItemStack dummy = ItemStack.builder()
                .itemType(ItemTypes.GOLD_INGOT)
                .build();
        if(GTS.getInstance().getEconomy() == null){
            dummy.offer(Keys.DISPLAY_NAME, MessageConfig.getMessage("Menus.Balance Icon", 0));
            return dummy;
        }
        Optional<UniqueAccount> acc = GTS.getInstance().getEconomy().getOrCreateAccount(p.getUniqueId());
        if(acc.isPresent()) {
            dummy.offer(Keys.DISPLAY_NAME, MessageConfig.getMessage("Menus.Balance Icon", acc.get().getBalance(GTS.getInstance().getEconomy().getDefaultCurrency())));
        } else {
            dummy.offer(Keys.DISPLAY_NAME, MessageConfig.getMessage("Menus.Balance Icon", 0));
        }
        return dummy;
    }

    static ItemStack search(List<String> pokemon){
        ItemStack dummy = ItemStack.builder()
                .itemType(ItemTypes.MAP)
                .build();
        dummy.offer(Keys.DISPLAY_NAME, MessageConfig.getMessage("Menus.Search For.Title"));
        List<Text> lore = new ArrayList<>();
        if(pokemon != null) {
            for (String s : pokemon) {
                lore.add(MessageConfig.getMessage("Menus.Search For.Lore Format", s));
            }
            dummy.offer(Keys.ITEM_LORE, lore);
        }
        return dummy;
    }

    static ItemStack playerIcon(Player p){
        ItemStack dummy = ItemStack.builder()
                .itemType(ItemTypes.SKULL)
                .build();
        dummy.offer(Keys.DISPLAY_NAME, MessageConfig.getMessage("Menus.Player Icon", p.getName()));
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
        button.offer(Keys.DISPLAY_NAME, MessageConfig.getMessage("Menus.Player Listings"));
        return button;
    }

    static ItemStack lastMenu(){
        Optional<ItemType> type = Sponge.getRegistry().getType(ItemType.class, "pixelmon:eject_button");
        if(type.isPresent()){
            ItemStack button = ItemStack.builder()
                    .itemType(type.get())
                    .build();
            button.offer(Keys.DISPLAY_NAME, MessageConfig.getMessage("Menus.Last Menu"));
            return button;
        } else {
            ItemStack button = ItemStack.builder()
                    .itemType(ItemTypes.REDSTONE_BLOCK)
                    .build();
            button.offer(Keys.DISPLAY_NAME, MessageConfig.getMessage("Menus.Last Menu"));
            return button;
        }
    }
}
