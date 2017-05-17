package com.nickimpact.GTS.listeners;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.GTS.configuration.MessageConfig;
import com.nickimpact.GTS.GTS;
import com.nickimpact.GTS.utils.LotCache;
import com.nickimpact.GTS.utils.LotUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.util.HashMap;
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
                List<LotCache> lots = Lists.newArrayList();
                for(LotCache lot : GTS.getInstance().getLots()){
                    if(lot.getLot().getOwner().equals(player.get().getUniqueId())){
                        lots.add(lot);
                    }
                }

                for(LotCache lot : lots){
                    if(lot.isExpired()){
                        LotUtils.givePlayerPokemon(player.get().getUniqueId(), lot.getLot());
                        for (int i = 0; i < GTS.getInstance().getLots().size(); i++){
                            if (GTS.getInstance().getLots().get(i).getLot().getLotID() == lot.getLot().getLotID()) {
                                GTS.getInstance().getLots().remove(i);
                                break;
                            }
                        }
                        LotUtils.deleteLot(lot.getLot().getLotID());

                        HashMap<String, Optional<Object>> textOptions = Maps.newHashMap();
                        textOptions.put("pokemon", Optional.of(lot.getLot().getItem().getName()));

                        player.get().sendMessage(MessageConfig.getMessage("Generic.Remove.Expired", textOptions));
                    }
                }
            } else {
                GTS.getInstance().getLogger().error("Something went terribly wrong with the join listener!");
                GTS.getInstance().getLogger().error("The UUID (" + event.getTargetEntity().getUniqueId() + ") was not found in the player list..");
            }
        }).delay(5, TimeUnit.SECONDS).submit(GTS.getInstance());
    }
}