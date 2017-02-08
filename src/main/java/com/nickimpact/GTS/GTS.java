package com.nickimpact.GTS;

import com.google.inject.Inject;
import com.nickimpact.GTS.Commands.*;
import com.nickimpact.GTS.Configuration.Config;
import com.nickimpact.GTS.Configuration.MessageConfig;
import com.nickimpact.GTS.Listeners.InventoryListener;
import com.nickimpact.GTS.Listeners.JoinListener;
import com.nickimpact.GTS.Storage.H2Provider;
import com.nickimpact.GTS.Storage.MySQLProvider;
import com.nickimpact.GTS.Storage.SQLDatabase;
import com.nickimpact.GTS.Utils.UpdateLotsTask;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.nio.file.Path;

import static com.nickimpact.GTS.GTSInfo.*;

/**----------------------------------------------------------------------------
 *   GTS (Sponge Edition)
 *   Developer: NickImpact
 *
 *
 *---------------------------------------------------------------------------*/


@Plugin(id=ID, name=NAME, version=VERSION, description=DESCRIPTION)
public class GTS {

    @Inject
    private Logger logger;

    @Inject
    @ConfigDir(sharedRoot=false)
    private Path configDir;

    private Config config;
    private MessageConfig messageConfig;

    private static GTS plugin;
    private SQLDatabase sql;

    private EconomyService economy;

    private boolean enabled = true;

    @Listener
    public void onInitialization(GameInitializationEvent e){
        plugin = this;

        getLogger().info("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
        getLogger().info("Thanks for using " + NAME + " (" + VERSION + ")");
        getLogger().info("");
        getLogger().info("Plugin developed by NickImpact (Nick)");
        getLogger().info("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
        getLogger().info("Initializing plugin components...");
        getLogger().info("");

        if(!Sponge.getPluginManager().isLoaded("pixelmon")){
            getLogger().error(Text.of(TextColors.RED, "Sponge was unable to detect Pixelmon..").toPlain());
            getLogger().error(Text.of(TextColors.RED, "  Please ensure you have pixelmon installed, and that").toPlain());
            getLogger().error(Text.of(TextColors.RED, "  it enabled correctly!").toPlain());
            getLogger().info("");
            enabled = false;
        }

        if(!Sponge.getPluginManager().isLoaded("nbthandler")){
            getLogger().error(Text.of(TextColors.RED, "Sponge was unable to detect the NBTHandler..").toPlain());
            getLogger().error(Text.of(TextColors.RED, "  Please ensure you have that installed, and that").toPlain());
            getLogger().error(Text.of(TextColors.RED, "  it enabled correctly!").toPlain());
            getLogger().info("");
            enabled = false;
        }

        if(enabled) {
            this.config = new Config();
            if(config.getDatabaseType().equalsIgnoreCase("H2")){
                this.sql = new H2Provider();
            } else if(config.getDatabaseType().equalsIgnoreCase("MySQL")){
                this.sql = new MySQLProvider();
            } else {
                logger.error(Text.of(TextColors.RED, "Invalid database type passed, defaulting to H2..").toString());
                this.sql = new H2Provider();
            }
            this.sql.createTable();
            Sponge.getCommandManager().register(this, CommandSpec.builder()
                    .permission("gts.use")
                    .executor(new GTSCommand())
                    .description(Text.of("Opens the GUI displaying all GTS listings"))
                    .child(CommandSpec.builder()
                            .executor(new AdditionCommand())
                            .arguments(GenericArguments.integer(Text.of("slot")), GenericArguments.integer(Text.of("price")))
                            .description(Text.of("Add a pokemon to the GTS based on your party slots"))
                            .build(), "add")
                    .child(CommandSpec.builder()
                            .executor(new SearchCommand())
                            .arguments(GenericArguments.remainingJoinedStrings(Text.of("pokemon")))
                            .description(Text.of("Search for a pokemon within the GTS"))
                            .build(), "search")
                    .child(CommandSpec.builder()
                            .executor(new ReloadCommand())
                            .permission("gts.admin")
                            .description(Text.of("Reload the plugins configuration"))
                            .build(), "reload")
                    .child(CommandSpec.builder()
                            .executor(new ClearCommand())
                            .permission("gts.admin")
                            .description(Text.of("Clear all listings in the GTS"))
                            .build(), "clear")
                    .child(CommandSpec.builder()
                            .executor(new EditCommand())
                            .permission("gts.admin")
                            .description(Text.of("Edit listings in the GTS"))
                            .build(), "edit")
                    .child(CommandSpec.builder()
                            .executor(new HelpCommand())
                            .description(Text.of("Receive help info on GTS"))
                            .build(), "help")
                    .build(), "gts");
            getLogger().info("    - Commands injected into registry");

            messageConfig = new MessageConfig();

            Sponge.getEventManager().registerListeners(this, new JoinListener());
            Sponge.getEventManager().registerListeners(this, new InventoryListener());
            getLogger().info("    - Listeners registered into system");
            getLogger().info("");
            getLogger().info("GTS has successfully enabled, and is ready for service");
            getLogger().info("Let the GTS experience, commence!");
            getLogger().info("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
        } else {
            getLogger().error("All GTS functions will be suspended and unusable!");
            getLogger().info("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
        }
    }

    @Listener
    public void onServerStart(GameStartedServerEvent e){
        if(enabled) {
            new UpdateLotsTask().setupUpdateTask();
        }
    }

    @Listener
    public void onReload(GameReloadEvent e){
        if(enabled){
            this.config = new Config();
            this.messageConfig = new MessageConfig();
            getLogger().info(Text.of(TextColors.YELLOW, "Configurations have been reloaded").toPlain());
        }
    }

    @Listener
    public void registerEconomyService(ChangeServiceProviderEvent e){
        if(e.getService().equals(EconomyService.class)){
            economy = (EconomyService) e.getNewProviderRegistration().getProvider();
        }
    }

    @Listener
    public void onServerStop(GameStoppingEvent e){
        if(enabled) {
            getLogger().info("Thanks for using " + NAME + " (" + VERSION + ")");
            try {
                sql.shutdown();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } else {
            getLogger().info(Text.of(TextColors.RED, "Remember to fix the issue that came about earlier on startup!").toPlain());
        }
    }

    public Logger getLogger(){
        return this.logger;
    }

    public SQLDatabase getSql(){
        return this.sql;
    }

    public static GTS getInstance(){
        return plugin;
    }

    public Config getConfig(){
        return config;
    }

    public Path getConfigDir(){
        return configDir;
    }

    public EconomyService getEconomy(){
        return this.economy;
    }

    public boolean isPluginEnabled(){
        return this.enabled;
    }

    public MessageConfig getMessageConfig() {
        return messageConfig;
    }
}
