package net.impactdev.gts.communication.implementation;

import net.impactdev.gts.communication.implementation.providers.SingleServerProvider;
import net.impactdev.gts.configuration.GTSConfigKeys;
import net.impactdev.gts.plugin.GTSPlugin;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.logging.PluginLogger;

public class CommunicationFactory {

    public final CommunicationService instance() {
        PluginLogger logger = GTSPlugin.instance().logger();
        Config config = GTSPlugin.instance().configuration();

        String requested = config.get(GTSConfigKeys.MESSAGING_SERVICE);
        if(requested.equalsIgnoreCase("none")) {
            return new CommunicationService(new SingleServerProvider());
        }

        return null;
    }

}
