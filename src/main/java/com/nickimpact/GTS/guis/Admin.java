package com.nickimpact.GTS.guis;

import com.google.common.collect.Lists;
import com.nickimpact.GTS.configuration.MessageConfig;
import com.nickimpact.GTS.GTS;
import com.nickimpact.GTS.utils.Lot;
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
import org.spongepowered.common.item.inventory.adapter.impl.slots.SlotAdapter;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Nick on 12/15/2016.
 */
public class Admin {

    private static HashMap<Player, Integer> pages = new HashMap<>();

    public static void showGUI(Player p, int page){
        Inventory inv = registerInventory(p);
        pages.put(p, page);
        setupGUI(inv, page);
        p.openInventory(inv, Cause.of(NamedCause.source(GTS.getInstance())));
    }

    private static void setupGUI(Inventory inv, int page) {
        int index = (page - 1) * 42;
        List<Lot> lots = GTS.getInstance().getSql().getAllLots();

        int x;
        int y;
        for(x = 7, y = 0; y <= 5; y++) {
            inv.query(new SlotPos(x, y)).offer(SharedItems.border("Black"));
        }

        inv.query(new SlotPos(8, 0)).offer(SharedItems.page(true));
        inv.query(new SlotPos(8, 1)).offer(SharedItems.page(false));
        inv.query(new SlotPos(8, 3)).offer(SharedItems.refreshList());


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
            inv.query(new SlotPos(x, y)).offer(item.getItem(lot));
        }
    }

    public static Inventory registerInventory(Player p){
        return Inventory.builder().of(InventoryArchetypes.DOUBLE_CHEST)
            .property("inventorytitle",
                    (InventoryTitle.of(Text.of("GTS | Admin"))))
            .listener(ClickInventoryEvent.class, e -> {
                e.setCancelled(true);
                if(!(e instanceof ClickInventoryEvent.Shift) && !(e instanceof ClickInventoryEvent.Drop)) {
                    if (e.getTransactions().size() != 0) {
                        int slot = ((SlotAdapter) e.getTransactions().get(0).getSlot()).slotNumber;
                        if (slot < 54) {
                            if(slot % 9 < 7) {
                                if (!e.getCursorTransaction().getFinal().getType().equals(ItemTypes.NONE)) {
                                    String lotID = e.getCursorTransaction().getFinal().get(Keys.ITEM_LORE).get().get(0).toPlain();
                                    Lot lot = GTS.getInstance().getSql().getLot(Integer.valueOf(lotID.substring(lotID.indexOf(": ") + 2)));

                                    if (lot == null) {
                                        for(Text text : MessageConfig.getMessages("Generic.Purchase.Error.Already Sold", null))
                                            p.sendMessage(text);
                                    } else if (GTS.getInstance().getSql().isExpired(lot.getLotID())) {
                                        for(Text text : MessageConfig.getMessages("Generic.Purchase.Error.Expired", null))
                                            p.sendMessage(text);
                                    } else {
                                        Sponge.getScheduler().createTaskBuilder().execute(() -> {
                                            LotUI.showGUI(p, lot, false, Lists.newArrayList(), true);
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
                                                showGUI(p, pages.get(p) + 1);
                                            } else {
                                                showGUI(p, 1);
                                            }
                                        } else {
                                            if (pages.get(p) > 1) {
                                                showGUI(p, pages.get(p) - 1);
                                            } else {
                                                showGUI(p, size / 42 + 1);
                                            }
                                        }
                                    } else if(slot == 35){
                                        showGUI(p, pages.get(p));
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
