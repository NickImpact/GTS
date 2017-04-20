package com.nickimpact.GTS.guis;

import com.google.common.collect.Maps;
import com.nickimpact.GTS.configuration.MessageConfig;
import com.nickimpact.GTS.GTS;
import com.nickimpact.GTS.logging.Log;
import com.nickimpact.GTS.utils.Lot;
import com.nickimpact.GTS.utils.LotUtils;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
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
    private static HashMap<Player, List<Lot>> tokens = new HashMap<>();
    private static HashMap<Player, Boolean> isAdmin = new HashMap<>();

    public static void showGUI(Player p, Lot lot, boolean search, List<Lot> pokemon, boolean admin) {
        Inventory inv = registerInventory(p);

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

        inv.query(new SlotPos(1, 1)).offer(lot.getItem().getItem(lot));
        if(admin){
            inv.query(new SlotPos(3, 1)).offer(removeListing(true));
        } else {
            Optional<Player> player = Sponge.getServer().getPlayer(lot.getOwner());
            if(player.isPresent()){
                if(p.getUniqueId().equals(player.get().getUniqueId())){
                    inv.query(new SlotPos(3, 1)).offer(removeListing(false));
                } else {
                    if(lot.isAuction())
                        inv.query(new SlotPos(3, 1)).offer(confirmBid());
                    else
                        inv.query(new SlotPos(3, 1)).offer(confirmListing());
                }
            } else {
                if(lot.isAuction())
                    inv.query(new SlotPos(3, 1)).offer(confirmBid());
                else
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

    private static ItemStack confirmBid(){
        ItemStack button = ItemStack.builder()
                .itemType(ItemTypes.DYE)
                .build();
        button.offer(Keys.DYE_COLOR, DyeColors.GREEN);
        button.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "Bid for this Pokemon"));

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

    private static Inventory registerInventory(Player p) {
        return Inventory.builder().of(InventoryArchetypes.CHEST)
                .property("inventorytitle",
                        (InventoryTitle.of(Text.of("GTS | Confirm"))))
                .listener(ClickInventoryEvent.class, event -> {
                    event.setCancelled(true);
                    if(!(event instanceof ClickInventoryEvent.Shift) && !(event instanceof ClickInventoryEvent.Drop)) {
                        if (event.getTransactions().size() != 0) {
                            int slot = ((SlotAdapter) event.getTransactions().get(0).getSlot()).slotNumber;
                            if(slot == 12 || slot == 16) {
                                if (slot == 12) {
                                    HashMap<String, Optional<Object>> textOptions = Maps.newHashMap();
                                    textOptions.put("pokemon", Optional.of(lots.get(p).getItem().getName()));

                                    boolean admin = isAdmin.get(p);
                                    if (event.getCursorTransaction().getFinal().getType().equals(ItemTypes.ANVIL)) {
                                        if (admin) {
                                            if (GTS.getInstance().getSql().getLot(lots.get(p).getLotID()) != null) {
                                                if (event instanceof ClickInventoryEvent.Secondary) {
                                                    for(Text text : MessageConfig.getMessages("Administrative.LotUI.Delete", textOptions))
                                                        p.sendMessage(text);
                                                    GTS.getInstance().getSql().deleteLot(lots.get(p).getLotID());
                                                } else if (event instanceof ClickInventoryEvent.Primary) {
                                                    for(Text text : MessageConfig.getMessages("Administrative.LotUI.Remove", textOptions))
                                                        p.sendMessage(text);

                                                    textOptions.putAll(LotUtils.getInfo(lots.get(p).getItem().getPokemon(lots.get(p), p)));
                                                    Log log = LotUtils.forgeLog(Sponge.getServer().getPlayer(lots.get(p).getOwner()).get(), "Removal", textOptions);
                                                    GTS.getInstance().getSql().appendLog(log);
                                                    Optional<PlayerStorage> storage = PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID((MinecraftServer) Sponge.getServer(), p.getUniqueId());
                                                    if(storage.isPresent()) {
                                                        storage.get().addToParty(lots.get(p).getItem().getPokemon(lots.get(p), p));
                                                        storage.get().sendUpdatedList();
                                                    } else {
                                                        GTS.getInstance().getLogger().error("Error occurred in Lot Confirmation for " + p.getName());
                                                    }
                                                    GTS.getInstance().getSql().deleteLot(lots.get(p).getLotID());
                                                }
                                            } else {
                                                for(Text text : MessageConfig.getMessages("Generic.Remove.Failed", textOptions))
                                                    p.sendMessage(text);
                                            }
                                        } else {
                                            if (GTS.getInstance().getSql().getLot(lots.get(p).getLotID()) != null) {
                                                for(Text text : MessageConfig.getMessages("Generic.Remove.Success", textOptions))
                                                    p.sendMessage(text);
                                                Optional<PlayerStorage> storage = PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID((MinecraftServer) Sponge.getServer(), p.getUniqueId());
                                                if(storage.isPresent()) {
                                                    storage.get().addToParty(lots.get(p).getItem().getPokemon(lots.get(p), p));
                                                    storage.get().sendUpdatedList();
                                                } else {
                                                    GTS.getInstance().getLogger().error("Error occurred in Lot Confirmation for " + p.getName());
                                                }
                                                GTS.getInstance().getSql().deleteLot(lots.get(p).getLotID());
                                                textOptions.putAll(LotUtils.getInfo(lots.get(p).getItem().getPokemon(lots.get(p), p)));
                                                Log log = LotUtils.forgeLog(p, "Removal", textOptions);
                                                GTS.getInstance().getSql().appendLog(log);
                                            }
                                        }
                                    } else {
                                        Lot lot = GTS.getInstance().getSql().getLot(lots.get(p).getLotID());
                                        if(lot != null){
                                            if(lot.isAuction())
                                                LotUtils.bid(p, lot);
                                            else
                                                if(lot.isPokemon())
                                                    LotUtils.trade(p, lot);
                                                else
                                                    LotUtils.buyLot(p, lot);
                                        } else {
                                            for(Text text : MessageConfig.getMessages("Generic.Purchase.Failed", textOptions))
                                                p.sendMessage(text);
                                        }
                                    }

                                    Sponge.getScheduler().createTaskBuilder().execute(() -> {
                                        if(isAdmin.get(p)){
                                            Admin.showGUI(p, 1);
                                        } else {
                                            p.closeInventory(Cause.of(NamedCause.source(GTS.getInstance())));
                                        }
                                    }).delayTicks(1).submit(GTS.getInstance());
                                } else {
                                    Sponge.getScheduler().createTaskBuilder().execute(() ->
                                        p.closeInventory(Cause.of(NamedCause.source(GTS.getInstance())))
                                    ).delayTicks(1).submit(GTS.getInstance());
                                }
                            }
                        }
                    }
                })
                .build(GTS.getInstance());
    }
}
