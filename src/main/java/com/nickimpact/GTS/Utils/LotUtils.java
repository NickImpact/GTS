package com.nickimpact.GTS.Utils;

import com.google.gson.Gson;
import com.nickimpact.GTS.Configuration.MessageConfig;
import com.nickimpact.GTS.GTS;
import com.nickimpact.nbthandler.NBTHandler;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerNotLoadedException;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Created by Nick on 12/15/2016.
 */
public class LotUtils {

    public static Object lotFromJson(String s){
        Gson gson = new Gson();
        return gson.fromJson(s, Lot.class);
    }

    public static void addPokemonToMarket(Player player, int slot, int price){
        if (GTS.getInstance().getSql().hasTooMany(player.getUniqueId())) {
            player.sendMessage(MessageConfig.getMessage("GTS.Addition.Error.Exceed Max", GTS.getInstance().getConfig().getMaxPokemon()));
            return;
        }

        Optional<PlayerStorage> storage = PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID((MinecraftServer) Sponge.getServer(), player.getUniqueId());
        if(storage.isPresent()) {
            if (storage.get().count() <= 1) {
                player.sendMessage(MessageConfig.getMessage("GTS.Addition.Error.Last Pokemon"));
                return;
            }
        } else {
            return;
        }

        // Fetch the nbt components at the specific slot, and create them into a pokemon
        NBTTagCompound[] party = storage.get().getList();
        NBTTagCompound nbt = party[slot];
        if (nbt == null) {
            player.sendMessage(MessageConfig.getMessage("GTS.Addition.Error.Empty Slot", slot + 1));
            return;
        }

        EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt, NBTHandler.getWorld());
        for(String s : GTS.getInstance().getConfig().getBlocked()){
            if(pokemon.getName().equalsIgnoreCase(s)){
                player.sendMessage(MessageConfig.getMessage("GTS.Addition.Error.Invalid", pokemon.getName()));
                return;
            }
        }

        // Handle taxing the player, if it is enabled
        BigDecimal tax = new BigDecimal((double)price * GTS.getInstance().getConfig().getTaxRate());
        if(GTS.getInstance().getConfig().isTaxEnabled()) {
            Optional<UniqueAccount> account = GTS.getInstance().getEconomy().getOrCreateAccount(player.getUniqueId());
            if(account.isPresent()) {
                UniqueAccount acc = account.get();
                if(acc.getBalance(GTS.getInstance().getEconomy().getDefaultCurrency()).compareTo(tax) < 0) {
                    player.sendMessage(MessageConfig.getMessage("Pricing.Tax.Error.Not Enough", tax));
                    return;
                }
                acc.withdraw(GTS.getInstance().getEconomy().getDefaultCurrency(), tax, Cause.source(GTS.getInstance()).build());
                player.sendMessage(MessageConfig.getMessage("Pricing.Tax.Success.Paid", tax));
            } else {
                GTS.getInstance().getLogger().error(Text.of(TextColors.RED, "Account for UUID (" + player.getUniqueId() + ") was not found").toPlain());
            }
        }

        // Create the values to be used by the GTS Listing
        PokemonItem pokeItem = new PokemonItem(NBTHandler.getPokemon(nbt), player.getName(), price);

        // Add the GTS Listing to the Database
        int placement = GTS.getInstance().getSql().getPlacement();

        Lot lot = new Lot(placement, player.getUniqueId(), NBTHandler.pokemonToNBT(pokemon).toString(), pokeItem, price);
        Gson gson = new Gson();
        GTS.getInstance().getSql().addLot(player.getUniqueId(), gson.toJson(lot));

        // Remove the pokemon from the client
        storage.get().recallAllPokemon();
        storage.get().removeFromPartyPlayer(slot);
        storage.get().sendUpdatedList();

        // Print Success Message
        player.sendMessage(MessageConfig.getMessage("GTS.Addition.Success.Added", pokemon.getPokemonName()));
    }

    public static void buyLot(Player p, int id) {

        Lot lot = GTS.getInstance().getSql().getLot(id);

        if (lot == null) {
            p.sendMessage(MessageConfig.getMessage("GTS.Purchase.Error.Already Sold"));
            return;
        }
        if (!GTS.getInstance().getSql().isExpired(lot.getLotID())) {
            GTS.getInstance().getSql().deleteLot(id);
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
                    p.sendMessage(MessageConfig.getMessage("GTS.Purchase.Success.Buyer", lot.getItem().getPokemon(lot).getName(), price.intValue()));
                    if (Sponge.getServer().getPlayer(lot.getOwner()).isPresent()) {
                        Sponge.getServer().getPlayer(lot.getOwner()).get().sendMessage(MessageConfig.getMessage("GTS.Purchase.Success.Owner", lot.getItem().getPokemon(lot).getName(), p.getName()));
                    }

                    Optional<UniqueAccount> ownerAccount = GTS.getInstance().getEconomy().getOrCreateAccount(lot.getOwner());
                    if(ownerAccount.isPresent()){
                        UniqueAccount owner = ownerAccount.get();
                        owner.deposit(GTS.getInstance().getEconomy().getDefaultCurrency(), price, Cause.of(NamedCause.source(GTS.getInstance())));
                    } else {
                        GTS.getInstance().getLogger().error("Player '" + Sponge.getServer().getPlayer(lot.getOwner()).get().getName() + "' was unable to receive $" + price.intValue() + " from the GTS");
                    }

                    EntityPixelmon pokemon = lot.getItem().getPokemon(lot);
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

    public static void givePlayerPokemon(Player player, PokemonItem item, Lot lot){
        Optional<PlayerStorage> storage = PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID((MinecraftServer) Sponge.getServer(), player.getUniqueId());
        if(storage.isPresent()){
            storage.get().addToParty(item.getPokemon(lot));
        } else {
            GTS.getInstance().getLogger().error("Player " + player.getName() + " was unable to receive a " + item.getName() + " from GTS");
        }
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
