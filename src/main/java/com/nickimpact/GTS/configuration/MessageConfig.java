package com.nickimpact.GTS.configuration;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.nickimpact.GTS.GTS;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Nick on 12/15/2016.
 */
public class MessageConfig {

    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    private static CommentedConfigurationNode main;

    public MessageConfig(){
        this.loadConfig();
    }

    private void loadConfig(){
        Path configFile = Paths.get(GTS.getInstance().getConfigDir() + "/messages.conf");
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

            CommentedConfigurationNode generic = main.getNode("Generic");
            CommentedConfigurationNode admin = main.getNode("Administrative");
            CommentedConfigurationNode auctions = main.getNode("Auctions");
            CommentedConfigurationNode pricing = main.getNode("Pricing");
            CommentedConfigurationNode menus = main.getNode("UI");

            // Generic Settings
            generic.setComment("\n" +
                    "+-------------------------------------------------------------------+ #\n" +
                    "|                         Generic Messages                          | #\n" +
                    "+-------------------------------------------------------------------+ #");

            // Addition Messages:
            generic.getNode("Addition", "Broadcast", "Normal").getList(TypeToken.of(String.class), Lists.newArrayList(
                    "&a&lGTS &e\u00BB &c{{player}} &7has added a &a{{ability}} {{IV%}} {{shiny:s}}{{pokemon}} &7to the GTS for &a{{curr_symbol}}{{price}}&7!"
            ));

            generic.getNode("Addition", "Broadcast", "Pokemon").getList(TypeToken.of(String.class), Lists.newArrayList(
                    "&a&lGTS &e\u00BB &c{{player}} &7has added a &a{{ability}} {{IV%}} {{shiny:s}}{{pokemon}} &7to the GTS and is asking for a &a{{poke_looked_for}}&7!"
            ));

            generic.getNode("Addition", "Success").getList(TypeToken.of(String.class), Lists.newArrayList(
                    "&a&lGTS &e\u00BB &7Your &e{{pokemon}} &7has been added to the listings!"
            ));

            generic.getNode("Addition", "Error", "Empty Slot").getList(TypeToken.of(String.class), Lists.newArrayList(
                    "&c&lGTS &e\u00BB &7Sorry, but it appears slot &e{{slot}} &7is actually empty..."
            ));

            generic.getNode("Addition", "Error", "Exceed Max").getList(TypeToken.of(String.class), Lists.newArrayList(
                    "&c&lGTS &e\u00BB &7Unfortunately, you already have the max number of &e{{max_pokemon}} pokemon&7 in the GTS..."
            ));

            generic.getNode("Addition", "Error", "Invalid Pokemon").getList(TypeToken.of(String.class), Lists.newArrayList(
                    "&c&lGTS &e\u00BB &7Sorry, but &e{{pokemon}} &7has been blacklisted from the GTS..."
            ));

            generic.getNode("Addition", "Error", "Last Pokemon").getList(TypeToken.of(String.class), Lists.newArrayList(
                    "&c&lGTS &e\u00BB &7Woah now, you can't deposit the last pokemon in your party..."
            ));

            generic.getNode("Search", "Error", "Not Found").getList(TypeToken.of(String.class), Lists.newArrayList(
                    "&c&lGTS &e\u00BB &7Unfortunately, we couldn't find any listings matching your search criteria..."
            ));

            generic.getNode("Remove", "Expired").getList(TypeToken.of(String.class), Lists.newArrayList(
                    "&c&lGTS &e\u00BB &7Your market ended, so your &e{{pokemon}} &7has been returned!"
            ));

            generic.getNode("Remove", "Failed").getList(TypeToken.of(String.class), Lists.newArrayList(
                    "&c&lGTS &e\u00BB &7Well shoot, it appears your &e{{pokemon}} &7has already been purchased..."
            ));

            generic.getNode("Remove", "Success").getList(TypeToken.of(String.class), Lists.newArrayList(
                    "&a&lGTS &e\u00BB &7Your &e{{pokemon}} &7has successfully been returned to your possession!"
            ));

            generic.getNode("Purchase", "Success", "Buyer").getList(TypeToken.of(String.class), Lists.newArrayList(
                    "&a&lGTS &e\u00BB &7You successfully purchased that &e{{pokemon}} &7for &c{{curr_symbol}}{{price}}&7!"
            ));

            generic.getNode("Purchase", "Success", "Seller").getList(TypeToken.of(String.class), Lists.newArrayList(
                    "&a&lGTS &e\u00BB &7Your &e{{pokemon}} was purchased for &c{{curr_symbol}}{{price}}&7!"
            ));

            generic.getNode("Purchase", "Error", "Already Sold").getList(TypeToken.of(String.class), Lists.newArrayList(
                    "&c&lGTS &e\u00BB &7Unfortunately, it appears that listing has been sold..."
            ));

            generic.getNode("Purchase", "Error", "Expired").getList(TypeToken.of(String.class), Lists.newArrayList(
                    "&c&lGTS &e\u00BB &7Unfortunately, that lot has recently expired..."
            ));

            generic.getNode("Purchase", "Error", "Not Enough").getList(TypeToken.of(String.class), Lists.newArrayList(
                    "&c&lGTS &e\u00BB &7Unfortunately, it appears you don't have enough to afford this listing...",
                    "&c&lGTS &e\u00BB &7In order to afford it, you need &e{{curr_symbol}}{{money_diff}}"
            ));

            generic.getNode("Purchase", "Failed").getList(TypeToken.of(String.class), Lists.newArrayList(
                    "&c&lGTS &e\u00BB &7Unfortunately, it appears that lot was recently purchased..."
            ));

            generic.getNode("Trade", "Owner", "Receive-Poke").getList(TypeToken.of(String.class), Lists.newArrayList(
                    "&a&lGTS &e\u00BB &7You have received a &e{{poke_looked_for}} &7from &a{{player}} &7in return for your &a{{pokemon}}&7!"
            ));

            generic.getNode("Trade", "Recipient", "Receive-Poke").getList(TypeToken.of(String.class), Lists.newArrayList(
                    "&a&lGTS &e\u00BB &7You have received a &e{{poke_looked_for}} &7from &a{{player}} &7in return for your &a{{pokemon}}&7!"
            ));

            // Admin Settings
            admin.setComment(
                    "+-------------------------------------------------------------------+ #\n" +
                    "|                          Admin Messages                           | #\n" +
                    "+-------------------------------------------------------------------+ #");

            admin.getNode("Clear").getList(TypeToken.of(String.class), Lists.newArrayList(
                    "&a&lGTS &e\u00BB &7You cleared &e{{cleared}} &7lots from the GTS listings!"
            ));
            admin.getNode("Reload").getList(TypeToken.of(String.class), Lists.newArrayList(
                    "&a&lGTS &e\u00BB &7The configuration has been reloaded!"
            ));

            admin.getNode("LotUI", "Remove").getList(TypeToken.of(String.class), Lists.newArrayList(
                    "&a&lGTS &e\u00BB &7You have removed the &e{{pokemon}} &7from the listings!"
            ));

            admin.getNode("LotUI", "Delete").getList(TypeToken.of(String.class), Lists.newArrayList(
                    "&a&lGTS &e\u00BB &7You have removed and deleted the &e{{pokemon}} &7entirely!"
            ));


            // Auction Settings
            auctions.setComment("\n" +
                    "+-------------------------------------------------------------------+ #\n" +
                    "|                        Auction Messages                           | #\n" +
                    "+-------------------------------------------------------------------+ #");

            auctions.getNode("Broadcast").getList(TypeToken.of(String.class), Lists.newArrayList(
                    "&a&lGTS &e\u00BB &c{{player}} &7has started an auction for a &a{{ability}} {{IV%}} {{shiny:s}}{{pokemon}}&7!",
                    "&a&lGTS &e\u00BB &7Starting Price: &e{{curr_symbol}}{{start_price}}",
                    "&a&lGTS &e\u00BB &7Increment: &e{{curr_symbol}}{{increment}}",
                    "&a&lGTS &e\u00BB &7Duration: &e{{expires}}"
            ));

            auctions.getNode("Award").getList(TypeToken.of(String.class), Lists.newArrayList(
                    "&a&lGTS &e\u00BB &c{{player}} &7has won the auction for the &e{{pokemon}} &7by bidding &e{{curr_symbol}}{{price}}&7!"
            ));

            auctions.getNode("Outbid").getList(TypeToken.of(String.class), Lists.newArrayList(
                    "&c&lGTS &e\u00BB &7Your bid on the &e{{pokemon}} &7has been surpassed by &c{{player}}&7!"
            ));

            auctions.getNode("Placed Bid").getList(TypeToken.of(String.class), Lists.newArrayList(
                    "&a&lGTS &e\u00BB &7You have bid a price of &c{{curr_symbol}}{{price}} &7on the &e{{pokemon}}&7!"
            ));

            auctions.getNode("Current Bidder").getList(TypeToken.of(String.class), Lists.newArrayList(
                    "&c&lGTS &e\u00BB &7You are currently the highest bidder for this &e{{pokemon}}&7!"
            ));

            // Pricing Messages
            pricing.setComment("\n" +
                    "+-------------------------------------------------------------------+ #\n" +
                    "|                        Pricing Messages                           | #\n" +
                    "+-------------------------------------------------------------------+ #");

            pricing.getNode("Tax", "Error", "Not Enough").getList(TypeToken.of(String.class), Lists.newArrayList(
                    "&c&lGTS &e\u00BB &7Sorry, but you were unable to afford the tax of &e{{tax}}&7!"
            ));

            pricing.getNode("Tax", "Success", "Paid").getList(TypeToken.of(String.class), Lists.newArrayList(
                    "&a&lGTS &e\u00BB &e{{tax}} &7in taxes has been collected from your listing!"
            ));

            menus.setComment("\n" +
                    "+-------------------------------------------------------------------+ #\n" +
                    "|                        UI Item Messages                           | #\n" +
                    "+-------------------------------------------------------------------+ #");

            menus.getNode("Items", "Balance Icon").getString("&7Balance (&a{{curr_symbol}}{{balance}}&7)");
            menus.getNode("Items", "Last Menu").getString("&cLast Menu");
            menus.getNode("Items", "Page Up").getString("&aNext Page");
            menus.getNode("Items", "Page Down").getString("&cLast Page");
            menus.getNode("Items", "Player Icon").getString("&7Player Info");
            menus.getNode("Items", "Player Listings").getString("&aYour Listings");
            menus.getNode("Items", "Refresh List").getString("&eRefresh Listings");
            menus.getNode("Items", "Search For", "Lore Format").getString("  &7- &3{{pokemon}}");
            menus.getNode("Items", "Search For", "Title").getString("&aSearching for:");

            menus.getNode("Pokemon", "Display").getList(TypeToken.of(String.class), Lists.newArrayList(
                    // TODO Bring the PokemonItem display over into configurable settings
            ));

            loader.save(main);
        } catch (IOException|ObjectMappingException e) {
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

    public static Text getMessage(String path, HashMap<String, Optional<Object>> replacements) {
        String message = main.getNode((Object[]) path.split("\\.")).getString();
        if (message == null) {
            return Text.of(TextColors.RED, "A missing message setup was detected for path: ", TextColors.YELLOW, path);
        }

        if (replacements != null)
            return replaceOptions(message, replacements);
        else
            return TextSerializers.FORMATTING_CODE.deserialize(message);
    }

    public static List<Text> getMessages(String path, HashMap<String, Optional<Object>> replacements) {
        try {
            List<Text> translated = Lists.newArrayList();
            List<String> messages = main.getNode((Object[]) path.split("\\.")).getList(TypeToken.of(String.class));
            if (messages == null) {
                translated.add(Text.of(TextColors.RED, "A missing message setup was detected for path: ", TextColors.YELLOW, path));
                return translated;
            }
            for(String message : messages) {
                if (replacements != null)
                    translated.add(replaceOptions(message, replacements));
                else
                    translated.add(TextSerializers.FORMATTING_CODE.deserialize(message));
            }

            return translated;
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }

        return Lists.newArrayList();
    }

    public static Text replaceOptions(String original, HashMap<String, Optional<Object>> replacements){
        String translated = original;

        for(Tokens token : Tokens.values()){
            if((translated.contains("{{" + token.getToken() + "}}") || translated.contains("{{" + token.getToken() + ":s}}")) && replacements.containsKey(token.getToken())) {
                if (original.contains("{{" + token.getToken() + "}}")) {
                    translated = translated.replaceAll(Pattern.quote("{{" + token.getToken() + "}}"), Matcher.quoteReplacement("" + replacements.get(token.getToken()).orElse("{{" + token.getToken() + "}}")));
                } else {
                    if (replacements.get(token.getToken()).isPresent())
                        translated = translated.replaceAll(Pattern.quote("{{" + token.getToken() + ":s}}"), Matcher.quoteReplacement(replacements.get(token.getToken()).get() + " "));
                    else
                        translated = translated.replaceAll(Pattern.quote("{{" + token.getToken() + ":s}}"), "");
                }
            }
        }

        return TextSerializers.FORMATTING_CODE.deserialize(translated);
    }
}
