package net.impactdev.gts.extensions.reforged;

import com.google.inject.Inject;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.extensions.Extension;

// TODO - build.gradle.kts.kts.kts needs to enforce ForgeGradle
public class ReforgedExtension implements Extension {

    private final GTSService service;

    // TODO - We probably want to provide additional parameters, such as configuration/data directory paths
    @Inject
    public ReforgedExtension(final GTSService service) {
        this.service = service;
    }

    @Override
    public void construct() {

    }

    @Override
    public void enable() {

    }

    @Override
    public void shutdown() {

    }

}
