package com.nickimpact.GTS.Listeners;

import com.nickimpact.GTS.Configuration.MessageConfig;
import com.nickimpact.GTS.GTS;
import com.nickimpact.GTS.Utils.Lot;
import com.nickimpact.GTS.Utils.LotUtils;
import com.nickimpact.nbthandler.NBTHandler;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Created by Nick on 12/15/2016.
 */
public class JoinListener {

    @Listener
    public void onJoinEvent(ClientConnectionEvent.Join event){
        Sponge.getScheduler().createTaskBuilder().execute(() -> {
            final Optional<Player> player = Sponge.getServer().getPlayer(event.getTargetEntity().getUniqueId());
            if(player.isPresent()) {
                final List<Lot> lots = GTS.getInstance().getSql().getPlayerLots(event.getTargetEntity().getUniqueId());

                for(Lot lot : lots){
                    if(GTS.getInstance().getSql().isExpired(lot.getLotID())){
                        LotUtils.givePlayerPokemon(player.get(), GTS.getInstance().getSql().getLot(lot.getLotID()).getItem(), lot);
                        GTS.getInstance().getSql().deleteLot(lot.getLotID());
                        player.get().sendMessage(MessageConfig.getMessage("GTS.Remove.Expired", lot.getItem().getName()));
                    }
                }
            } else {
                GTS.getInstance().getLogger().error("Something went terribly wrong with the join listener!");
                GTS.getInstance().getLogger().error("The UUID (" + event.getTargetEntity().getUniqueId() + ") was not found in the player list..");
            }
        }).delay(5, TimeUnit.SECONDS).submit(GTS.getInstance());
    }
}