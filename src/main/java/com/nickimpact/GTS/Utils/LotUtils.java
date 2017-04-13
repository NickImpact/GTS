package com.nickimpact.GTS.Utils;

import com.google.gson.Gson;
import com.nickimpact.GTS.Configuration.MessageConfig;
import com.nickimpact.GTS.GTS;
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
import java.util.Optional;

/**
 * Created by Nick on 12/15/2016.
 */
public class LotUtils {

    public static Lot lotFromJson(String s) {
        return new Gson().fromJson(s, Lot.class);
    }

    public static void addPokemonStatic(Player player, int slot, int price, boolean expires, long time) {
        addPokemonToMarket(1, player, slot, price, expires, time);
    }

    public static void addPokemonAuc(Player player, int slot, int startPrice, int increment, long time) {
        addPokemonToMarket(2, player, slot, startPrice, increment, time);
    }

    public static void addPokemon4Pokemon(Player player, int slot, String pokemon, boolean expires, long time){
        addPokemonToMarket(3, player, slot, pokemon, expires, time);
    }

    private static void addPokemonToMarket(int mode, Player player, int slot, Object... options){
        if (GTS.getInstance().getSql().hasTooMany(player.getUniqueId())) {
            player.sendMessage(MessageConfig.getMessage("GTS.Addition.Error.Exceed Max", GTS.getInstance().getConfig().getMaxPokemon()));
            return;
        }

        Optional<PlayerStorage> storage = getStorage(player);
        if(!storage.isPresent()) {
            player.sendMessage(Text.of(TextColors.RED, "Your party info couldn't be found..."));
            return;
        }

        NBTTagCompound nbt = getNbt(player, slot, storage.get());
        if(nbt == null) {
            player.sendMessage(Text.of(TextColors.RED, "The nbt info of slot " + slot + "couldn't be found..."));
            return;
        }

        EntityPixelmon pokemon = getPokemon(player, nbt);
        if(pokemon == null) {
            player.sendMessage(Text.of(TextColors.RED, "The pokemon couldn't be forged..."));
            return;
        }

        // Do checking
        if(mode == 1){
            int price = (Integer)options[0];
            boolean expires = (Boolean)options[1];
            long time = (Long)options[2];

            if (!handleTax(player, price)){
                player.sendMessage(Text.of(TextColors.RED, "Unable to afford taxes..."));
                return;
            }

            PokemonItem pokeItem = new PokemonItem(pokemon, player.getName(), price);

            int placement = GTS.getInstance().getSql().getPlacement();

            Lot lot = new Lot(placement, player.getUniqueId(), nbt.toString(), pokeItem, price, expires);
            GTS.getInstance().getSql().addLot(player.getUniqueId(), new Gson().toJson(lot), expires, time);

            Sponge.getServer().getBroadcastChannel().send(MessageConfig.getMessage("GTS.Addition.Broadcast.Static", player.getName(), pokemon.getName()));
        } else if(mode == 2) {
            int stPrice = (Integer) options[0];
            int increment = (Integer) options[1];
            long time = (Long) options[2];

            if (!handleTax(player, stPrice)){
                player.sendMessage(Text.of(TextColors.RED, "Unable to afford taxes..."));
                return;
            }

            PokemonItem pokeItem = new PokemonItem(pokemon, player.getName(), stPrice, increment);

            int placement = GTS.getInstance().getSql().getPlacement();

            Lot lot = new Lot(placement, player.getUniqueId(), nbt.toString(), pokeItem, stPrice, false, true, null, stPrice, increment);
            GTS.getInstance().getSql().addLot(player.getUniqueId(), new Gson().toJson(lot), true, time);

            Sponge.getServer().getBroadcastChannel().send(MessageConfig.getMessage("GTS.Addition.Broadcast.Auction", player.getName(), pokemon.getName()));
        } else {
            String poke = (String)options[0];
            boolean expires = (Boolean)options[1];
            long time = (Long)options[2];

            if(!handleTax(player, GTS.getInstance().getConfig().pokeTax())){
                player.sendMessage(Text.of(TextColors.RED, "Unable to afford taxes..."));
                return;
            }

            PokemonItem pokeItem = new PokemonItem(pokemon, player.getName(), poke);

            int placement = GTS.getInstance().getSql().getPlacement();
            Lot lot = new Lot(placement, player.getUniqueId(), nbt.toString(), pokeItem, true, poke);
            GTS.getInstance().getSql().addLot(player.getUniqueId(), new Gson().toJson(lot), expires, time);
            Sponge.getServer().getBroadcastChannel().send(MessageConfig.getMessage("GTS.Addition.Broadcast.Pokemon", player.getName(), pokemon.getName()));
        }

        // Remove the pokemon from the client
        storage.get().recallAllPokemon();
        storage.get().removeFromPartyPlayer(slot);
        storage.get().sendUpdatedList();

        // Print Success Message
        player.sendMessage(MessageConfig.getMessage("GTS.Addition.Success.Added", pokemon.getPokemonName()));
    }

    public static void buyLot(Player p, Lot lot) {

        if (lot == null) {
            p.sendMessage(MessageConfig.getMessage("GTS.Purchase.Error.Already Sold"));
            return;
        }
        if (!GTS.getInstance().getSql().isExpired(lot.getLotID())) {
            BigDecimal price = new BigDecimal(lot.getPrice());
            try {
                Optional<UniqueAccount> account = GTS.getInstance().getEconomy().getOrCreateAccount(p.getUniqueId());
                if(account.isPresent()) {
                    UniqueAccount acc = account.get();
                    if(acc.getBalance(GTS.getInstance().getEconomy().getDefaultCurrency()).intValue() < price.intValue()){
                        p.sendMessage(MessageConfig.getMessage("GTS.Purchase.Error.Not Enough"));
                        return;
                    }
                    acc.withdraw(GTS.getInstance().getEconomy().getDefaultCurrency(), price, Cause.source(GTS.getInstance()).build());
                    p.sendMessage(MessageConfig.getMessage("GTS.Purchase.Success.Buyer", lot.getItem().getPokemon(lot, p).getName(), price.intValue()));
                    GTS.getInstance().getSql().deleteLot(lot.getLotID());
                    if (Sponge.getServer().getPlayer(lot.getOwner()).isPresent()) {
                        Sponge.getServer().getPlayer(lot.getOwner()).get().sendMessage(MessageConfig.getMessage("GTS.Purchase.Success.Owner", lot.getItem().getPokemon(lot, p).getName(), p.getName()));
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
            p.sendMessage(MessageConfig.getMessage("GTS.Purchase.Error.Expired"));
        }
    }

    public static void bid(Player player, Lot lot){
        if(lot.getHighBidder() != null){
            if(player.getUniqueId().equals(lot.getHighBidder())){
                player.sendMessage(Text.of(TextColors.RED, "You must wait till someone outbids you to bid again..."));
                return;
            }

            Optional<Player> p = Sponge.getServer().getPlayer(lot.getHighBidder());
            if(p.isPresent()){
                if(p.get().isOnline()){
                    p.get().sendMessage(MessageConfig.getMessage("GTS.Auction.Outbid", player.getName(), lot.getItem().getName()));
                }
            }
        }

        for(Player p : lot.getAucListeners())
            if(!p.getUniqueId().equals(lot.getHighBidder()))
                p.sendMessage(MessageConfig.getMessage("GTS.Auction.Placed-Bid", player.getName(), lot.getItem().getName()));

        lot.setStPrice(lot.getStPrice() + lot.getIncrement());
        lot.setHighBidder(player.getUniqueId());
        GTS.getInstance().getSql().updateEntry(lot);
    }

    public static void trade(Player player, Lot lot){
        GTS.getInstance().getSql().deleteLot(lot.getLotID());
        givePlayerPokemon(player, lot);
        player.sendMessage(MessageConfig.getMessage("GTS.Trade.Recipient.Receive-Poke", lot.getItem().getName()));

        Sponge.getServer().getPlayer(lot.getOwner()).ifPresent(o ->
                o.sendMessage(MessageConfig.getMessage("GTS.Trade.Owner.Receive-Poke", lot.getPokeWanted())));
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
                player.sendMessage(MessageConfig.getMessage("GTS.Addition.Error.Last Pokemon"));
                return Optional.empty();
            }

        return storage;
    }

    private static NBTTagCompound getNbt(Player player, int slot, PlayerStorage ps){
        NBTTagCompound[] party = ps.getList();
        NBTTagCompound nbt = party[slot];
        if (nbt == null) {
            player.sendMessage(MessageConfig.getMessage("GTS.Addition.Error.Empty Slot", slot + 1));
            return null;
        }

        return nbt;
    }

    private static EntityPixelmon getPokemon(Player player, NBTTagCompound nbt){
        EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt, (World)player.getWorld());
        for(String s : GTS.getInstance().getConfig().getBlocked()){
            if(pokemon.getName().equalsIgnoreCase(s)){
                player.sendMessage(MessageConfig.getMessage("GTS.Addition.Error.Invalid", pokemon.getName()));
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
                UniqueAccount acc = account.get();
                if(acc.getBalance(GTS.getInstance().getEconomy().getDefaultCurrency()).compareTo(tax) < 0) {
                    player.sendMessage(MessageConfig.getMessage("Pricing.Tax.Error.Not Enough", tax));
                    return false;
                }
                acc.withdraw(GTS.getInstance().getEconomy().getDefaultCurrency(), tax, Cause.source(GTS.getInstance()).build());
                player.sendMessage(MessageConfig.getMessage("Pricing.Tax.Success.Paid", tax));
            } else {
                GTS.getInstance().getLogger().error(Text.of(TextColors.RED, "Account for UUID (" + player.getUniqueId() + ") was not found").toPlain());
                return false;
            }
        }
        return true;
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
}
