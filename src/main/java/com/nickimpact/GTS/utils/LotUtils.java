package com.nickimpact.GTS.utils;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.nickimpact.GTS.configuration.MessageConfig;
import com.nickimpact.GTS.GTS;
import com.nickimpact.GTS.logging.Log;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.HashMap;
import java.util.Optional;

/**
 * Created by Nick on 12/15/2016.
 */
public class LotUtils {

    public static Lot lotFromJson(String s) {
        return new Gson().fromJson(s, Lot.class);
    }

    public static void addPokemonStatic(Player player, int slot, String note, int price, boolean expires, long time) {
        addPokemonToMarket(1, player, slot, note, price, expires, time);
    }

    public static void addPokemonAuc(Player player, int slot, String note, int startPrice, int increment, long time) {
        addPokemonToMarket(2, player, slot, note, startPrice, increment, time);
    }

    public static void addPokemon4Pokemon(Player player, int slot, String note, String pokemon, boolean expires, long time){
        addPokemonToMarket(3, player, slot, note, pokemon, expires, time);
    }

    private static void addPokemonToMarket(int mode, Player player, int slot, String note, Object... options){
        HashMap<String, Optional<Object>> textOptions = Maps.newHashMap();

        if (GTS.getInstance().getSql().hasTooMany(player.getUniqueId())) {
            textOptions.put("max_pokemon", Optional.of(GTS.getInstance().getConfig().getMaxPokemon()));
            for(Text text : MessageConfig.getMessages("Generic.Addition.Error.Exceed Max", textOptions))
                player.sendMessage(text);

            return;
        }

        Optional<PlayerStorage> storage = getStorage(player);
        if(!storage.isPresent()) {
            return;
        }

        NBTTagCompound nbt = getNbt(player, slot, storage.get());
        if(nbt == null) {
            return;
        }

        EntityPixelmon pokemon = getPokemon(player, nbt);
        if(pokemon == null) {
            return;
        }

        // Do checking
        if(mode == 1) {
            int price = (Integer) options[0];
            boolean expires = (Boolean) options[1];
            long time = (Long) options[2];

            if (!handleTax(player, price)) {
                return;
            }

            PokemonItem pokeItem = new PokemonItem(pokemon, player.getName(), price);

            int placement = GTS.getInstance().getSql().getPlacement(1);

            Lot lot = new Lot(placement, player.getUniqueId(), nbt.toString(), pokeItem, price, expires, note);
            GTS.getInstance().getSql().addLot(player.getUniqueId(), new Gson().toJson(lot), expires, time);

            textOptions.put("player", Optional.of(player.getName()));
            textOptions.put("pokemon", Optional.of(pokemon.getName()));
            textOptions.put("lot_type", Optional.of("static"));
            textOptions.put("curr_symbol", Optional.of(GTS.getInstance().getEconomy().getDefaultCurrency().getSymbol().toPlain()));
            textOptions.put("price", Optional.of(price));
            textOptions.put("expires", expires ? Optional.of("Never") : Optional.of(getTime(time * 1000)));
            textOptions.putAll(getInfo(pokemon));

            if (!pokemon.isEgg){
                for(Text text : MessageConfig.getMessages("Generic.Addition.Broadcast.Normal", textOptions))
                    Sponge.getServer().getBroadcastChannel().send(text);
            } else {
                for(Text text : MessageConfig.getMessages("Generic.Addition.Broadcast.Egg.Normal", textOptions))
                    Sponge.getServer().getBroadcastChannel().send(text);
            }

            Log log = forgeLog(player, "Addition", textOptions);
            GTS.getInstance().getSql().appendLog(log);
        } else if(mode == 2) {
            int stPrice = (Integer) options[0];
            int increment = (Integer) options[1];
            long time = (Long) options[2];

            if (!handleTax(player, stPrice)){
                return;
            }

            PokemonItem pokeItem = new PokemonItem(pokemon, player.getName(), stPrice, increment);

            int placement = GTS.getInstance().getSql().getPlacement(1);

            Lot lot = new Lot(placement, player.getUniqueId(), nbt.toString(), pokeItem, stPrice, true, true, null, stPrice, increment, note);
            GTS.getInstance().getSql().addLot(player.getUniqueId(), new Gson().toJson(lot), true, time);

            textOptions.put("player", Optional.of(player.getName()));
            textOptions.put("pokemon", Optional.of(pokemon.getName()));
            textOptions.put("lot_type", Optional.of("Auction"));
            textOptions.put("curr_symbol", Optional.of(GTS.getInstance().getEconomy().getDefaultCurrency().getSymbol().toPlain()));
            textOptions.put("start_price", Optional.of(stPrice));
            textOptions.put("increment", Optional.of(increment));
            textOptions.put("expires", Optional.of(getTime(time * 1000)));
            textOptions.putAll(getInfo(pokemon));

            if(!pokemon.isEgg) {
                for (Text text : MessageConfig.getMessages("Auctions.Broadcast.Pokemon", textOptions))
                    Sponge.getServer().getBroadcastChannel().send(text);
            } else {
                for (Text text : MessageConfig.getMessages("Auctions.Broadcast.Egg.Pokemon", textOptions))
                    Sponge.getServer().getBroadcastChannel().send(text);
            }

            textOptions.put("pokemon", Optional.of(pokemon.getName()));
            Log log = forgeLog(player, "Addition", textOptions);
            GTS.getInstance().getSql().appendLog(log);
        } else {
            String poke = (String)options[0];
            boolean expires = (Boolean)options[1];
            long time = (Long)options[2];

            if(!handleTax(player, GTS.getInstance().getConfig().pokeTax())){
                return;
            }

            PokemonItem pokeItem = new PokemonItem(pokemon, player.getName(), poke);

            int placement = GTS.getInstance().getSql().getPlacement(1);
            Lot lot = new Lot(placement, player.getUniqueId(), nbt.toString(), pokeItem, true, poke, note);
            GTS.getInstance().getSql().addLot(player.getUniqueId(), new Gson().toJson(lot), expires, time);

            textOptions.put("player", Optional.of(player.getName()));
            textOptions.put("pokemon", Optional.of(pokemon.getName()));
            textOptions.put("lot_type", Optional.of("pokemon"));
            textOptions.put("poke_looked_for", Optional.of(poke));
            textOptions.put("expires", expires ? Optional.of("Never") : Optional.of(getTime(time * 1000)));
            textOptions.putAll(getInfo(pokemon));

            if(!pokemon.isEgg) {
                for (Text text : MessageConfig.getMessages("Generic.Addition.Broadcast.Pokemon", textOptions))
                    Sponge.getServer().getBroadcastChannel().send(text);
            } else {
                for (Text text : MessageConfig.getMessages("Generic.Addition.Broadcast.Egg", textOptions))
                    Sponge.getServer().getBroadcastChannel().send(text);
            }

            textOptions.put("pokemon", Optional.of(pokemon.getName()));
            Log log = forgeLog(player, "Addition", textOptions);
            GTS.getInstance().getSql().appendLog(log);
        }

        // Remove the pokemon from the client
        storage.get().recallAllPokemon();
        storage.get().removeFromPartyPlayer(slot);
        storage.get().sendUpdatedList();

        // Print Success Message
        textOptions.put("pokemon", Optional.of(pokemon.isEgg ? "Mystery Egg" : pokemon.getName()));
        for(Text text : MessageConfig.getMessages("Generic.Addition.Success", textOptions))
            player.sendMessage(text);
    }

    public static void buyLot(Player p, Lot lot) {
        HashMap<String, Optional<Object>> textOptions = Maps.newHashMap();
        if (lot == null) {
            for(Text text : MessageConfig.getMessages("Generic.Purchase.Error.Already Sold", null))
                p.sendMessage(text);
            return;
        }
        if (!GTS.getInstance().getSql().isExpired(lot.getLotID())) {
            BigDecimal price = new BigDecimal(lot.getPrice());
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

                    textOptions.put("pokemon", lot.getItem().getPokemon(lot, p).isEgg ? Optional.of("Mystery Egg") : Optional.of(lot.getItem().getName()));
                    textOptions.put("curr_symbol", Optional.of(GTS.getInstance().getEconomy().getDefaultCurrency().getSymbol().toPlain()));
                    textOptions.put("price", Optional.of(price));
                    textOptions.put("seller", Optional.of(Sponge.getServer().getGameProfileManager().getCache().getById(lot.getOwner()).get().getName()));
                    for(Text text : MessageConfig.getMessages("Generic.Purchase.Success.Buyer", textOptions))
                        p.sendMessage(text);

                    textOptions.put("pokemon", Optional.of(lot.getItem().getName()));
                    Log log = forgeLog(p, "Purchase-Recipient", textOptions);
                    GTS.getInstance().getSql().appendLog(log);

                    textOptions.clear();

                    GTS.getInstance().getSql().deleteLot(lot.getLotID());
                    if (Sponge.getServer().getPlayer(lot.getOwner()).isPresent()) {
                        textOptions.put("pokemon", Optional.of(lot.getItem().getName()));
                        textOptions.put("curr_symbol", Optional.of(GTS.getInstance().getEconomy().getDefaultCurrency().getSymbol().toPlain()));
                        textOptions.put("price", Optional.of(price));
                        textOptions.put("buyer", Optional.of(p.getName()));

                        for(Text text : MessageConfig.getMessages("Generic.Purchase.Success.Seller", textOptions))
                            Sponge.getServer().getPlayer(lot.getOwner()).get().sendMessage(text);

                        Log log2 = forgeLog(p, "Purchase-Owner", textOptions);
                        GTS.getInstance().getSql().appendLog(log2);
                    }

                    Optional<UniqueAccount> ownerAccount = GTS.getInstance().getEconomy().getOrCreateAccount(lot.getOwner());
                    if(ownerAccount.isPresent()){
                        UniqueAccount owner = ownerAccount.get();
                        owner.deposit(GTS.getInstance().getEconomy().getDefaultCurrency(), price, Cause.of(NamedCause.source(GTS.getInstance())));
                    } else {
                        GTS.getInstance().getLogger().error("Player '" + Sponge.getServer().getPlayer(lot.getOwner()).get().getName() + "' was unable to receive $" + price.intValue() + " from the GTS");
                    }

                    EntityPixelmon pokemon = lot.getItem().getPokemon(lot, p);
                    Optional<PlayerStorage> storage = PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID((MinecraftServer) Sponge.getServer(), p.getUniqueId());
                    if(storage.isPresent()){
                        storage.get().addToParty(pokemon);
                    } else {
                        GTS.getInstance().getLogger().error("Player " + p.getName() + " was unable to receive a " + lot.getItem().getName() + " from GTS");
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
                    for (Text text : MessageConfig.getMessages("Generic.Purchase.Error.Not Enough", null))
                        player.sendMessage(text);
                    return;
                }
            }

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

        for(Player p : lot.getAucListeners())
            if(!p.getUniqueId().equals(lot.getHighBidder())) {
                textOptions.put("pokemon", Optional.of(lot.getItem().getName()));
                textOptions.put("curr_symbol", Optional.of(GTS.getInstance().getEconomy().getDefaultCurrency().getSymbol().toPlain()));
                textOptions.put("price", Optional.of(lot.getStPrice() + lot.getIncrement()));

                for(Text text : MessageConfig.getMessages("Auctions.Placed Bid", textOptions))
                    p.sendMessage(text);
            }

        lot.setStPrice(lot.getStPrice() + lot.getIncrement());
        lot.setHighBidder(player.getUniqueId());
        GTS.getInstance().getSql().updateEntry(lot);
    }

    public static void trade(Player player, Lot lot){
        GTS.getInstance().getSql().deleteLot(lot.getLotID());
        givePlayerPokemon(player, lot);

        HashMap<String, Optional<Object>> textOptions = Maps.newHashMap();
        textOptions.put("poke_looked_for", Optional.of(lot.getPokeWanted()));
        textOptions.put("pokemon", Optional.of(lot.getItem().getName()));
        textOptions.put("player", Optional.of(player.getName()));
        Log log = forgeLog(player, "Trade", textOptions);
        GTS.getInstance().getSql().appendLog(log);
        for(Text text : MessageConfig.getMessages("Generic.Trade.Recipient.Receive-Poke", textOptions))
            player.sendMessage(text);

        Sponge.getServer().getPlayer(lot.getOwner()).ifPresent(o -> {
            Log log2 = forgeLog(player, "Trade", textOptions);
            GTS.getInstance().getSql().appendLog(log2);

            for(Text text : MessageConfig.getMessages("Generic.Trade.Owner.Receive-Poke", textOptions))
                o.sendMessage(text);
        });
    }

    public static void givePlayerPokemon(Player player, Lot lot){
        Optional<PlayerStorage> storage = PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID((MinecraftServer) Sponge.getServer(), player.getUniqueId());
        if(storage.isPresent()){
            storage.get().addToParty(lot.getItem().getPokemon(lot, player));
        } else {
            GTS.getInstance().getLogger().error("Player " + player.getName() + " was unable to receive a " + lot.getItem().getName() + " from GTS");
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

    private static boolean handleTax(Player player, int price){
        BigDecimal tax = new BigDecimal((double)price * GTS.getInstance().getConfig().getTaxRate());
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

    public static Log forgeLog(Player player, String action, HashMap<String, Optional<Object>> replacements){
        Log log = new Log(GTS.getInstance().getSql().getPlacement(2), Date.from(Instant.now()),
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
