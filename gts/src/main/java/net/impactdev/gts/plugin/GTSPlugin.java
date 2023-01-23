package net.impactdev.gts.plugin;

import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.modules.markets.ListingManager;
import net.impactdev.gts.communication.implementation.CommunicationService;
import net.impactdev.gts.extensions.ExtensionManager;
import net.impactdev.gts.locale.TranslationManager;
import net.impactdev.gts.storage.GTSStorage;
import net.impactdev.impactor.api.plugin.ImpactorPlugin;
import net.impactdev.impactor.api.plugin.components.Configurable;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.Function;

public interface GTSPlugin extends ImpactorPlugin, Configurable {

    static GTSPlugin instance() {
        return AbstractGTSPlugin.instance();
    }

    GTSService api();

    ListingManager listings();

    GTSStorage storage();

    CommunicationService communication();

    ExtensionManager extensions();

    TranslationManager translations();

    boolean inSafeMode();

    /**
     * Attempts to find and create an InputStream from the resource at the specified
     * path. The parameter here is supplied as a function as calls to this method will
     * supply the root asset path via the input variable. Calls are expected to make use of
     * {@link Path#resolve(Path)} or {@link Path#resolve(String)} for resource identification.
     *
     * @param target The target path provider
     * @return An input stream for the target, if any was found.
     * @throws IllegalArgumentException If the resource could not be located
     */
    InputStream resource(Function<Path, Path> target);

}
