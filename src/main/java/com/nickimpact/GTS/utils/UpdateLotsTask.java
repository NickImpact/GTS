package com.nickimpact.GTS.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.GTS.GTSInfo;
import com.nickimpact.GTS.configuration.MessageConfig;
import com.nickimpact.GTS.GTS;
import com.nickimpact.GTS.logging.Log;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Created by Nick on 12/15/2016.
 */
public class UpdateLotsTask {

    public static void saveTask(){
        Sponge.getScheduler().createTaskBuilder().execute(() -> {
            GTS.getInstance().getSql().updateLots(LotUtils.getSqlCmds());
            LotUtils.getSqlCmds().clear();
        })
        .interval(60, TimeUnit.SECONDS)
        .async()
        .submit(GTS.getInstance());
    }

    public static void setupUpdateTask(){
        Sponge.getScheduler().createTaskBuilder().interval(1, TimeUnit.SECONDS).execute(() -> {
            final List<LotCache> listings = Lists.newArrayList(GTS.getInstance().getLots());
            for (LotCache lot : listings) {
                if(lot.getLot().canExpire() || (!lot.getLot().canExpire() && lot.getLot().getPokeWanted() == null)) {
                    if (!lot.isExpired()) {
                        if (lot.getDate().after(Date.from(Instant.now()))) continue;

                        if (lot.getLot().isAuction())
                            if (lot.getLot().getHighBidder() == null)
                                endMarket(lot);
                            else {
                                awardPokemon(Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(lot.getLot().getOwner()).orElse(null), lot.getLot());
                            }
                        else
                            endMarket(lot);
                    }
                }
            }
        }).submit(GTS.getInstance());
    }

    private static void endMarket(LotCache lot) {
        PokemonItem item = lot.getLot().getItem();
        Optional<Player> player = Sponge.getServer().getPlayer(item.getOwner());
        if (!player.isPresent()) {
            lot.setIsExpired(true);
            LotUtils.updateLot(lot.getLot().getLotID());
        } else {
            HashMap<String, Optional<Object>> textOptions = Maps.newHashMap();
            textOptions.put("pokemon", Optional.of(lot.getLot().getItem().getName()));

            for(Text text : MessageConfig.getMessages("Generic.Remove.Expired", textOptions))
                player.get().sendMessage(text);

            Log log = LotUtils.forgeLog(Sponge.getServer().getPlayer(lot.getLot().getOwner()).get(), "Expires", textOptions);
            GTS.getInstance().getSql().appendLog(log);
            Optional<PlayerStorage> storage = PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID((MinecraftServer)Sponge.getServer(), lot.getLot().getOwner());
            if(storage.isPresent()) {
                storage.get().addToParty(item.getPokemon(lot.getLot()));

                for (int i = 0; i < GTS.getInstance().getLots().size(); i++){
                    if (GTS.getInstance().getLots().get(i).getLot().getLotID() == lot.getLot().getLotID()) {
                        GTS.getInstance().getLots().remove(i);
                        break;
                    }
                }
                LotUtils.deleteLot(lot.getLot().getLotID());
            } else {
                GTS.getInstance().getConsole().sendMessage(Text.of(
                        GTSInfo.ERROR_PREFIX, TextColors.DARK_RED, "An error occurred on ending ",
                        TextColors.YELLOW, Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(lot.getLot().getOwner()).get().getName() + "'s listing")
                );
            }
        }
    }

    private static void awardPokemon(User user, Lot lot){
        if(user != null) {
            PokemonItem item = lot.getItem();
            Optional<PlayerStorage> storage = PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID((MinecraftServer)Sponge.getServer(), lot.getHighBidder());
            if(storage.isPresent()){
                HashMap<String, Optional<Object>> textOptions = Maps.newHashMap();
                textOptions.put("player", Optional.of(user.getName()));
                textOptions.put("pokemon", Optional.of(item.getName()));
                textOptions.put("curr_symbol", Optional.of(GTS.getInstance().getEconomy().getDefaultCurrency().getSymbol().toPlain()));
                textOptions.put("price", Optional.of(lot.getStPrice()));

                BigDecimal price = new BigDecimal(lot.getStPrice());
                Optional<UniqueAccount> account = GTS.getInstance().getEconomy().getOrCreateAccount(user.getUniqueId());
                if(account.isPresent()) {
                    UniqueAccount acc = account.get();
                    if (acc.getBalance(GTS.getInstance().getEconomy().getDefaultCurrency()).intValue() < price.intValue()) {
                        for (Text text : MessageConfig.getMessages("Generic.Purchase.Error.Not Enough", null))
                            user.getPlayer().ifPresent(p -> {
                                p.sendMessage(text);
                            });
                        return;
                    }

                    // Owner Log Info
                    HashMap<String, Optional<Object>> tOptsOwner = Maps.newHashMap();
                    tOptsOwner.put("player", Optional.of(user.getName()));
                    tOptsOwner.put("price", Optional.of(price));
                    textOptions.put("curr_symbol", Optional.of(GTS.getInstance().getEconomy().getDefaultCurrency().getSymbol().toPlain()));
                    tOptsOwner.put("pokemon", Optional.of(item.getName()));
                    Log log = LotUtils.forgeLog(user, "Auction-Seller", tOptsOwner);
                    GTS.getInstance().getSql().appendLog(log);

                    // Winner Log Info
                    HashMap<String, Optional<Object>> tOptsWinner = Maps.newHashMap();
                    tOptsWinner.putAll(textOptions);
                    tOptsWinner.put("player", Optional.of(Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(lot.getOwner()).get().getName()));
                    Log log2 = LotUtils.forgeLog(Sponge.getServer().getPlayer(lot.getOwner()).get(), "Auction-Winner", tOptsOwner);
                    GTS.getInstance().getSql().appendLog(log2);

                    acc.withdraw(GTS.getInstance().getEconomy().getDefaultCurrency(), price, Cause.source(GTS.getInstance()).build());

                    for (Text text : MessageConfig.getMessages("Auctions.Award", textOptions))
                        for (Player p : Sponge.getServer().getOnlinePlayers()) {
                            if (!GTS.getInstance().getIgnoreList().contains(p.getUniqueId()))
                                p.sendMessage(text);
                        }
                    storage.get().addToParty(item.getPokemon(lot));
                    GTS.getInstance().getSql().deleteLot(lot.getLotID());

                    Optional<UniqueAccount> ownerAccount = GTS.getInstance().getEconomy().getOrCreateAccount(lot.getOwner());
                    if(ownerAccount.isPresent()){
                        UniqueAccount owner = ownerAccount.get();
                        owner.deposit(GTS.getInstance().getEconomy().getDefaultCurrency(), price, Cause.of(NamedCause.source(GTS.getInstance())));
                    } else {
                        GTS.getInstance().getConsole().sendMessage(Text.of(
                                GTSInfo.ERROR_PREFIX, TextColors.DARK_RED, "Player '", TextColors.YELLOW,
                                Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(lot.getOwner()).get().getName()
                                        + "' listing was unable to receive $" + price.intValue() + " from the GTS")
                        );
                    }
                }
            } else {
                GTS.getInstance().getConsole().sendMessage(Text.of(
                        GTSInfo.ERROR_PREFIX, TextColors.DARK_RED, "An error occurred when trying to award a pokemon to ", TextColors.YELLOW, user.getName())
                );
            }
        } else {
            GTS.getInstance().getConsole().sendMessage(Text.of(
                    GTSInfo.ERROR_PREFIX, TextColors.DARK_RED, "A user could not be found (Auction related)")
            );
        }
    }
}
