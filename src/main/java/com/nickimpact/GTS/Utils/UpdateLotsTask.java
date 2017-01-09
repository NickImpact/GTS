package com.nickimpact.GTS.Utils;

import com.nickimpact.GTS.Configuration.MessageConfig;
import com.nickimpact.GTS.GTS;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerNotLoadedException;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Created by Nick on 12/15/2016.
 */
public class UpdateLotsTask {

    public void setupUpdateTask(){
        Sponge.getScheduler().createTaskBuilder().interval(1, TimeUnit.SECONDS).execute(() -> {
            for (Lot lot : GTS.getInstance().getSql().getAllLots()) {
                if(!GTS.getInstance().getSql().isExpired(lot.getLotID())) {
                    if (GTS.getInstance().getSql().getEnd(lot.getLotID()).after(Date.from(Instant.now()))) continue;
                    this.endMarket(lot);
                }
            }
        }).submit(GTS.getInstance());
    }

    private void endMarket(Lot lot) {
        PokemonItem item = lot.getItem();
        Optional<Player> player = Sponge.getServer().getPlayer(item.getOwner());
        if (!player.isPresent()) {
            GTS.getInstance().getSql().updateEntry(lot.getLotID());
        } else {
            player.get().sendMessage(MessageConfig.getMessage("GTS.Remove.Expired", lot.getItem().getName()));
            Optional<PlayerStorage> storage = PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID((MinecraftServer)Sponge.getServer(), Sponge.getServer().getPlayer(item.getOwner()).get().getUniqueId());
            if(storage.isPresent()) {
                storage.get().addToParty(item.getPokemon(lot));
                GTS.getInstance().getSql().deleteLot(lot.getLotID());
            } else {
                GTS.getInstance().getLogger().error("An error occurred on ending " + Sponge.getServer().getPlayer(lot.getOwner()).get().getName() + "'s listing");
            }
        }
    }
}
