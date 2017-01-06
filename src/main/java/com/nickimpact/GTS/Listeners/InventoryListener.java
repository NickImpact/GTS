package com.nickimpact.GTS.Listeners;

import com.nickimpact.GTS.Inventories.Admin;
import com.nickimpact.GTS.Inventories.LotUI;
import com.nickimpact.GTS.Inventories.Main;
import com.nickimpact.GTS.Inventories.PlayerListings;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.common.item.inventory.custom.CustomInventory;

/**
 * Created by Nick on 12/15/2016.
 */
public class InventoryListener {

    @Listener
    public void onClickEvent(ClickInventoryEvent event, @Root Player p){
        if(event.getTargetInventory().getName().get().contains("GTS | Page") || event.getTargetInventory().getName().get().equalsIgnoreCase("GTS | Search")){
            Main.handleClickEvent(event, p);
        } else if(event.getTargetInventory().getName().get().equalsIgnoreCase("GTS | Confirm")){
            LotUI.handleClickEvent(event, p);
        } else if(event.getTargetInventory().getName().get().equalsIgnoreCase("GTS | Admin")){
            Admin.handleClickEvent(event, p);
        } else if(event.getTargetInventory().getName().get().equalsIgnoreCase("GTS | Your Listings")){
            PlayerListings.handleClickEvent(event, p);
        }
    }

    @Listener
    public void onCloseEvent(InteractInventoryEvent.Close event, @Root Player p){
        if(event.getTargetInventory() instanceof CustomInventory) {
            if (event.getTargetInventory().getName().get().contains("GTS | Page") || event.getTargetInventory().getName().get().equalsIgnoreCase("GTS | Search")) {
                Main.handleCloseEvent(p);
            } else if(event.getTargetInventory().getName().get().equalsIgnoreCase("GTS | Confirm")){
                LotUI.handleCloseEvent(p);
            } else if(event.getTargetInventory().getName().get().equalsIgnoreCase("GTS | Admin")){
                Admin.handleCloseEvent(p);
            } else if(event.getTargetInventory().getName().get().equalsIgnoreCase("GTS | Your Listings")){
                PlayerListings.handleCloseEvent(p);
            }
        }
    }
}
