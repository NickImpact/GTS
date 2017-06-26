package com.nickimpact.GTS.configuration;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.nickimpact.GTS.GTS;
import com.nickimpact.GTS.GTSInfo;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick on 12/10/2016.
 */
public class Config {

    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    private static CommentedConfigurationNode main;

    public Config(){
        this.loadConfig();
    }

    private void loadConfig(){
        Path configFile = Paths.get(GTS.getInstance().getConfigDir() + "/settings.conf");
        loader = HoconConfigurationLoader.builder().setPath(configFile).build();

        try{
            if(!Files.exists(GTS.getInstance().getConfigDir())){
                Files.createDirectory(GTS.getInstance().getConfigDir());
            }

            if(!Files.exists(configFile)){
                Files.createFile(configFile);
            }

            if(main == null){
                main = loader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));
            }

            CommentedConfigurationNode general = main.getNode("General");
            CommentedConfigurationNode storage = main.getNode("Storage");
            CommentedConfigurationNode blacklist = main.getNode("Blacklist");

            blacklist.setComment(
                    "+-------------------------------------------------------------------+ #\n" +
                    "|                        Blacklisted Pokemon                        | #\n" +
                    "+-------------------------------------------------------------------+ #\n" +
                    "| Here, you can define pokemon which would be unable to be listed   | #\n" +
                    "| in the GTS listings. Simply just specify the name of the pokemon, | #\n" +
                    "| and let the system do the rest!                                   | #\n" +
                    "+-------------------------------------------------------------------+ #\n");

            general.setComment("\n" +
                    "+-------------------------------------------------------------------+ #\n" +
                    "|                         General Settings                          | #\n" +
                    "+-------------------------------------------------------------------+ #\n" +
                    "| Listed in this section are the main controls of GTS. Alter these  | #\n" +
                    "| values to change functionality of the plugin to your liking!      | #\n" +
                    "+-------------------------------------------------------------------+ #\n");

            storage.setComment("\n" +
                    "+-------------------------------------------------------------------+ #\n" +
                    "|                         Storage Settings                          | #\n" +
                    "+-------------------------------------------------------------------+ #\n" +
                    "| Ahh, the storage options. With GTS, you can choose to store your  | #\n" +
                    "| data in a H2 or MySQL database. Simply specify which type of      | #\n" +
                    "| storage option you plan to use, and apply connection info as      | #\n" +
                    "| necessary.                                                        | #\n" +
                    "|                                                                   | #\n" +
                    "| Note: When the H2 database is first created, whatever connection  | #\n" +
                    "| info is set will be what the databases uses for its admin user.   | #\n" +
                    "+-------------------------------------------------------------------+ #\n");

            storage.getNode("storage-Method").getString("H2");
            storage.getNode("Connection", "Host").getString("localhost");
            storage.getNode("Connection", "Port").getInt(3306);
            storage.getNode("Connection", "Database").getString("GTS");
            storage.getNode("Connection", "User").getString("username");
            storage.getNode("Connection", "Password").getString("password");
            storage.getNode("Tables", "Main").getString("GTS");
            storage.getNode("Tables", "Logs").getString("Logs");

            general.getNode("Modules", "Trading").getBoolean(true);
            general.getNode("Modules", "Auction").getBoolean(true);
            general.getNode("Modules", "Price-Tag").getBoolean(true);

            general.getNode("Max-Pokemon").setComment("Set how many pokemon a player is limited to listing, -1 for no cap");
            general.getNode("Max-Pokemon").getInt(3);

            general.getNode("Min-Prices").setComment("Use these values to enforce prices above the matching criteria");
            general.getNode("Min-Prices", "Normal Pokemon").getInt(1000);
            general.getNode("Min-Prices", "Shiny Pokemon").getInt(5000);
            general.getNode("Min-Prices", "HAs").getInt(10000);
            general.getNode("Min-Prices", "Per IV", "Price").getInt(5000);
            general.getNode("Min-Prices", "Per IV", "Minimum").getInt(28);
            general.getNode("Min-Prices", "Legendary Pokemon").getInt(50000);


            general.getNode("Lot-Time").setComment("Sets the default duration of a lot (In Minutes)");
            general.getNode("Lot-Time").getInt(60);
            general.getNode("Max-Lot-Time").setComment("The max duration of an auction in the GTS (In Minutes)");
            general.getNode("Max-Lot-Time").getInt(720);

            general.getNode("Auctions", "Default-Time").setComment("The default duration of an auction in the GTS (In Minutes)");
            general.getNode("Auctions", "Default-Time").getInt(1);
            general.getNode("Auctions", "Max-Time").setComment("The max duration of an auction in the GTS (In Minutes)");
            general.getNode("Auctions", "Max-Time").getInt(5);
            general.getNode("Auctions", "Increment").setComment("The default amount to increase each bid by");
            general.getNode("Auctions", "Increment").getInt(50);

            general.getNode("Tax").setComment("Control the taxing of GTS listings");
            general.getNode("Tax", "Enabled").getBoolean(false);
            general.getNode("Tax", "Percentage").getDouble(0.10);

            general.getNode("Tax", "Stacking-Tax").setComment(
                    "These values can be used to specify a higher tax\n" +
                    "for specific criteria. Turning on the enabled option\n" +
                    "in the stacking options will allow the legends and shiny\n" +
                    "taxes to stack on top of each other."
            );
            general.getNode("Tax", "Stacking-Tax", "Enabled").getBoolean(false);
            general.getNode("Tax", "Stacking-Tax", "Max Tax").getDouble(0.50);
            general.getNode("Tax", "Stacking-Tax", "HAs").getDouble(0.20);
            general.getNode("Tax", "Stacking-Tax", "IVs").getDouble(0.175);
            general.getNode("Tax", "Stacking-Tax", "Shiny").getDouble(0.15);
            general.getNode("Tax", "Stacking-Tax", "Legendary").getDouble(0.20);
            general.getNode("Tax", "Pokemon Trade Tax").setComment("The value to start with when evaluating tax on pokemon 4 pokemon trades");
            general.getNode("Tax", "Pokemon Trade Tax").getInt(500);

            List<String> blockedPokemon = new ArrayList<>();
            blockedPokemon.add("Blacklisted_Poke1");
            blockedPokemon.add("Blacklisted_Poke2");
            blacklist.getList(TypeToken.of(String.class), blockedPokemon);

            loader.save(main);
        } catch (IOException e) {
            GTS.getInstance().getConsole().sendMessage(Text.of(
                    GTSInfo.ERROR_PREFIX, TextColors.RED, "Config initialization failed"
            ));
            e.printStackTrace();
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
    }

    public static void saveConfig(){
        try{
            loader.save(main);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        try {
            main = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // SQL Configuration Options
    public String getDatabaseType() {
        return main.getNode("Storage", "storage-Method").getString();
    }

    public String getHost() {
        return main.getNode("Storage", "Connection", "Host").getString();
    }

    public String getDatabase() {
        return main.getNode("Storage", "Connection", "Database").getString();
    }

    public int getPort() {
        return main.getNode("Storage", "Connection", "Port").getInt();
    }

    public String getUser() {
        return main.getNode("Storage", "Connection", "User").getString();
    }

    public String getPassword() {
        return main.getNode("Storage", "Connection", "Password").getString();
    }

    public String getMainTable() {
        return main.getNode("Storage", "Tables", "Main").getString();
    }

    public String getLogTable() {
        return main.getNode("Storage", "Tables", "Logs").getString();
    }

    // Other Configuration Options
    public int getMaxPokemon() {
        return main.getNode("General", "Max-Pokemon").getInt();
    }

    public long getLotTime() {
        return main.getNode("General", "Lot-Time").getInt() * 60;
    }

    public long getMaxLotTime() {
        return main.getNode("General", "Max-Lot-Time").getInt() * 60;
    }

    public long getMaxAucTime() {
        return main.getNode("General", "Auctions", "Max-Time").getInt() * 60;
    }

    public boolean isTaxEnabled() {
        return main.getNode("General", "Tax", "Enabled").getBoolean();
    }

    public double getTaxRate() {
        return main.getNode("General", "Tax", "Percentage").getDouble();
    }

    public List<String> getBlocked() {
        try {
            List<String> validBlocks = new ArrayList<>();
            for (String s : main.getNode("Blacklist").getList(TypeToken.of(String.class))) {
                if (EnumPokemon.hasPokemon(s)) {
                    validBlocks.add(s);
                }
            }
            return validBlocks;
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
        return Lists.newArrayList();
    }

    public int getIncrement() {
        return main.getNode("General", "Auctions", "Default-Time").getInt() * 60;
    }

    public int pokeTax() {

        return main.getNode("General", "Tax", "Pokemon Trade Tax").getInt();
    }

    public int getShinyTax(){
        return main.getNode("General", "Tax", "Stacking-Tax", "Shiny").getInt();
    }

    public int getLegendTax(){
        return main.getNode("General", "Tax", "Stacking-Tax", "Legendary").getInt();
    }

    public boolean isStackTaxEnabled(){
        return main.getNode("General", "Tax", "Stacking-Tax", "Enabled").getBoolean();
    }

    public int getMinPrice(){
        return main.getNode("General", "Min-Prices", "Normal Pokemon").getInt();
    }

    public int getMinShinyPrice(){
        return main.getNode("General", "Min-Prices", "Shiny Pokemon").getInt();
    }

    public int getMinLegendPrice(){
        return main.getNode("General", "Min-Prices", "Legendary Pokemon").getInt();
    }

    public int getMinHAPrice(){
        return main.getNode("General", "Min-Prices", "HAs").getInt();
    }

    public int getMinIVPrice(){
        return main.getNode("General", "Min-Prices", "Per IV", "Price").getInt();
    }

    public int getMinIV(){
        return main.getNode("General", "Min-Prices", "Per IV", "Minimum").getInt();
    }

    public long getAucTime() {
        return main.getNode("General", "Auctions", "Default-Time").getInt() * 60;
    }

    public boolean cmdPriceTagEnabled() {
        return main.getNode("General", "Modules", "Price-Tag").getBoolean();
    }

    public boolean cmdAuctionEnabled() {
        return main.getNode("General", "Modules", "Auction").getBoolean();
    }

    public boolean cmdTradeEnabled() {
        return main.getNode("General", "Modules", "Trading").getBoolean();
    }
}
