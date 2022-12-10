package net.impactdev.gts.api.modules.markets.global;

import net.impactdev.gts.api.modules.GTSModule;
import net.impactdev.gts.api.modules.markets.ListingManager;

public interface GTSMarketModule extends GTSModule {

    /**
     * Provides the manager responsible for processing transactions within the global GTS market.
     *
     * @return
     */
    ListingManager manager();

}
