package com.nickimpact.GTS.Configuration;

import com.nickimpact.GTS.GTS;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextFormat;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.common.text.SpongeTexts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Map;

/**
 * Created by Nick on 12/15/2016.
 */
public class MessageConfig {

    private static Path configFile;
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    private static CommentedConfigurationNode main;

    public MessageConfig(){
        this.loadConfig();
    }

    private void loadConfig(){
        configFile = Paths.get(GTS.getInstance().getConfigDir() + "/messages.conf");
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

            CommentedConfigurationNode node = main.getNode("Messages");
            node.setComment("These control the messages sent by GTS");

            CommentedConfigurationNode gts = node.getNode("GTS");
            gts.setComment("These nodes are for many of the main functions in the GTS");

            ConfigurationNode addition = gts.getNode("Addition");
            ConfigurationNode success = addition.getNode("Success");
            success.getNode("Added").getString("&a&lGTS &7| &e{0} &7has been successfully deposited into the GTS");

            ConfigurationNode error = addition.getNode("Error");
            error.getNode("Exceed Max").getString("&c&lGTS &7| Sorry, but you already have {0} pokemon in the GTS..");
            error.getNode("Last Pokemon").getString("&c&lGTS &7| You can't deposit your last pokemon..");
            error.getNode("Empty Slot").getString("&c&lGTS &7| Unfortunately, it appears slot &e{0} &7is empty..");
            error.getNode("Invalid").getString("&c&lGTS &7| Unfortunately, &e{0} &7has been blacklisted from the GTS..");

            ConfigurationNode display = gts.getNode("Display");
            error = display.getNode("Error");
            error.getNode("Empty").getString("&c&lGTS &7| No listings were detected in the system..");

            ConfigurationNode purchase = gts.getNode("Purchase");
            success = purchase.getNode("Success");
            success.getNode("Owner").getString("&a&lGTS &7| Your &e{0} &7was just purchased by &3{1}");
            success.getNode("Buyer").getString("&a&lGTS &7| You successfully purchased the &e{0} &7for &c${1}");

            error = purchase.getNode("Error");
            error.getNode("Already Sold").getString("&c&lGTS &7| Unfortunately, it appears that lot has sold already..");
            error.getNode("Expired").getString("&c&lGTS &7| Unfortunately, it appears that lot has expired..");
            error.getNode("Not Enough").getString("&c&lGTS &7| You don't have enough money to afford this listing..");

            ConfigurationNode failed = purchase.getNode("Failed");
            failed.getString("&c&lGTS &7| It seems that lot has been recently purchased.. sorry!");

            ConfigurationNode remove = gts.getNode("Remove");
            remove.getNode("Success").getString("&a&lGTS &7| Your &e{0} &7has been removed from the GTS");
            remove.getNode("Failed").getString("&c&lGTS &7| Your &e{0} &7has been purchased already!");
            remove.getNode("Expired").getString("&c&lGTS &7| Your market ended, so your &e{0} &7was returned!");

            ConfigurationNode admin = remove.getNode("Admin");
            admin.getNode("Delete").getString("&a&lGTS &7| The &e{0} &7has been removed and deleted!");
            admin.getNode("Remove").getString("&a&lGTS &7| The &e{0} &7has been removed from the GTS!");

            ConfigurationNode search = gts.getNode("Search");
            error = search.getNode("Error");
            error.getNode("Not Found").getString("&c&lGTS &7| Unfortunately, your search returned no results..");

            CommentedConfigurationNode menus = node.getNode("Menus");
            menus.setComment("Used in item displays in the main menus");
            menus.getNode("Page Up").getString("&aNext Page");
            menus.getNode("Page Down").getString("&cLast Page");
            menus.getNode("Refresh List").getString("&eRefresh Listings");
            menus.getNode("Balance Icon").getString("&7Balance &7(&a${0}&7)");

            ConfigurationNode searchFor = menus.getNode("Search For");
            searchFor.getNode("Title").getString("&aSearching for:");
            searchFor.getNode("Lore Format").getString("  &7- &3{0}");
            menus.getNode("Player Icon").getString("&7Player Info");
            menus.getNode("Player Listings").getString("&aYour Listings");
            menus.getNode("Last Menu").getString("&cExit GTS");

            CommentedConfigurationNode pricing = node.getNode("Pricing");
            ConfigurationNode tax = pricing.getNode("Tax");
            success = tax.getNode("Success");
            success.getNode("Paid").getString("&a&lGTS &7| You paid ${0} in taxes for your GTS listing");

            error = tax.getNode("Error");
            error.getNode("Not Enough").getString("&c&lGTS &7| You're unable to afford the tax of ${0}..");

            CommentedConfigurationNode admin2 = node.getNode("Admin");
            admin2.setComment("Basic return messages for admin specific functions in the plugin");
            admin2.getNode("Reload").getString("&a&lGTS &7| Configuration successfully reloaded!");
            admin2.getNode("Clear").getString("&a&lGTS &7| All listings have been returned to their owners!");

            loader.save(main);
        } catch (IOException e) {
            GTS.getInstance().getLogger().error("    - An error occurred in the message config initialization");
            e.printStackTrace();
            return;
        }
        GTS.getInstance().getLogger().info("    - Message Config successfully initialized");
    }

    public static void saveConfig(){
        try{
            loader.save(main);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reload(){
        try {
            main = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Text getMessage(String path, Object... replacements){
        String message = main.getNode((Object[])("Messages." + path).split("\\.")).getString();
        if(message == null){
            return Text.of(TextColors.RED, "A missing message setup was detected for path: ", TextColors.YELLOW, "Messages." + path);
        }
        return TextSerializers.FORMATTING_CODE.deserialize(MessageFormat.format(message, replacements));
    }
}
