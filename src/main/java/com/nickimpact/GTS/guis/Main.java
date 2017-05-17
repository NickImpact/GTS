package com.nickimpact.GTS.guis;

import com.google.common.collect.Lists;
import com.nickimpact.GTS.GTSInfo;
import com.nickimpact.GTS.configuration.MessageConfig;
import com.nickimpact.GTS.GTS;
import com.nickimpact.GTS.utils.Lot;
import com.nickimpact.GTS.utils.LotCache;
import com.nickimpact.GTS.utils.PokemonItem;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
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
    private static HashMap<Player, List<LotCache>> playerTokens = new HashMap<>();
    private static HashMap<Player, Integer> playerMax = new HashMap<>();

    public static void showGUI(Player p, int page, boolean search, final List<LotCache> pokemon){
        playerPage.put(p, page);
        playerMax.put(p, GTS.getInstance().getLots().size() / 28 + 1);
        playerSearch.put(p, search);
        playerTokens.put(p, pokemon);

        Inventory inv = registerInventory(p, page, search);
        boolean open = setupGUI(inv, p, page, search, pokemon);
        if(open){
            p.openInventory(inv, Cause.of(NamedCause.source(GTS.getInstance())));
        } else {
            for(Text text : MessageConfig.getMessages("Generic.Search.Error.Not Found", null))
                p.sendMessage(text);
        }
    }

    private static boolean setupGUI(Inventory inv, Player p, int page, boolean search, List<LotCache> lots) {
        int index = (page - 1) * 28;

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
        inv.query(new SlotPos(4,5)).offer(SharedItems.search(search ? lots : Lists.newArrayList()));
        inv.query(new SlotPos(6,5)).offer(SharedItems.listings());

        for(x = 0, y = 0; y < 4; index++){
            if(index >= lots.size()){
                break;
            }
            if(x == 7){
                x = 0;
                y++;
            }
            if(lots.get(index).isExpired()) continue;
            Lot lot = lots.get(index).getLot();
            PokemonItem item = lot.getItem();
            inv.query(new SlotPos(x, y)).offer(item.getItem(lots.get(index)));
            x++;
        }

        if(x == 0 && y == 0 && search){
            return false;
        }

        return true;
    }

    private static Inventory registerInventory(Player p, int page, boolean search){
        return Inventory.builder().of(InventoryArchetypes.DOUBLE_CHEST)
            .property("inventorytitle",
                    (search ? InventoryTitle.of(Text.of("GTS | Search"))
                            : InventoryTitle.of(Text.of("GTS | Page ", page, "/", GTS.getInstance().getLots().size() / 28 + 1))))
            .listener(ClickInventoryEvent.class, e -> {
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
                                    LotCache lot = null;
                                    for(LotCache lc : GTS.getInstance().getLots()) {
                                        if (lc.getLot().getLotID() == Integer.valueOf(lotID.substring(lotID.indexOf(": ") + 2))) {
                                            lot = lc;
                                            break;
                                        }
                                    }

                                    if (lot == null) {
                                        for(Text text : MessageConfig.getMessages("Generic.Purchase.Error.Already Sold", null))
                                            p.sendMessage(text);
                                    } else if (GTS.getInstance().getSql().isExpired(lot.getLot().getLotID())) {
                                        for(Text text : MessageConfig.getMessages("Generic.Purchase.Error.Expired", null))
                                            p.sendMessage(text);
                                    } else {
                                        final LotCache l = lot;
                                        Sponge.getScheduler().createTaskBuilder().execute(() -> {
                                            LotUI.showGUI(p, l, playerSearch.get(p), playerTokens.get(p), false);
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
                                        showGUI(p, playerPage.get(p), playerSearch.get(p), playerSearch.get(p) ? playerTokens.get(p) : GTS.getInstance().getLots());
                                    } else {
                                        // Player Listings
                                        PlayerListings.showGUI(p, 1, playerSearch.get(p), playerTokens.get(p));
                                    }
                                }).delayTicks(1).submit(GTS.getInstance());
                            }
                        }
                    }
                }
            })
        .build(GTS.getInstance());
    }
}
