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
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.item.inventory.adapter.impl.slots.SlotAdapter;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Nick on 12/15/2016.
 */
public class PlayerListings {

    private static HashMap<Player, Integer> pages = new HashMap<>();
    private static HashMap<Player, Boolean> search = new HashMap<>();
    private static HashMap<Player, List<String>> tokens = new HashMap<>();

    public static void showGUI(Player p, int page, boolean searching, List<String> pokemon){
        Inventory inv = Inventory.builder().of(InventoryArchetypes.DOUBLE_CHEST)
                .property("inventorytitle",
                        (InventoryTitle.of(Text.of("GTS | Your Listings"))))
                .build(GTS.getInstance());
        pages.put(p, page);
        search.put(p, searching);
        tokens.put(p, pokemon);
        if(setupGUI(inv, p, page)){
            p.openInventory(inv, Cause.of(NamedCause.source(GTS.getInstance())));
        } else {
            p.sendMessage(MessageConfig.getMessage("GTS.Display.Error.Empty"));
            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                Main.showGUI(p, 1, searching, pokemon);
            }).delayTicks(1).submit(GTS.getInstance());
        }
    }

    private static boolean setupGUI(Inventory inv, Player p, int page){
        int index = (page - 1) * 42;
        List<Lot> lots = GTS.getInstance().getSql().getPlayerLots(p.getUniqueId());
        if(lots.size() == 0){
            return false;
        }

        int x;
        int y;
        for(x = 7, y = 0; y <= 5; y++) {
            inv.query(new SlotPos(x, y)).offer(SharedItems.border("Black"));
        }

        inv.query(new SlotPos(8, 0)).offer(SharedItems.page(true));
        inv.query(new SlotPos(8, 1)).offer(SharedItems.page(false));
        inv.query(new SlotPos(8, 3)).offer(SharedItems.refreshList());
        inv.query(new SlotPos(8, 5)).offer(SharedItems.lastMenu());

        for(x = 0, y = 0; y < 6; x++, index++){
            if(index >= lots.size()){
                break;
            }
            if(x == 7){
                x = 0;
                y++;
            }

            Lot lot = lots.get(index);
            PokemonItem item = lot.getItem();
            inv.query(new SlotPos(x, y)).offer(item.getItem(lot.getLotID(), GTS.getInstance().getSql().getEnd(lot.getLotID())));
        }

        return true;
    }

    public static void handleClickEvent(ClickInventoryEvent e, @Root Player p) {
        e.setCancelled(true);
        if(!(e instanceof ClickInventoryEvent.Shift) && !(e instanceof ClickInventoryEvent.Drop)) {
            if (e.getTransactions().size() != 0) {
                int slot = ((SlotAdapter) e.getTransactions().get(0).getSlot()).slotNumber;
                if (slot < 54) {
                    if (slot % 9 < 7) {
                        if (!e.getCursorTransaction().getFinal().getType().equals(ItemTypes.NONE)) {
                            String lotID = e.getCursorTransaction().getFinal().get(Keys.ITEM_LORE).get().get(0).toPlain();
                            Lot lot = GTS.getInstance().getSql().getLot(Integer.valueOf(lotID.substring(lotID.indexOf(": ") + 2)));

                            if (lot == null) {
                                p.sendMessage(MessageConfig.getMessage("GTS.Purchase.Error.Already Sold"));
                            } else if (GTS.getInstance().getSql().isExpired(lot.getLotID())) {
                                p.sendMessage(MessageConfig.getMessage("GTS.Purchase.Error.Expired"));
                            } else {
                                Sponge.getScheduler().createTaskBuilder().execute(() -> {
                                    LotUI.showGUI(p, lot, false, Lists.newArrayList(), false);
                                    pages.remove(p);
                                }).delayTicks(1).submit(GTS.getInstance());
                            }
                        }
                    } else {
                        Sponge.getScheduler().createTaskBuilder().execute(() -> {
                            if(slot == 8 || slot == 17) {
                                int size = GTS.getInstance().getSql().getAllLots().size();
                                if (slot == 8) {
                                    if (pages.get(p) < size / 42 + 1) {
                                        showGUI(p, pages.get(p) + 1, search.get(p), tokens.get(p));
                                    } else {
                                        showGUI(p, 1, search.get(p), tokens.get(p));
                                    }
                                } else {
                                    if (pages.get(p) > 1) {
                                        showGUI(p, pages.get(p) - 1, search.get(p), tokens.get(p));
                                    } else {
                                        showGUI(p, size / 42 + 1, search.get(p), tokens.get(p));
                                    }
                                }
                            } else if(slot == 35){
                                showGUI(p, pages.get(p), search.get(p), tokens.get(p));
                            } else if(slot == 53){
                                Main.showGUI(p, 1, search.get(p), tokens.get(p));
                            }
                        }).delayTicks(1).submit(GTS.getInstance());
                    }
                }
            }
        }
    }

    public static void handleCloseEvent(Player p) {

    }

    public static int getCurrPage(Player p){
        if(pages.containsKey(p)){
            return pages.get(p);
        }
        return 1;
    }

    public static boolean getCurrSearch(Player p){
        if(search.containsKey(p)){
            return search.get(p);
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
