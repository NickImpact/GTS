package net.impactdev.gts.forge;

import net.impactdev.gts.plugin.bootstrapper.GTSBootstrapper;
import net.impactdev.impactor.api.logging.Log4jLogger;
import net.impactdev.impactor.api.utilities.ExceptionPrinter;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;

import java.nio.file.Paths;

@Mod("gts")
public final class ForgeGTSBootstrap extends GTSBootstrapper {

    private final ForgeGTSPlugin plugin;

    public ForgeGTSBootstrap() {
        super(
                new Log4jLogger(LogManager.getLogger("GTS")),
                Paths.get("config").resolve("gts"),
                Paths.get("gts")
        );

        this.plugin = new ForgeGTSPlugin(this);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConstruct);
    }

    public void onConstruct(FMLConstructModEvent event) {
        this.logger().info("Launching GTS");

        try {
            this.plugin.construct();
        } catch (Exception e) {
            ExceptionPrinter.print(this.plugin, e);
            this.logLaunchFailure(e);
        }
    }
}
