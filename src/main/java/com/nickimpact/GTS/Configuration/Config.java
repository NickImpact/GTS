package com.nickimpact.GTS.Configuration;

import com.google.common.reflect.TypeToken;
import com.nickimpact.GTS.GTS;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import ninja.leaping.configurate.ConfigurationNode;
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

    private static Path configFile;
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    private static CommentedConfigurationNode main;

    private String databaseType;
    private String host;
    private String db;
    private int port;
    private String user;
    private String password;

    private int maxPokemon;
    private int time;

    private boolean taxEnabled;
    private double taxRate;

    private List<String> blocked;

    public Config(){
        this.loadConfig();
    }

    private void loadConfig(){
        configFile = Paths.get(GTS.getInstance().getConfigDir() + "/settings.conf");
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

            CommentedConfigurationNode node = main.getNode("Settings");
            node.setComment("Alter these settings to control the GTS");

            ConfigurationNode database = node.getNode("SQLDatabase");
            databaseType = database.getNode("Type").getString("H2");
            ConfigurationNode connect = database.getNode("Connection Info");
            host = connect.getNode("Host").getString("localhost");
            db = connect.getNode("SQLDatabase").getString("daycare");
            port = connect.getNode("Port").getInt(3306);
            user = connect.getNode("User").getString("username");
            password = connect.getNode("Password").getString("password");

            CommentedConfigurationNode max = node.getNode("Max Pokemon");
            max.setComment("Set how many pokemon a player is limited to listing, -1 for no cap");
            maxPokemon = max.getInt(3);
            CommentedConfigurationNode lotTime = node.getNode("Lot Time");
            lotTime.setComment("Set how long a lot should be (In Minutes)");
            time = lotTime.getInt(10);

            CommentedConfigurationNode tax = node.getNode("Tax");
            tax.setComment("Control the taxing of GTS listings");
            taxEnabled = tax.getNode("Enabled").getBoolean(false);
            taxRate = tax.getNode("Tax rate").getDouble(0.10);

            ConfigurationNode blacklist = main.getNode("Blacklist");
            List<String> blockedPokemon = new ArrayList<>();
            blockedPokemon.add("Test1");
            blockedPokemon.add("Test2");
            blocked = blacklist.getNode("Pokemon").getList(TypeToken.of(String.class), blockedPokemon);

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

    public String getDatabaseType() {
        return databaseType;
    }

    public String getHost() {
        return host;
    }

    public String getDatabase() {
        return db;
    }

    public int getPort() {
        return port;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public int getMaxPokemon() {
        return maxPokemon;
    }

    public int getLotTime() {
        return time;
    }

    public boolean isTaxEnabled() {
        return taxEnabled;
    }

    public double getTaxRate() {
        return taxRate;
    }

    public List<String> getBlocked() {
        List<String> validBlocks = new ArrayList<>();
        for(String s : blocked){
            if(EnumPokemon.hasPokemon(s)){
                validBlocks.add(s);
            }
        }
        return validBlocks;
    }
}
