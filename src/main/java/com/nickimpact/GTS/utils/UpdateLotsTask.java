package com.nickimpact.GTS.utils;

import com.google.common.collect.Maps;
import com.nickimpact.GTS.configuration.MessageConfig;
import com.nickimpact.GTS.GTS;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
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

                    if(lot.isAuction())
                        if(lot.getHighBidder() == null)
                            this.endMarket(lot);
                        else {
                            this.awardPokemon(Sponge.getServer().getPlayer(lot.getHighBidder()).orElse(null), lot);
                        }
                    else
                        this.endMarket(lot);
                }
            }
        }).submit(GTS.getInstance());
    }

    private void endMarket(Lot lot) {
        PokemonItem item = lot.getItem();
        Optional<Player> player = Sponge.getServer().getPlayer(item.getOwner());
        if (!player.isPresent()) {
            GTS.getInstance().getSql().setExpired(lot.getLotID());
        } else {
            HashMap<String, Optional<Object>> textOptions = Maps.newHashMap();
            textOptions.put("pokemon", Optional.of(lot.getItem().getName()));

            player.get().sendMessage(MessageConfig.getMessage("Generic.Remove.Expired", textOptions));
            Optional<PlayerStorage> storage = PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID((MinecraftServer)Sponge.getServer(), Sponge.getServer().getPlayer(item.getOwner()).get().getUniqueId());
            if(storage.isPresent()) {
                storage.get().addToParty(item.getPokemon(lot, player.get()));
                GTS.getInstance().getSql().deleteLot(lot.getLotID());
            } else {
                GTS.getInstance().getLogger().error("An error occurred on ending " + Sponge.getServer().getPlayer(lot.getOwner()).get().getName() + "'s listing");
            }
        }
    }

    private void awardPokemon(Player player, Lot lot){
        if(player != null) {
            PokemonItem item = lot.getItem();
            Optional<PlayerStorage> storage = PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID((MinecraftServer)Sponge.getServer(), lot.getHighBidder());
            if(storage.isPresent()){
                HashMap<String, Optional<Object>> textOptions = Maps.newHashMap();
                textOptions.put("player", Optional.of(player.getName()));
                textOptions.put("pokemon", Optional.of(item.getName()));
                textOptions.put("price", Optional.of(lot.getStPrice()));

                BigDecimal price = new BigDecimal(lot.getStPrice());
                Optional<UniqueAccount> account = GTS.getInstance().getEconomy().getOrCreateAccount(player.getUniqueId());
                if(account.isPresent()) {
                    UniqueAccount acc = account.get();
                    if (acc.getBalance(GTS.getInstance().getEconomy().getDefaultCurrency()).intValue() < price.intValue()) {
                        for (Text text : MessageConfig.getMessages("Generic.Purchase.Error.Not Enough", null))
                            player.sendMessage(text);
                        return;
                    }
                    acc.withdraw(GTS.getInstance().getEconomy().getDefaultCurrency(), price, Cause.source(GTS.getInstance()).build());

                    for (Text text : MessageConfig.getMessages("Auctions.Award", textOptions))
                        Sponge.getServer().getBroadcastChannel().send(text);
                    storage.get().addToParty(item.getPokemon(lot, player));
                    GTS.getInstance().getSql().deleteLot(lot.getLotID());

                    Optional<UniqueAccount> ownerAccount = GTS.getInstance().getEconomy().getOrCreateAccount(lot.getOwner());
                    if(ownerAccount.isPresent()){
                        UniqueAccount owner = ownerAccount.get();
                        owner.deposit(GTS.getInstance().getEconomy().getDefaultCurrency(), price, Cause.of(NamedCause.source(GTS.getInstance())));
                    } else {
                        GTS.getInstance().getLogger().error("Player '" + Sponge.getServer().getPlayer(lot.getOwner()).get().getName() + "' was unable to receive $" + price.intValue() + " from the GTS");
                    }
                }
            } else {
                GTS.getInstance().getLogger().error("An error occurred when trying to award a pokemon to " + player.getName());
            }
        } else {
            GTS.getInstance().getLogger().error("An error occurred on finding the player with uuid " + lot.getHighBidder());
        }

    }
}
