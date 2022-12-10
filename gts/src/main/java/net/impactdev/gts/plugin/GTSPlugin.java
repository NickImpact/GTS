package net.impactdev.gts.plugin;

import com.google.inject.Injector;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.extensions.ExtensionManager;
import net.impactdev.gts.api.storage.StorageProvider;
import net.impactdev.gts.locale.TranslationManager;
import net.impactdev.impactor.api.plugin.ImpactorPlugin;

public interface GTSPlugin extends ImpactorPlugin {

    static GTSPlugin instance() {
        return AbstractGTSPlugin.instance();
    }

    GTSService api();

    Injector injector();

    StorageProvider storage();

    ExtensionManager extensions();

    TranslationManager translations();

    boolean inSafeMode();

}
