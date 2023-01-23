package net.impactdev.gts.forge;

import net.impactdev.gts.communication.implementation.CommunicationFactory;
import net.impactdev.gts.plugin.AbstractGTSPlugin;
import net.impactdev.gts.plugin.bootstrapper.GTSBootstrapper;

public class ForgeGTSPlugin extends AbstractGTSPlugin {

    public ForgeGTSPlugin(GTSBootstrapper bootstrapper) {
        super(bootstrapper);
    }

    @Override
    protected CommunicationFactory communicationFactory() {
        return new CommunicationFactory();
    }

}
