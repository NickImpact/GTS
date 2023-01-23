package net.impactdev.gts.api.events.listings;

import net.impactdev.gts.api.elements.listings.Listing;
import net.impactdev.gts.api.events.metadata.CancellableEvent;
import net.impactdev.impactor.api.platform.sources.PlatformSource;
import org.spongepowered.api.util.annotation.eventgen.GenerateFactoryMethod;

/**
 * Represents when a listing is being published on the market.
 */
@GenerateFactoryMethod
public interface PublishListingEvent extends CancellableEvent {

    Listing listing();

    PlatformSource lister();

}
