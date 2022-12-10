package net.impactdev.gts.api.events.listings;

import net.impactdev.gts.api.components.listings.Listing;
import net.impactdev.impactor.api.events.ImpactorEvent;
import net.impactdev.impactor.api.platform.sources.PlatformSource;

/**
 * Represents when a listing is being published on the market.
 */
public interface PublishListingEvent extends ImpactorEvent {

    Listing listing();

    PlatformSource lister();

}
