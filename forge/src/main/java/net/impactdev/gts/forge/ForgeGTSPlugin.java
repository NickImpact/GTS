package net.impactdev.gts.forge;

import net.impactdev.gts.locale.TranslationManager;
import net.impactdev.gts.plugin.AbstractGTSPlugin;
import net.impactdev.gts.plugin.bootstrapper.GTSBootstrapper;

public class ForgeGTSPlugin extends AbstractGTSPlugin {

    public ForgeGTSPlugin(GTSBootstrapper bootstrapper) {
        super(bootstrapper);
    }

    @Override
    public TranslationManager translations() {
        return null;
    }

    @Override
    public boolean inSafeMode() {
        return false;
    }

}
