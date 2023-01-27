package net.impactdev.gts;

import net.impactdev.gts.communication.implementation.CommunicationFactory;
import net.impactdev.gts.plugin.AbstractGTSPlugin;
import net.impactdev.gts.plugin.bootstrapper.GTSBootstrapper;

public class TestPlugin extends AbstractGTSPlugin {

    public TestPlugin(GTSBootstrapper bootstrapper) {
        super(bootstrapper);
    }

    @Override
    protected CommunicationFactory communicationFactory() {
        return new CommunicationFactory();
    }

}
