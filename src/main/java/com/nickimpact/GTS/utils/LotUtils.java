package com.nickimpact.GTS.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.nickimpact.GTS.configuration.MessageConfig;
import com.nickimpact.GTS.GTS;
import com.nickimpact.GTS.logging.Log;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
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
import java.sql.Date;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.*;

/**
 * Created by Nick on 12/15/2016.
 */
public class LotUtils {

    private static List<String> sqlCmds = Lists.newArrayList();
    private static int placement = 1;
    private static int logPlacement = 1;

    public static List<String> getSqlCmds(){
        return sqlCmds;
    }

    public static Lot lotFromJson(String s) throws JsonSyntaxException {
        return new Gson().fromJson(s, Lot.class);
    }

    public static void addPokemonStatic(Player player, int slot, String note, int price, boolean expires, long time) {
        addPokemonToMarket(1, player, slot, note, price, expires, time);
    }

    public static void addPokemonAuc(Player player, int slot, String note, int startPrice, int increment, long time) {
        addPokemonToMarket(2, player, slot, note, startPrice, increment, time);
    }

    public static void addPokemon4Pokemon(Player player, int slot, String note, PokeRequest pokemon, boolean expires, long time){
        addPokemonToMarket(3, player, slot, note, pokemon, expires, time);
    }

    private static void addPokemonToMarket(int mode, Player player, int slot, String note, Object... options) {
        HashMap<String, Optional<Object>> textOptions = Maps.newHashMap();

        if (hasMax(player.getUniqueId())) {
            textOptions.put("max_pokemon", Optional.of(GTS.getInstance().getConfig().getMaxPokemon()));
            for (Text text : MessageConfig.getMessages("Generic.Addition.Error.Exceed Max", textOptions))
                player.sendMessage(text);

            return;
        }

        Optional<PlayerStorage> storage = getStorage(player);
        if (!storage.isPresent()) {
            return;
        }

        NBTTagCompound nbt = getNbt(player, slot, storage.get());
        if (nbt == null) {
            return;
        }

        EntityPixelmon pokemon = getPokemon(player, nbt);
        if (pokemon == null) {
            return;
        }

        // Do checking
        if (mode == 1) {
            int price = (Integer) options[0];
            boolean expires = (Boolean) options[1];
            long time = (Long) options[2];

            if (!checkPrice(player, pokemon, price)) {
                return;
            }

            if (!handleTax(player, pokemon, price)) {
                return;
            }

            PokemonItem pokeItem = new PokemonItem(pokemon, player.getName(), price);

            int placement = getPlacement();

            Lot lot = new Lot(placement, player.getUniqueId(), nbt.toString(), pokeItem, price, expires, note);

            Gson gson = new Gson();
            String json = gson.toJson(lot);

            try {
                gson.fromJson(json, Lot .class);
            }catch(JsonSyntaxException e){
                player.sendMessage(Text.of(TextColors.RED, "It appears your pokemon's info encountered an error, and has been prevented from being added to GTS..."));
            }

            addLot(player.getUniqueId(), gson.toJson(lot), false, time);
            GTS.getInstance().getLots().add(new LotCache(lot, false, Date.from(Instant.now().plusSeconds(time))));

            textOptions.put("player", Optional.of(player.getName()));
            textOptions.put("pokemon", Optional.of(pokemon.getName()));
            textOptions.put("lot_type", Optional.of("static"));
            textOptions.put("curr_symbol", Optional.of(GTS.getInstance().getEconomy().getDefaultCurrency().getSymbol().toPlain()));
            textOptions.put("price", Optional.of(price));
            textOptions.put("expires", expires ? Optional.of("Never") : Optional.of(getTime(time * 1000)));
            textOptions.putAll(getInfo(pokemon));

            if (!pokemon.isEgg) {
                for (Text text : MessageConfig.getMessages("Generic.Addition.Broadcast.Normal", textOptions)) {
                    for (Player p : Sponge.getServer().getOnlinePlayers()) {
                        if (!GTS.getInstance().getIgnoreList().contains(p.getUniqueId()))
                            p.sendMessage(text);
                    }
                }
            } else {
                for (Text text : MessageConfig.getMessages("Generic.Addition.Broadcast.Egg.Normal", textOptions)) {
                    for (Player p : Sponge.getServer().getOnlinePlayers()) {
                        if (!GTS.getInstance().getIgnoreList().contains(p.getUniqueId()))
                            p.sendMessage(text);
                    }
                }
            }

            Log log = forgeLog(player, "Addition", textOptions);
            addLog(player.getUniqueId(), log);
        } else if (mode == 2) {
            int stPrice = (Integer) options[0];
            int increment = (Integer) options[1];
            long time = (Long) options[2];

            if (!checkPrice(player, pokemon, stPrice)) {
                return;
            }

            if (!handleTax(player, pokemon, stPrice)) {
                return;
            }

            PokemonItem pokeItem = new PokemonItem(pokemon, player.getName(), stPrice, increment);

            int placement = getPlacement();

            Lot lot = new Lot(placement, player.getUniqueId(), nbt.toString(), pokeItem, stPrice, true, true, null, stPrice, increment, note);

            Gson gson = new Gson();
            String json = gson.toJson(lot);

            try {
                gson.fromJson(json, Lot .class);
            }catch(JsonSyntaxException e){
                player.sendMessage(Text.of(TextColors.RED, "It appears your pokemon's info encountered an error, and has been prevented from being added to GTS..."));
            }

            addLot(player.getUniqueId(), new Gson().toJson(lot), false, time);
            GTS.getInstance().getLots().add(new LotCache(lot, false, Date.from(Instant.now().plusSeconds(time))));

            textOptions.put("player", Optional.of(player.getName()));
            textOptions.put("pokemon", Optional.of(pokemon.getName()));
            textOptions.put("lot_type", Optional.of("Auction"));
            textOptions.put("curr_symbol", Optional.of(GTS.getInstance().getEconomy().getDefaultCurrency().getSymbol().toPlain()));
            textOptions.put("start_price", Optional.of(stPrice));
            textOptions.put("increment", Optional.of(increment));
            textOptions.put("expires", Optional.of(getTime(time * 1000)));
            textOptions.putAll(getInfo(pokemon));

            if (!pokemon.isEgg) {
                for (Text text : MessageConfig.getMessages("Auctions.Broadcast.Pokemon", textOptions))
                    for (Player p : Sponge.getServer().getOnlinePlayers()) {
                        if (!GTS.getInstance().getIgnoreList().contains(p.getUniqueId()))
                            p.sendMessage(text);
                    }
            } else {
                for (Text text : MessageConfig.getMessages("Auctions.Broadcast.Egg.Pokemon", textOptions))
                    for (Player p : Sponge.getServer().getOnlinePlayers()) {
                        if (!GTS.getInstance().getIgnoreList().contains(p.getUniqueId()))
                            p.sendMessage(text);
                    }
            }

            textOptions.put("pokemon", Optional.of(pokemon.getName()));
            Log log = forgeLog(player, "Addition", textOptions);
            addLog(player.getUniqueId(), log);
        } else {
            PokeRequest poke = (PokeRequest)options[0];
            boolean expires = (Boolean) options[1];
            long time = (Long) options[2];

            if (!handleTax(player, pokemon, GTS.getInstance().getConfig().pokeTax())) {
                return;
            }

            PokemonItem pokeItem = new PokemonItem(pokemon, player.getName());

            Gson gson = new Gson();
            int placement = getPlacement();
            Lot lot = new Lot(placement, player.getUniqueId(), nbt.toString(), pokeItem, true, gson.toJson(poke), note);

            String json = gson.toJson(lot);

            try {
                gson.fromJson(json, Lot.class);
            }catch(JsonSyntaxException e){
                player.sendMessage(Text.of(TextColors.RED, "It appears your pokemon's info encountered an error, and has been prevented from being added to GTS..."));
            }

            addLot(player.getUniqueId(), new Gson().toJson(lot), false, time);
            GTS.getInstance().getLots().add(new LotCache(lot, false, Date.from(Instant.now().plusSeconds(time))));

            textOptions.put("player", Optional.of(player.getName()));
            textOptions.put("pokemon", Optional.of(pokemon.getName()));
            textOptions.put("lot_type", Optional.of("pokemon"));
            textOptions.put("poke_looked_for", Optional.of(poke.getPokemon()));
            textOptions.put("expires", expires ? Optional.of("Never") : Optional.of(getTime(time * 1000)));
            textOptions.putAll(getInfo(pokemon));

            if (!pokemon.isEgg) {
                for (Text text : MessageConfig.getMessages("Generic.Addition.Broadcast.Pokemon", textOptions))
                    for (Player p : Sponge.getServer().getOnlinePlayers()) {
                        if (!GTS.getInstance().getIgnoreList().contains(p.getUniqueId()))
                            p.sendMessage(text);
                    }
            } else {
                for (Text text : MessageConfig.getMessages("Generic.Addition.Broadcast.Egg.Pokemon", textOptions))
                    for (Player p : Sponge.getServer().getOnlinePlayers()) {
                        if (!GTS.getInstance().getIgnoreList().contains(p.getUniqueId()))
                            p.sendMessage(text);
                    }
            }

            textOptions.put("pokemon", Optional.of(pokemon.getName()));
            Log log = forgeLog(player, "Addition", textOptions);
            addLog(player.getUniqueId(), log);
        }

        ++placement;
        // Remove the pokemon from the client
        storage.get().recallAllPokemon();
        storage.get().removeFromPartyPlayer(slot);
        storage.get().sendUpdatedList();

        // Print Success Message
        textOptions.put("pokemon", Optional.of(pokemon.isEgg ? "Mystery Egg" : pokemon.getName()));
        for (Text text : MessageConfig.getMessages("Generic.Addition.Success", textOptions))
            player.sendMessage(text);
    }

    private static boolean hasMax(UUID uuid) {
        int total = 0;
        for(LotCache lot : GTS.getInstance().getLots()){
            if(lot.getLot().getOwner().equals(uuid)){
                ++total;
            }
        }

        return total >= GTS.getInstance().getConfig().getMaxPokemon();
    }

    public static void buyLot(Player p, LotCache lot) {
        HashMap<String, Optional<Object>> textOptions = Maps.newHashMap();
        if (lot == null) {
            for(Text text : MessageConfig.getMessages("Generic.Purchase.Error.Already Sold", null))
                p.sendMessage(text);
            return;
        }
        if (!lot.isExpired()) {
            BigDecimal price = new BigDecimal(lot.getLot().getPrice());
            try {
                Optional<UniqueAccount> account = GTS.getInstance().getEconomy().getOrCreateAccount(p.getUniqueId());
                if(account.isPresent()) {
                    UniqueAccount acc = account.get();
                    if(acc.getBalance(GTS.getInstance().getEconomy().getDefaultCurrency()).intValue() < price.intValue()){
                        textOptions.put("money_diff", Optional.of(GTS.getInstance().getEconomy().getDefaultCurrency().getSymbol().toString() + (price.intValue() - acc.getBalance(GTS.getInstance().getEconomy().getDefaultCurrency()).intValue())));

                        for(Text text : MessageConfig.getMessages("Generic.Purchase.Error.Not Enough", null))
                            p.sendMessage(text);
                        return;
                    }
                    acc.withdraw(GTS.getInstance().getEconomy().getDefaultCurrency(), price, Cause.source(GTS.getInstance()).build());

                    GTS.getInstance().getLots().remove(lot);
                    LotUtils.deleteLot(lot.getLot().getLotID());

                    EntityPixelmon pokemon = lot.getLot().getItem().getPokemon(lot.getLot());
                    Optional<PlayerStorage> storage = PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID((MinecraftServer) Sponge.getServer(), p.getUniqueId());
                    if(storage.isPresent()){
                        storage.get().addToParty(pokemon);
                    } else {
                        GTS.getInstance().getLogger().error("Player " + p.getName() + " was unable to receive a " + lot.getLot().getItem().getName() + " from GTS");
                    }

                    textOptions.put("pokemon", lot.getLot().getItem().getPokemon(lot.getLot()).isEgg ? Optional.of("Mystery Egg") : Optional.of(lot.getLot().getItem().getName()));
                    textOptions.put("curr_symbol", Optional.of(GTS.getInstance().getEconomy().getDefaultCurrency().getSymbol().toPlain()));
                    textOptions.put("price", Optional.of(price));
                    textOptions.put("seller", Optional.of(Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(lot.getLot().getOwner()).get().getName()));
                    for(Text text : MessageConfig.getMessages("Generic.Purchase.Success.Buyer", textOptions))
                        p.sendMessage(text);

                    textOptions.put("pokemon", Optional.of(lot.getLot().getItem().getName()));
                    Log log = forgeLog(p, "Purchase-Recipient", textOptions);
                    LotUtils.addLog(p.getUniqueId(), log);

                    textOptions.clear();

                    Optional<UniqueAccount> ownerAccount = GTS.getInstance().getEconomy().getOrCreateAccount(lot.getLot().getOwner());
                    if(ownerAccount.isPresent()){
                        UniqueAccount owner = ownerAccount.get();
                        owner.deposit(GTS.getInstance().getEconomy().getDefaultCurrency(), price, Cause.of(NamedCause.source(GTS.getInstance())));
                    } else {
                        GTS.getInstance().getLogger().error("Player '" + Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(lot.getLot().getOwner()).get().getName() + "' was unable to receive $" + price.intValue() + " from the GTS");
                    }

                    if (Sponge.getServer().getPlayer(lot.getLot().getOwner()).isPresent()) {
                        textOptions.put("pokemon", Optional.of(lot.getLot().getItem().getName()));
                        textOptions.put("curr_symbol", Optional.of(GTS.getInstance().getEconomy().getDefaultCurrency().getSymbol().toPlain()));
                        textOptions.put("price", Optional.of(price));
                        textOptions.put("buyer", Optional.of(p.getName()));

                        for(Text text : MessageConfig.getMessages("Generic.Purchase.Success.Seller", textOptions))
                            Sponge.getServer().getPlayer(lot.getLot().getOwner()).get().sendMessage(text);

                        Log log2 = forgeLog(p, "Purchase-Owner", textOptions);
                        LotUtils.addLog(lot.getLot().getOwner(), log2);
                    }
                } else {
                    GTS.getInstance().getLogger().error(Text.of(TextColors.RED, "Account for UUID (" + p.getUniqueId() + ") was not found").toPlain());
                }
            }
            catch (Exception exc) {
                exc.printStackTrace();
            }
        } else {
            for(Text text : MessageConfig.getMessages("Generic.Purchase.Error.Expired", null))
                p.sendMessage(text);
        }
    }

    public static void bid(Player player, Lot lot){
        HashMap<String, Optional<Object>> textOptions = Maps.newHashMap();

        if(lot.getHighBidder() != null){
            Optional<Player> p = Sponge.getServer().getPlayer(lot.getHighBidder());
            if(p.isPresent()){
                if(p.get().isOnline()){
                    textOptions.put("player", Optional.of(player.getName()));
                    textOptions.put("pokemon", Optional.of(lot.getItem().getName()));

                    for(Text text : MessageConfig.getMessages("Auctions.Outbid", textOptions))
                        p.get().sendMessage(text);
                }
            }
        }

        if(player.getUniqueId().equals(lot.getHighBidder())){
            textOptions.put("pokemon", Optional.of(lot.getItem().getName()));
            for(Text text : MessageConfig.getMessages("Auctions.Current Bidder", textOptions))
                player.sendMessage(text);
            return;
        }

        BigDecimal price = new BigDecimal(lot.getStPrice());
        Optional<UniqueAccount> account = GTS.getInstance().getEconomy().getOrCreateAccount(player.getUniqueId());
        if(account.isPresent()) {
            UniqueAccount acc = account.get();
            if (acc.getBalance(GTS.getInstance().getEconomy().getDefaultCurrency()).intValue() < price.intValue()) {
                textOptions.put("money_diff", Optional.of(price.intValue() - acc.getBalance(GTS.getInstance().getEconomy().getDefaultCurrency()).intValue()));
                textOptions.put("curr_symbol", Optional.of(GTS.getInstance().getEconomy().getDefaultCurrency().getSymbol()));

                for (Text text : MessageConfig.getMessages("Generic.Purchase.Error.Not Enough", textOptions))
                    player.sendMessage(text);
                return;
            }
        }

        textOptions.put("pokemon", Optional.of(lot.getItem().getName()));
        textOptions.put("curr_symbol", Optional.of(
                GTS.getInstance().getEconomy().getDefaultCurrency().getSymbol().toPlain()));
        textOptions.put("price", Optional.of(lot.getStPrice() + lot.getIncrement()));

        for(Text text : MessageConfig.getMessages("Auctions.Placed Bid", textOptions)){
            player.sendMessage(text);
        }

        for(Player p : Sponge.getServer().getOnlinePlayers())
            if(!GTS.getInstance().getIgnoreList().contains(p.getUniqueId()) && !p.getUniqueId().equals(player.getUniqueId())) {
                if (!p.getUniqueId().equals(lot.getHighBidder())) {
                    textOptions.put("player", Optional.of(player.getName()));
                    textOptions.put("seller", Optional.of(Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(lot.getOwner()).get().getName()));

                    for (Text text : MessageConfig.getMessages("Auctions.Announce Bid", textOptions))
                        p.sendMessage(text);
                }
            }

        lot.setStPrice(lot.getStPrice() + lot.getIncrement());
        lot.setHighBidder(player.getUniqueId());
        LotUtils.updateLot(lot);
    }

    public static void trade(Player player, LotCache lot, int slot){
        Optional<PlayerStorage> storage = PixelmonStorage.pokeBallManager.getPlayerStorage((EntityPlayerMP)player);
        Optional<PlayerStorage> owner = PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID((MinecraftServer)Sponge.getServer(), lot.getLot().getOwner());
        if(storage.isPresent() && owner.isPresent()) {
            EntityPixelmon pokemon = (EntityPixelmon)PixelmonEntityList.createEntityFromNBT(storage.get().partyPokemon[slot], (World)player.getWorld());

            storage.get().removeFromPartyPlayer(slot);
            storage.get().addToParty(lot.getLot().getItem().getPokemon(lot.getLot()));
            storage.get().sendUpdatedList();

            HashMap<String, Optional<Object>> variables = Maps.newHashMap();
            variables.put("pokemon", Optional.of(lot.getLot().getItem().getName()));
            variables.put("player", Optional.of(Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(lot.getLot().getOwner()).get().getName()));
            variables.put("poke_looked_for", Optional.of(pokemon.getName()));

            for(Text text : MessageConfig.getMessages("Generic.Trade.Recipient.Receive-Poke", variables)){
                player.sendMessage(text);
            }

            owner.get().addToParty(pokemon);
            if(!owner.get().isOffline()){
                owner.get().sendUpdatedList();

                variables.put("pokemon", Optional.of(pokemon.getName()));
                variables.put("player", Optional.of(player.getName()));
                variables.put("poke_looked_for", Optional.of(lot.getLot().getItem().getName()));

                for(Text text : MessageConfig.getMessages("Generic.Trade.Owner.Receive-Poke", variables)){
                    Sponge.getServer().getPlayer(lot.getLot().getOwner()).get().sendMessage(text);
                }
            }

            GTS.getInstance().getLots().remove(lot);
            LotUtils.deleteLot(lot.getLot().getLotID());
        }
    }

    public static boolean isValidTrade(PokeRequest pr, EntityPixelmon pokemon){
        if (!pokemon.getName().equalsIgnoreCase(pr.getPokemon())) return false;

        int[] evs = new int[]{pokemon.stats.EVs.HP, pokemon.stats.EVs.Attack, pokemon.stats.EVs.Defence,
                pokemon.stats.EVs.SpecialAttack, pokemon.stats.EVs.SpecialDefence,
                pokemon.stats.EVs.Speed};
        int[] ivs = new int[]{pokemon.stats.IVs.HP, pokemon.stats.IVs.Attack, pokemon.stats.IVs.Defence,
                pokemon.stats.IVs.SpAtt, pokemon.stats.IVs.SpDef,
                pokemon.stats.IVs.Speed};

        for(int i = 0; i < 6; i++){
            if(evs[i] < pr.getEvs()[i]) return false;
        }

        for(int i = 0; i < 6; i++){
            if(ivs[i] < pr.getIvs()[i]) return false;
        }

        return pokemon.getLvl().getLevel() >= pr.getLevel() &&
                pokemon.getIsShiny() == pr.isShiny() &&
                (pokemon.gender.name().equals(pr.getGender()) || pr.getGender().equals("N/A")) &&
                (pokemon.getNature().name().equals(pr.getNature()) || pr.getNature().equals("N/A")) &&
                (pokemon.getAbility().getName().equals(pr.getAbility()) || pr.getAbility().equals("N/A")) &&
                (pokemon.getGrowth().name().equals(pr.getGrowth()) || pr.getGrowth().equals("N/A")) &&
                (pokemon.getForm() == pr.getForm() || pokemon.getForm() == -1 || pr.getForm() == -1) &&
                (pokemon.caughtBall.name().equals(pr.getPokeball()) || pr.getPokeball().equals("N/A"));
    }

    public static void givePlayerPokemon(UUID uuid, Lot lot){
        Optional<PlayerStorage> storage = PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID((MinecraftServer) Sponge.getServer(), uuid);
        if(storage.isPresent()){
            storage.get().addToParty(lot.getItem().getPokemon(lot));
        } else {
            GTS.getInstance().getLogger().error("UUID (" + uuid + ") was unable to receive a " + lot.getItem().getName() + " from GTS");
        }
    }

    private static Optional<PlayerStorage> getStorage(Player player){
        Optional<PlayerStorage> storage = PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID((MinecraftServer) Sponge.getServer(), player.getUniqueId());
        if(storage.isPresent())
            if (storage.get().count() <= 1) {
                for(Text text : MessageConfig.getMessages("Generic.Addition.Error.Last Pokemon", null))
                    player.sendMessage(text);
                return Optional.empty();
            }

        return storage;
    }

    private static NBTTagCompound getNbt(Player player, int slot, PlayerStorage ps){
        NBTTagCompound[] party = ps.getList();
        NBTTagCompound nbt = party[slot];
        if (nbt == null) {
            HashMap<String, Optional<Object>> textOptions = Maps.newHashMap();
            textOptions.put("slot", Optional.of(slot + 1));

            for(Text text : MessageConfig.getMessages("Generic.Addition.Error.Empty Slot", textOptions))
                player.sendMessage(text);
            return null;
        }

        return nbt;
    }

    private static EntityPixelmon getPokemon(Player player, NBTTagCompound nbt){
        EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt, (World)player.getWorld());
        for(String s : GTS.getInstance().getConfig().getBlocked()){
            if(pokemon.getName().equalsIgnoreCase(s)){
                HashMap<String, Optional<Object>> textOptions = Maps.newHashMap();
                textOptions.put("pokemon", Optional.of(pokemon.getName()));

                for(Text text : MessageConfig.getMessages("Generic.Addition.Error.Invalid", textOptions))
                    player.sendMessage(text);
                return null;
            }
        }

        return pokemon;
    }

    private static boolean checkPrice(Player player, EntityPixelmon pokemon, int price) {
        int ivPrice = 0;
        for(int iv : pokemon.stats.IVs.getArray()){
            if(iv >= GTS.getInstance().getConfig().getMinIV()){
                ivPrice += GTS.getInstance().getConfig().getMinIVPrice();
            }
        }

        int totalPrice;

        if(pokemon.getAbilitySlot().equals(2)){
            if(pokemon.getIsShiny() && EnumPokemon.legendaries.contains(pokemon.getName())){
                totalPrice = GTS.getInstance().getConfig().getMinHAPrice() + ivPrice +
                        GTS.getInstance().getConfig().getMinShinyPrice() + GTS.getInstance().getConfig().getMinLegendPrice();

                if(price >= totalPrice){
                    return true;
                }
            } else if (pokemon.getIsShiny()) {
                totalPrice = GTS.getInstance().getConfig().getMinHAPrice() + ivPrice + GTS.getInstance().getConfig().getMinShinyPrice();

                if(price >= totalPrice){
                    return true;
                }
            } else {
                totalPrice = GTS.getInstance().getConfig().getMinHAPrice() + ivPrice + GTS.getInstance().getConfig().getMinLegendPrice();

                if(price >= totalPrice){
                    return true;
                }
            }

            if(price >= GTS.getInstance().getConfig().getMinHAPrice() + ivPrice){
                return true;
            }
        }
        else if(EnumPokemon.legendaries.contains(pokemon.getName())){
            if(pokemon.getIsShiny()){
                totalPrice = GTS.getInstance().getConfig().getMinLegendPrice() + ivPrice + GTS.getInstance().getConfig().getMinShinyPrice();
                if(price >= totalPrice){
                    return true;
                }
            }

            totalPrice = GTS.getInstance().getConfig().getMinLegendPrice() + ivPrice;

            if (price >= totalPrice)
                return true;
        } else if(pokemon.getIsShiny()){
            totalPrice = GTS.getInstance().getConfig().getMinShinyPrice() + ivPrice;
            if(price >= totalPrice)
                return true;
        } else {
            totalPrice = GTS.getInstance().getConfig().getMinPrice() + ivPrice;
            if(price >= totalPrice)
                return true;
        }

        HashMap<String, Optional<Object>> textOptions = Maps.newHashMap();
        textOptions.put("curr_symbol", Optional.of(GTS.getInstance().getEconomy().getDefaultCurrency().getSymbol().toPlain()));
        textOptions.put("price", Optional.of(totalPrice));

        for(Text text : MessageConfig.getMessages("Pricing.MinPrice.Error.Not Enough", textOptions))
            player.sendMessage(text);
        return false;
    }

    private static boolean handleTax(Player player, EntityPixelmon pokemon, int price){

        double t;
        if(pokemon.getIsShiny() && EnumPokemon.legendaries.contains(pokemon.getName()) && GTS.getInstance().getConfig().isStackTaxEnabled()){
            t = GTS.getInstance().getConfig().getShinyTax() + GTS.getInstance().getConfig().getLegendTax();
        } else if(EnumPokemon.legendaries.contains(pokemon.getName())){
            t = GTS.getInstance().getConfig().getLegendTax();
        } else if(pokemon.getIsShiny()){
            t = GTS.getInstance().getConfig().getShinyTax();
        } else {
            t = GTS.getInstance().getConfig().getTaxRate();
        }

        BigDecimal tax = new BigDecimal((double)price * t);
        if(GTS.getInstance().getConfig().isTaxEnabled()) {
            Optional<UniqueAccount> account = GTS.getInstance().getEconomy().getOrCreateAccount(player.getUniqueId());
            if(account.isPresent()) {
                HashMap<String, Optional<Object>> textOptions = Maps.newHashMap();
                textOptions.put("curr_symbol", Optional.of(GTS.getInstance().getEconomy().getDefaultCurrency().getSymbol().toPlain()));
                textOptions.put("tax", Optional.of(tax));

                UniqueAccount acc = account.get();
                if(acc.getBalance(GTS.getInstance().getEconomy().getDefaultCurrency()).compareTo(tax) < 0) {
                    for(Text text : MessageConfig.getMessages("Pricing.Tax.Error.Not Enough", textOptions))
                        player.sendMessage(text);
                    return false;
                }
                acc.withdraw(GTS.getInstance().getEconomy().getDefaultCurrency(), tax, Cause.source(GTS.getInstance()).build());
                for(Text text : MessageConfig.getMessages("Pricing.Tax.Success.Paid", textOptions))
                    player.sendMessage(text);
            } else {
                GTS.getInstance().getLogger().error(Text.of(TextColors.RED, "Account for UUID (" + player.getUniqueId() + ") was not found").toPlain());
                return false;
            }
        }
        return true;
    }

    public static HashMap<String, Optional<Object>> getInfo(EntityPixelmon pokemon){
        HashMap<String, Optional<Object>> info = Maps.newHashMap();
        info.put("nickname", Optional.of(pokemon.getNickname()));
        info.put("ability", Optional.of(pokemon.getAbility().getName()));
        info.put("nature", Optional.of(pokemon.getNature().name()));
        info.put("gender", Optional.of(pokemon.gender.name()));
        info.put("growth", Optional.of(pokemon.getGrowth().name()));
        info.put("shiny", pokemon.getIsShiny() ? Optional.of("Shiny") : Optional.empty());
        info.put("shiny_tf", Optional.of(pokemon.getIsShiny()));
        info.put("level", Optional.of(pokemon.getLvl().getLevel()));
        info.put("form", Optional.of(pokemon.getForm()));
        info.put("halloween", pokemon.getSpecialTexture() == 2 ? Optional.of("halloween textured") : Optional.empty());
        info.put("roasted", pokemon.getSpecialTexture() == 1 ? Optional.of("roast textured") : Optional.empty());

        DecimalFormat df = new DecimalFormat("##0");
        int totalEvs = pokemon.stats.EVs.HP + pokemon.stats.EVs.Attack + pokemon.stats.EVs.Defence + pokemon.stats.EVs.SpecialAttack +
                pokemon.stats.EVs.SpecialDefence + pokemon.stats.EVs.Speed;
        int totalIVs = pokemon.stats.IVs.HP + pokemon.stats.IVs.Attack + pokemon.stats.IVs.Defence + pokemon.stats.IVs.SpAtt +
                pokemon.stats.IVs.SpDef + pokemon.stats.IVs.Speed;

        info.put("EV%", Optional.of(df.format(totalEvs / 510.0 * 100) + "%"));
        info.put("evtotal", Optional.of(totalEvs));
        info.put("evhp", Optional.of(pokemon.stats.EVs.HP));
        info.put("evatk", Optional.of(pokemon.stats.EVs.Attack));
        info.put("evdef", Optional.of(pokemon.stats.EVs.Defence));
        info.put("evspatk", Optional.of(pokemon.stats.EVs.SpecialAttack));
        info.put("evspdef", Optional.of(pokemon.stats.EVs.SpecialDefence));
        info.put("evspeed", Optional.of(pokemon.stats.EVs.Speed));

        info.put("IV%", Optional.of(df.format(totalIVs / 186.0 * 100) + "%"));
        info.put("ivtotal", Optional.of(totalIVs));
        info.put("ivhp", Optional.of(pokemon.stats.IVs.HP));
        info.put("ivatk", Optional.of(pokemon.stats.IVs.Attack));
        info.put("ivdef", Optional.of(pokemon.stats.IVs.Defence));
        info.put("ivspatk", Optional.of(pokemon.stats.IVs.SpAtt));
        info.put("ivspdef", Optional.of(pokemon.stats.IVs.SpDef));
        info.put("ivspeed", Optional.of(pokemon.stats.IVs.Speed));

        return info;
    }

    public static int getPlacement() {
        return placement;
    }

    public static void setPlacement(int placement) {
        LotUtils.placement = placement;
    }

    public static int getLogPlacement() {
        return logPlacement;
    }

    public static void setLogPlacement(int placement) {
        LotUtils.logPlacement = placement;
    }

    public static String capitalize(String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

    public enum Commands {

        Add("INSERT INTO `" + GTS.getInstance().getConfig().getMainTable() + "` (ID, Ends, Expired, uuid, Lot, DoesExpire) VALUES ({{id}}, '{{Ends}}', {{Expired}}, '{{UUID}}', '{{Lot}}', {{Can Expire}})"),
        Update_Expired("UPDATE `" + GTS.getInstance().getConfig().getMainTable() + "` SET Expired=1 WHERE ID={{id}}"),
        Update_Auction("UPDATE `" + GTS.getInstance().getConfig().getMainTable() + "` SET Lot='{{lot}}' WHERE ID={{id}}"),
        Delete("DELETE FROM `" + GTS.getInstance().getConfig().getMainTable() + "` WHERE ID={{id}}"),
        Logs_Add("INSERT INTO `" + GTS.getInstance().getConfig().getLogTable() + "`(ID, Date, uuid, Action, Log) VALUES ({{id}}, '{{Date}}', '{{UUID}}', '{{Action}}', '{{Log}}')");

        private String command;
        private Commands(String command){
            this.command = command;
        }

        public String getCommand(){
            return this.command;
        }
    }

    public static synchronized void addLot(UUID uuid, String json, boolean expired, long time){
        String cmd = Commands.Add.getCommand();

        cmd = cmd.replace("{{id}}", String.valueOf(getPlacement()));
        cmd = cmd.replace("{{Ends}}", Instant.now().plusSeconds(time).toString());
        cmd = cmd.replace("{{Expired}}", String.valueOf(false));
        cmd = cmd.replace("{{UUID}}", uuid.toString());
        cmd = cmd.replace("{{Lot}}", json);
        cmd = cmd.replace("{{Can Expire}}", String.valueOf(expired));
        sqlCmds.add(cmd);
    }

    public static synchronized void setExpired(int id){
        String cmd = Commands.Update_Expired.getCommand();
        cmd = cmd.replace("{{id}}", "" + id);
        sqlCmds.add(cmd);
    }

    public static synchronized void updateLot(Lot lot){
        String cmd = Commands.Update_Auction.getCommand();

        cmd = cmd.replace("{{lot}}", new Gson().toJson(lot));
        cmd = cmd.replace("{{id}}", String.valueOf(lot.getLotID()));

        sqlCmds.add(cmd);
    }

    public static synchronized void deleteLot(int id){
        String cmd = Commands.Delete.getCommand();
        cmd = cmd.replace("{{id}}", "" + id);
        sqlCmds.add(cmd);
    }

    public static synchronized void addLog(UUID uuid, Log log){
        String cmd = Commands.Logs_Add.getCommand();

        cmd = cmd.replace("{{id}}", String.valueOf(log.getId()));
        cmd = cmd.replace("{{Date}}", log.getDate().toInstant().toString());
        cmd = cmd.replace("{{UUID}}", log.getActor().toString());
        cmd = cmd.replace("{{Action}}", log.getAction());
        cmd = cmd.replace("{{Log}}", log.getLog());

        sqlCmds.add(cmd);

        GTS.getInstance().getLogs().put(uuid, log);
        logPlacement++;
    }

    static String getTime(long timeEnd) {
        long seconds = timeEnd / 1000;
        int minutes = (int)(seconds / 60);
        int hours = minutes / 60;
        int days = hours / 24;
        seconds -= 60 * minutes;
        minutes -= 60 * hours;
        hours -= 24 * days;

        String time = "";
        if(days > 0){
            time += days + "d ";
        }
        if(hours > 0){
            time += hours + "h ";
        }
        if(minutes > 0){
            time += minutes + "m ";
        }
        if(seconds > 0){
            time += seconds + "s";
        }
        if(time.equals("")){
            return "Expired";
        }

        return time;
    }

    public static Log forgeLog(User player, String action, HashMap<String, Optional<Object>> replacements){
        Log log = new Log(getLogPlacement(), Date.from(Instant.now()),
                player.getUniqueId(), action);

        if(action.equals("Addition"))
            Log.additionLog().forEach(l -> {
                l = MessageConfig.replaceOptions(l, replacements).toPlain();
                log.setLog(log.getLog() + l + " | ");
            });
        else if(action.equals("Expired"))
            Log.expiresLog().forEach(l -> {
                l = MessageConfig.replaceOptions(l, replacements).toPlain();
                log.setLog(log.getLog() + l + " | ");
            });
        else if(action.equals("Removal"))
            Log.removalLog().forEach(l -> {
                l = MessageConfig.replaceOptions(l, replacements).toPlain();
                log.setLog(log.getLog() + l + " | ");
            });
        else if(action.equals("Purchase-Seller"))
            Log.purchaseLog(1).forEach(l -> {
                l = MessageConfig.replaceOptions(l, replacements).toPlain();
                log.setLog(log.getLog() + l + " | ");
            });
        else if(action.equals("Purchase-Recipient"))
            Log.purchaseLog(2).forEach(l -> {
                l = MessageConfig.replaceOptions(l, replacements).toPlain();
                log.setLog(log.getLog() + l + " | ");
            });
        else if(action.equals("Auction-Seller"))
            Log.auctionLog(1).forEach(l -> {
                l = MessageConfig.replaceOptions(l, replacements).toPlain();
                log.setLog(log.getLog() + l + " | ");
            });
        else if(action.equals("Auction-Winner"))
            Log.auctionLog(2).forEach(l -> {
                l = MessageConfig.replaceOptions(l, replacements).toPlain();
                log.setLog(log.getLog() + l + " | ");
            });
        else if(action.equals("Trade-Owner"))
            Log.tradeLog(1).forEach(l -> {
                l = MessageConfig.replaceOptions(l, replacements).toPlain();
                log.setLog(log.getLog() + l + " | ");
            });
        else if(action.equals("Trade-Recipient"))
            Log.tradeLog(2).forEach(l -> {
                l = MessageConfig.replaceOptions(l, replacements).toPlain();
                log.setLog(log.getLog() + l + " | ");
            });

        return log;
    }
}
