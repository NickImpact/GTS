package com.nickimpact.GTS.Inventories;

import com.google.common.collect.Lists;
import com.nickimpact.GTS.Configuration.MessageConfig;
import com.nickimpact.GTS.GTS;
import com.nickimpact.GTS.Utils.Lot;
import com.nickimpact.GTS.Utils.PokemonItem;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.filter.cause.Root;
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

import java.util.HashMap;
import java.util.List;

/**
 * Created by Nick on 12/15/2016.
 */
public class Main {

    private static HashMap<Player, Integer> playerPage = new HashMap<>();
    private static HashMap<Player, Boolean> playerSearch = new HashMap<>();
    private static HashMap<Player, List<String>> playerTokens = new HashMap<>();
    private static HashMap<Player, Integer> playerMax = new HashMap<>();

    public static void showGUI(Player p, int page, boolean search, List<String> pokemon){
        playerPage.put(p, page);
        playerMax.put(p, GTS.getInstance().getSql().getAllLots().size() / 28 + 1);

        playerSearch.put(p, search);
        playerTokens.put(p, pokemon);
        Inventory inv = Inventory.builder().of(InventoryArchetypes.DOUBLE_CHEST)
                .property("inventorytitle",
                        (search ? InventoryTitle.of(Text.of("GTS | Search"))
                        : InventoryTitle.of(Text.of("GTS | Page ", page, "/", playerMax.get(p)))))
                .build(GTS.getInstance());
        boolean open = setupGUI(inv, p, page, search, pokemon);
        if(open){
            p.openInventory(inv, Cause.of(NamedCause.source(GTS.getInstance())));
        } else {
            p.sendMessage(MessageConfig.getMessage("GTS.Search.Error.Not Found"));
        }
    }

    private static boolean setupGUI(Inventory inv, Player p, int page, boolean search, List<String> pokemon) {
        int index = (page - 1) * 28;
        List<Lot> lots = GTS.getInstance().getSql().getAllLots();

        int x;
        int y;
        for(x = 7, y = 0; y <= 4; y++) {
            inv.query(new SlotPos(x, y)).offer(SharedItems.border("Black"));
        }

        for(x = 0, y = 4; x < 7; x++){
            inv.query(new SlotPos(x, y)).offer(SharedItems.border("Black"));
        }

        inv.query(new SlotPos(8,0)).offer(SharedItems.page(true));
        inv.query(new SlotPos(8,1)).offer(SharedItems.page(false));
        inv.query(new SlotPos(8,3)).offer(SharedItems.refreshList());
        inv.query(new SlotPos(0,5)).offer(SharedItems.playerIcon(p));
        inv.query(new SlotPos(1,5)).offer(SharedItems.border("Green"));
        inv.query(new SlotPos(2,5)).offer(SharedItems.balance(p));
        inv.query(new SlotPos(4,5)).offer(SharedItems.search(pokemon));
        inv.query(new SlotPos(6,5)).offer(SharedItems.listings());

        for(x = 0, y = 0; y < 4; index++){
            if(index >= lots.size()){
                break;
            }
            if(x == 7){
                x = 0;
                y++;
            }
            if (search) {
                Lot lot = lots.get(index);
                for(String poke : pokemon){
                    if(lot.getItem().getName().equalsIgnoreCase(poke)){
                        PokemonItem item = lot.getItem();
                        inv.query(new SlotPos(x, y)).offer(item.getItem(lot.getLotID(), GTS.getInstance().getSql().getEnd(lot.getLotID())));
                        x++;
                    }
                }
                if(x == 0 && y == 0){
                    return false;
                }
            } else {
                Lot lot = lots.get(index);
                PokemonItem item = lot.getItem();
                inv.query(new SlotPos(x, y)).offer(item.getItem(lot.getLotID(), GTS.getInstance().getSql().getEnd(lot.getLotID())));
                x++;
            }
        }

        return true;
    }

    public static void handleClickEvent(ClickInventoryEvent e, @Root Player p) {
        e.setCancelled(true);
        if(!(e instanceof ClickInventoryEvent.Shift)) {
            if (e.getTransactions().size() != 0) {
                int slot = ((SlotAdapter) e.getTransactions().get(0).getSlot()).slotNumber;
                if (slot < 54) {
                    if ((slot >= 0 && slot <= 6) || (slot >= 9 && slot <= 15)
                            || slot >= 18 && slot <= 24 || slot >= 27 && slot <= 33) {
                        if (!e.getCursorTransaction().getFinal().getType().equals(ItemTypes.NONE)) {
                            // Lot data

                            String lotID = e.getCursorTransaction().getFinal().get(Keys.ITEM_LORE).get().get(0).toPlain();
                            Lot lot = GTS.getInstance().getSql().getLot(Integer.valueOf(lotID.substring(lotID.indexOf(": ") + 2)));

                            if (lot == null) {
                                p.sendMessage(MessageConfig.getMessage("GTS.Purchase.Error.Already Sold"));
                            } else if (GTS.getInstance().getSql().isExpired(lot.getLotID())) {
                                p.sendMessage(MessageConfig.getMessage("GTS.Purchase.Error.Expired"));
                            } else {
                                Sponge.getScheduler().createTaskBuilder().execute(() -> {
                                    LotUI.showGUI(p, lot, playerSearch.get(p), playerTokens.get(p), false);
                                    playerPage.remove(p);
                                    playerMax.remove(p);
                                    playerSearch.remove(p);
                                    playerTokens.remove(p);
                                }).delayTicks(1).submit(GTS.getInstance());
                            }
                        }
                    } else if(slot == 8 || slot == 17 || slot == 35 || slot == 51) {
                        Sponge.getScheduler().createTaskBuilder().execute(() -> {
                            if (slot == 8) {
                                // Page up
                                if (playerPage.get(p) < playerMax.get(p)) {
                                    showGUI(p, playerPage.get(p) + 1, playerSearch.get(p), playerTokens.get(p));
                                } else {
                                    showGUI(p, 1, playerSearch.get(p), playerTokens.get(p));
                                }
                            } else if (slot == 17) {
                                // Page down
                                if (playerPage.get(p) > 1) {
                                    showGUI(p, playerPage.get(p) - 1, playerSearch.get(p), playerTokens.get(p));
                                } else {
                                    showGUI(p, playerMax.get(p), playerSearch.get(p), playerTokens.get(p));
                                }
                            } else if (slot == 35) {
                                // Refresh Listings
                                showGUI(p, playerPage.get(p), playerSearch.get(p), playerTokens.get(p));
                            } else {
                                // Player Listings
                                PlayerListings.showGUI(p, 1, playerSearch.get(p), playerTokens.get(p));
                                playerMax.remove(p);
                                playerSearch.remove(p);
                                playerPage.remove(p);
                                playerTokens.remove(p);
                            }
                        }).delayTicks(1).submit(GTS.getInstance());
                    }
                }
            }
        }
    }

    public static void handleCloseEvent(@Root Player p){
        playerMax.remove(p);
        playerSearch.remove(p);
        playerPage.remove(p);
        playerTokens.remove(p);
    }

    public static int getCurrPage(Player p){
        if(playerPage.containsKey(p)){
            return playerPage.get(p);
        }
        return 1;
    }

    public static boolean getCurrSearch(Player p){
        if(playerSearch.containsKey(p)){
            return playerSearch.get(p);
        }
        return false;
    }

    public static List<String> getPokemon(Player p){
        if(playerTokens.containsKey(p)){
            return playerTokens.get(p);
        }
        return Lists.newArrayList();
    }
}
