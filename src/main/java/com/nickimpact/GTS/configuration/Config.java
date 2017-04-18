package com.nickimpact.GTS.configuration;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.nickimpact.GTS.GTS;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

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
                    "|                         storage Settings                          | #\n" +
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

            general.getNode("Max-Pokemon").setComment("Set how many pokemon a player is limited to listing, -1 for no cap");
            general.getNode("Max-Pokemon").getInt(3);

            general.getNode("Lot-Time").setComment("Sets the default duration of a lot (In Minutes)");
            general.getNode("Lot-Time").getInt(60);

            general.getNode("Auctions", "Default-Time").setComment("The default duration of an auction in the GTS (In Minutes)");
            general.getNode("Auctions", "Default-Time").getInt(1);
            general.getNode("Auctions", "Increment").setComment("The default amount to increase each bid by");
            general.getNode("Auctions", "Increment").getInt(50);

            general.getNode("Tax").setComment("Control the taxing of GTS listings");
            general.getNode("Tax", "Enabled").getBoolean(false);
            general.getNode("Tax", "Percentage").getDouble(0.10);

            List<String> blockedPokemon = new ArrayList<>();
            blockedPokemon.add("Blacklisted_Poke1");
            blockedPokemon.add("Blacklisted_Poke2");
            blacklist.getList(TypeToken.of(String.class), blockedPokemon);

            loader.save(main);
        } catch (IOException e) {
            GTS.getInstance().getLogger().error("  An error occurred in config initialization");
            e.printStackTrace();
            return;
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
        GTS.getInstance().getLogger().info("    - Config successfully initialized");

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

    public boolean isTaxEnabled() {
        return main.getNode("General", "Taxes", "Enabled").getBoolean();
    }

    public double getTaxRate() {
        return main.getNode("General", "Taxes", "Percentage").getInt();
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

        return 0;
    }

    public long getAucTime() {
        return main.getNode("General", "Auctions", "Default-Time").getInt() * 60;
    }
}
