package com.nickimpact.GTS.Inventories;

import com.google.common.collect.Lists;
import com.nickimpact.GTS.Configuration.MessageConfig;
import com.nickimpact.GTS.GTS;
import com.nickimpact.GTS.Utils.Lot;
import com.nickimpact.GTS.Utils.LotUtils;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerNotLoadedException;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.common.item.inventory.adapter.impl.slots.SlotAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Created by Nick on 12/15/2016.
 */
public class LotUI {

    private static HashMap<Player, Lot> lots = new HashMap<>();
    private static HashMap<Player, Boolean> isSearching = new HashMap<>();
    private static HashMap<Player, List<String>> tokens = new HashMap<>();
    private static HashMap<Player, Boolean> isAdmin = new HashMap<>();

    public static void showGUI(Player p, Lot lot, boolean search, List<String> pokemon, boolean admin) {
        Inventory inv = Inventory.builder().of(InventoryArchetypes.CHEST)
                .property("inventorytitle",
                        (InventoryTitle.of(Text.of("GTS | Confirm"))))
                .build(GTS.getInstance());

        lots.put(p, lot);
        isSearching.put(p, search);
        tokens.put(p, pokemon);
        isAdmin.put(p, admin);

        setupGUI(inv, p, lot, admin);
        p.openInventory(inv, Cause.of(NamedCause.source(GTS.getInstance())));
    }

    private static void setupGUI(Inventory inv, Player p, Lot lot, boolean admin) {

        int x;
        int y;
        for(x = 0, y = 0; y <= 2; x++) {
            if(x == 9){
                x = 0;
                y++;
            }
            if(x == 1 && y == 1){
                x++;
            }
            if(x == 3 && y == 1){
                x += 5;
            }
            inv.query(new SlotPos(x, y)).offer(SharedItems.border("Black"));
        }

        inv.query(new SlotPos(1, 1)).offer(lot.getItem().getItem(lot.getLotID(), GTS.getInstance().getSql().getEnd(lot.getLotID())));
        if(admin){
            inv.query(new SlotPos(3, 1)).offer(removeListing(true));
         } else {
            Optional<Player> player = Sponge.getServer().getPlayer(lot.getOwner());
            if(player.isPresent()){
                if(p.getUniqueId().equals(player.get().getUniqueId())){
                    inv.query(new SlotPos(3, 1)).offer(removeListing(false));
                } else {
                    inv.query(new SlotPos(3, 1)).offer(confirmListing());
                }
            } else {
                inv.query(new SlotPos(3, 1)).offer(confirmListing());
            }
        }
        inv.query(new SlotPos(5, 1)).offer(lot.getItem().setStats());
        inv.query(new SlotPos(7, 1)).offer(cancel());
    }

    private static ItemStack removeListing(boolean admin){
        ItemStack button = ItemStack.builder()
                .itemType(ItemTypes.ANVIL)
                .build();
        button.offer(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Remove from GTS"));
        if(admin){
            List<Text> lore = new ArrayList<>();
            lore.add(Text.EMPTY);
            lore.add(Text.of(TextColors.GRAY, "Left Click: ", TextColors.GREEN, "Remove, player gets pokemon back"));
            lore.add(Text.EMPTY);
            lore.add(Text.of(TextColors.GRAY, "Right Click: ", TextColors.RED, "Remove, delete pokemon"));

            button.offer(Keys.ITEM_LORE, lore);
        }

        return button;
    }

    private static ItemStack confirmListing(){
        ItemStack button = ItemStack.builder()
                .itemType(ItemTypes.DYE)
                .build();
        button.offer(Keys.DYE_COLOR, DyeColors.GREEN);
        button.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "Purchase Pokemon"));

        return button;
    }

    private static ItemStack cancel(){
        ItemStack button = ItemStack.builder()
                .itemType(ItemTypes.DYE)
                .build();
        button.offer(Keys.DYE_COLOR, DyeColors.RED);
        button.offer(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Cancel"));

        return button;
    }

    public static void handleClickEvent(ClickInventoryEvent event, Player p) {
        event.setCancelled(true);
        if(!(event instanceof ClickInventoryEvent.Shift) && !(event instanceof ClickInventoryEvent.Drop)) {
            if (event.getTransactions().size() != 0) {
                int slot = ((SlotAdapter) event.getTransactions().get(0).getSlot()).slotNumber;
                if(slot == 12 || slot == 16) {
                    if (slot == 12) {
                        boolean admin = isAdmin.get(p);
                        if (event.getCursorTransaction().getFinal().getType().equals(ItemTypes.ANVIL)) {
                            if (admin) {
                                if (GTS.getInstance().getSql().getLot(lots.get(p).getLotID()) != null) {
                                    if (event instanceof ClickInventoryEvent.Secondary) {
                                        p.sendMessage(MessageConfig.getMessage("GTS.Remove.Admin.Delete", lots.get(p).getItem().getName()));
                                        GTS.getInstance().getSql().deleteLot(lots.get(p).getLotID());
                                    } else if (event instanceof ClickInventoryEvent.Primary) {
                                        p.sendMessage(MessageConfig.getMessage("GTS.Remove.Admin.Remove", lots.get(p).getItem().getName()));
                                        Optional<PlayerStorage> storage = PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID((MinecraftServer) Sponge.getServer(), p.getUniqueId());
                                        if(storage.isPresent()) {
                                            storage.get().addToParty(lots.get(p).getItem().getPokemon(lots.get(p)));
                                            storage.get().sendUpdatedList();
                                        } else {
                                            GTS.getInstance().getLogger().error("Error occurred in Lot Confirmation for " + p.getName());
                                        }
                                        GTS.getInstance().getSql().deleteLot(lots.get(p).getLotID());
                                    }
                                } else {
                                    p.sendMessage(MessageConfig.getMessage("GTS.Remove.Failed", lots.get(p).getItem().getName()));
                                }
                            } else {
                                if (GTS.getInstance().getSql().getLot(lots.get(p).getLotID()) != null) {
                                    p.sendMessage(MessageConfig.getMessage("GTS.Remove.Success", lots.get(p).getItem().getName()));
                                    Optional<PlayerStorage> storage = PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID((MinecraftServer) Sponge.getServer(), p.getUniqueId());
                                    if(storage.isPresent()) {
                                        storage.get().addToParty(lots.get(p).getItem().getPokemon(lots.get(p)));
                                        storage.get().sendUpdatedList();
                                    } else {
                                        GTS.getInstance().getLogger().error("Error occurred in Lot Confirmation for " + p.getName());
                                    }
                                    GTS.getInstance().getSql().deleteLot(lots.get(p).getLotID());
                                }
                            }
                        } else {
                            if(GTS.getInstance().getSql().getLot(lots.get(p).getLotID()) != null){
                                LotUtils.buyLot(p, lots.get(p).getLotID());
                            } else {
                                p.sendMessage(MessageConfig.getMessage("GTS.Purchase.Failed", lots.get(p).getItem().getName()));
                            }
                        }

                        Sponge.getScheduler().createTaskBuilder().execute(() -> {
                            if(isAdmin.get(p)){
                                Admin.showGUI(p, 1);
                            } else {
                                Main.showGUI(p, 1, isSearching.get(p), tokens.get(p));
                            }
                        }).delayTicks(1).submit(GTS.getInstance());
                    } else {
                        Sponge.getScheduler().createTaskBuilder().execute(() -> {
                            Main.showGUI(p, 1, isSearching.get(p), tokens.get(p));
                        }).delayTicks(1).submit(GTS.getInstance());
                    }
                }
            }
        }
    }

    public static void handleCloseEvent(Player p) {

    }

    public static Lot getCurrLot(Player p){
        if(lots.containsKey(p)){
            return lots.get(p);
        }
        return null;
    }

    public static boolean getCurrSearch(Player p){
        if(isSearching.containsKey(p)){
            return isSearching.get(p);
        }
        return false;
    }

    public static boolean getIsAdmin(Player p){
        if(isAdmin.containsKey(p)){
            return isAdmin.get(p);
        }
        return false;
    }

    public static List<String> getPokemon(Player p){
        if(tokens.containsKey(p)){
            return tokens.get(p);
        }
        return Lists.newArrayList();
    }
}
