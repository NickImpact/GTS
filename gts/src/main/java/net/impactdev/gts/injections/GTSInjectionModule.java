package net.impactdev.gts.injections;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.modules.markets.ListingManager;
import net.impactdev.gts.components.listings.GTSListingManager;
import net.impactdev.gts.service.GTSServiceImplementation;

@Singleton
public class GTSInjectionModule extends AbstractModule {

    @Override
    protected void configure() {
        this.bind(GTSService.class).to(GTSServiceImplementation.class);
        this.bind(ListingManager.class).to(GTSListingManager.class);
    }

}
